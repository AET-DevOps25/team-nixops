{config, ...}: let
  cfg = config.infra;
in {
  imports = [
    ./loadbalancer.nix
    ./control.nix
    ./etcd.nix
    ./worker.nix
  ];

  config = {
    assertions = [
      {
        assertion = cfg.clusterConfigDir != null;
        message = "infra: No value set for `config.infra.clusterConfigDir`";
      }
    ];

    sops.secrets.ca = {};

    services.kubernetes = {
      clusterCidr = "10.244.0.0/16";
      masterAddress = cfg.apiAdress;
      caFile = config.sops.secrets.ca.path;
    };
  };
}
