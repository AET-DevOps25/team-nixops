{
  lib,
  self,
  config,
  inputs,
  terraform,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    inputs.sops-nix.nixosModules.sops
  ];

  options = {
    cfg.role = lib.mkOption {
      type = lib.types.string;
      description = ''
        Role of the node in the cluster. Supports:
        "worker" | "control" | "etcd" | "loadbalancer"
      '';
    };
    cfg.clusterConfigDir = lib.mkOption {
      type = lib.types.path;
      description = ''
        Path of directory containing nixos-vars files.
        Expected to include information such as ids, names, IPs, ...
      '';
    };

    config = let
      #NOTE: pick one that wasn't assigned yet;
      apiAdress = "10.0.0.100";

      corednsPolicies = [
        {
          apiVersion = "rbac.authorization.k8s.io/v1";
          kind = "ClusterRole";
          metadata.name = "coredns-reader";
          rules.apiGroups =
            map (r: {
              apiGroups = [""];
              resources = r;
              verbs = ["get" "list" "watch"];
            }) [
              "endpoints"
              "services"
            ];
        }
        {
          apiVersion = "rbac.authorization.k8s.io/v1";
          kind = "ClusterRoleBinding";
          metadata.name = "coredns-reader-binding";
          subjects = [
            {
              kind = "ServiceAccount";
              name = "coredns";
              namespace = "kube-system";
            }
          ];
          roleRef = {
            kind = "ClusterRole";
            name = "coredns-reader";
            apiGroup = "rbac.authorization.k8s.io";
          };
        }
      ];

      nodes = lib.lists.flatten (
        builtins.filter (e: e != null) (map (
          name: builtins.match "(.*\).json$" name
        ) (builtins.attrNames (builtins.readDir cfg.clusterConfigDir)))
      );
      nodesConfig = builtins.listToAttrs (
        map (node: {
          name = node;
          value = builtins.fromJSON (builtins.readFile "${cfg.clusterConfigDir}/${node}.json");
        })
        nodes
      );

      etcds = lib.attrsets.filterAttrs (name: _: null != (builtins.match "etcd-.*" name)) nodesConfig;

      controls = lib.attrsets.filterAttrs (name: _: null != (builtins.match "control-.*" name)) nodesConfig;

      thisNode = nodesConfig.${terraform.name};
    in {
      assertions = [
        {
          assertion = cfg.clusterConfigDir != null;
          message = "infra: No value set for `config.infra.clusterConfigDir`";
        }
      ];

      boot.kernel.sysctl."net.ipv4.ip_nonlocal_bind" = true;

      networking.firewall = {
        extraCommands = "iptables -A INPUT -p vrrp -j ACCEPT";
        extraStopCommands = "iptables -D INPUT -p vrrp -j ACCEPT || true";
        allowedTCPPorts =
          [6443]
          ++ [2379 2380]
          ++ [config.services.kubernetes.kubelet.port]
          ++ [443];
      };

      services.haproxy = {
        enable = true;
        # TODO: backend healthchecks
        config = let
          backends =
            lib.attrsets.mapAttrsToList
            (name: value: "server ${name} ${value.network.ip}:6443")
            controls;
        in ''
          defaults
            timeout connect 10s

          frontend k8s
            mode tcp
            bind *:443
            default_backend controlplanes

          backend controlplanes
            mode tcp
            ${builtins.concatStringsSep "\n  " backends}
        '';
      };

      services.keepalived = {
        enable = true;
        vrrpInstances.k8s = {
          # TODO: at least basic (hardcoded) auth or other protective measures
          interface = "ens3";
          priority =
            # Prioritize loadbalancer1 over loadbalancer2 over loadbalancer3, etc.
            let
              number = lib.strings.toInt (lib.strings.removePrefix "loadbalancer" self.values.name);
            in
              200 - number;
          virtualRouterId = 42;
          virtualIps = [
            {
              addr = apiAdress;
            }
          ];
        };
      };

      services.etcd = {
        enable = true;

        initialcluster = lib.mkforce (
          lib.attrsets.mapAttrsToList
          (name: value: "${name}=${value.network.ip}")
          etcds
        );
        listenClientUrls = ["https://${thisNode.network.ip}:2379" "https://127.0.0.1:2379"];
        listenPeerUrls = ["https://${thisNode.network.ip}:2380" "https://127.0.0.1:2380"];

        clientcertauth = true;
        peerclientcertauth = true;

        certfile = config.sops.secrets.server.path;
        keyfile = config.sops.secrets.server-key.path;

        peercertfile = config.sops.secrets.peer.path;
        peerkeyfile = config.sops.secrets.peer-key.path;

        peertrustedcafile = config.sops.secrets.ca.path;
        trustedcafile = config.sops.secrets.ca.path;
      };

      systemd.services.etcd = {
        wants = ["network-online.target"];
        after = ["network-online.target"];
      };
      services.kubernetes = {
        clustercidr = "10.244.0.0/16";
        cafile = config.sops.secrets.ca.path;
        controllerManager = {
          enable = true;
          kubeconfig = {
            certFile = config.sops.secrets.controller-manager.path;
            keyFile = config.sops.secrets.controller-manager-key.path;
            server = "https://${apiAdress}";
          };

          serviceAccountKeyFile = config.sops.secrets.controller-account-key.path;
        };
        scheduler = {
          enable = true;
          kubeconfig = {
            certFile = config.sops.secrets.scheduler.path;
            keyFile = config.sops.secrets.scheduler-key.path;
            server = "https://${apiAdress}";
          };
        };
        #TODO: add audit policies
        apiserver = {
          enable = true;
          advertiseAddress = "https://${apiAdress}";
          serviceClusterIpRange = "10.96.0.0/12";

          authorizationMode = ["RBAC" "Node"];
          authorizationPolicy = corednsPolicies;

          etcd = {
            servers =
              lib.attrsets.mapAttrsToList
              (name: value: "https://${value.network.ip}:2379")
              etcds;
            caFile = config.sops.secrets.etcd-ca.path;
            certFile = config.sops.secrets.etcd-client.path;
            keyFile = config.sops.secrets.etcd-client-key.path;
          };

          kubeletClientCertFile = config.sops.secrets.kubelet-client.path;
          kubeletClientKeyFile = config.sops.secrets.kubelet-client-key.path;

          serviceAccountKeyFile = config.sops.secrets.apiserver-account-key.path;
          serviceAccountSigningKeyFile = config.sops.secrets.apiserver-account-signing-key.path;

          tlsCertFile = config.sops.secrets.apiserver.path;
          tlsKeyFile = config.sops.secrets.apiserver-key.path;
        };

        kubelet = {
          enable = true;
          unschedulable = false;
          tlsCertFile = config.sops.secrets.kubelet.path;
          tlsKeyFile = config.sops.secrets.kubelet-key.path;
        };

        proxy = {
          enable = true;
          kubeconfig = {
            certFile = config.sops.secrets.proxy.path;
            keyFile = config.sops.secrets.proxy-key.path;
            server = "https://${apiAdress}";
          };
        };

        addons = {
          dns = {
            enable = true;
            #TODO: don't use internal resolvers and intead only forward to dedicated DNS server
            corefile = ''
              .:10053 {
                errors
                health :10054
                kubernetes ${config.services.kubernetes.addons.dns.clusterDomain} in-addr.arpa ip6.arpa {
                  pods verify
                  fallthrough in-addr.arpa ip6.arpa
                }
                prometheus :10055
                forward . /etc/resolv.conf
                cache 30
                loop
                reload
                loadbalance
              }
            '';
          };
        };
      };
    };
  };
}
