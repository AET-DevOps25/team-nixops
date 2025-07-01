{
  lib,
  config,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    ./base.nix
  ];

  config = let
    getIP = vars: (builtins.head vars.network).ip;

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

    etcds = lib.attrsets.filterAttrs (name: _: null != (builtins.match "etcd-.*" name)) cfg.nodeConfigs;
  in
    lib.mkIf (cfg.role == "control") {
      sops.secrets = {
        controller-manager = {};
        controller-manager-key = {};
        scheduler = {};
        scheduler-key = {};
        etcd-ca = {};
        etcd-client = {};
        etcd-client-key = {};
        kubelet-client = {};
        kubelet-client-key = {};
        service-account-verify = {};
        service-account-key = {};
        apiserver = {};
        apiserver-key = {};
      };

      networking.firewall = {
        allowedTCPPorts = [6443];
      };
      systemd.services.etcd = {
        wants = ["network-online.target"];
        after = ["network-online.target"];
      };

      services.kubernetes = {
        controllerManager = {
          enable = true;
          kubeconfig = {
            certFile = config.sops.secrets.controller-manager.path;
            keyFile = config.sops.secrets.controller-manager-key.path;
            server = "https://${cfg.apiAdress}";
          };
        };
        scheduler = {
          enable = true;
          kubeconfig = {
            certFile = config.sops.secrets.scheduler.path;
            keyFile = config.sops.secrets.scheduler-key.path;
            server = "https://${cfg.apiAdress}";
          };
        };
        #TODO: add audit policies
        apiserver = {
          enable = true;
          advertiseAddress = "https://${cfg.apiAdress}";
          serviceClusterIpRange = "10.96.0.0/12";

          authorizationMode = ["RBAC" "Node"];
          authorizationPolicy = corednsPolicies;

          etcd = {
            servers =
              lib.attrsets.mapAttrsToList
              (name: value: "https://${getIP value}:2379")
              etcds;
            caFile = config.sops.secrets.etcd-ca.path;
            certFile = config.sops.secrets.etcd-client.path;
            keyFile = config.sops.secrets.etcd-client-key.path;
          };

          kubeletClientCertFile = config.sops.secrets.kubelet-client.path;
          kubeletClientKeyFile = config.sops.secrets.kubelet-client-key.path;

          serviceAccountKeyFile = config.sops.secrets.service-account-verify.path;
          serviceAccountSigningKeyFile = config.sops.secrets.service-account-key.path;

          tlsCertFile = config.sops.secrets.apiserver.path;
          tlsKeyFile = config.sops.secrets.apiserver-key.path;
        };
      };
    };
}
