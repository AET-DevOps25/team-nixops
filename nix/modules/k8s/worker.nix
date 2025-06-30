{
  config,
  inputs,
  lib,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    inputs.sops-nix.nixosModules.sops
    ./base.nix
  ];

  config = lib.mkIf (cfg.role == "worker") {
    networking.firewall = {
      allowedTCPPorts = [config.services.kubernetes.kubelet.port];
    };

    sops.secrets = {
      kubelet = {};
      kubelet-key = {};
      proxy = {};
      proxy-key = {};
    };

    services.kubernetes = {
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
          server = "https://${cfg.apiAdress}";
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
}
