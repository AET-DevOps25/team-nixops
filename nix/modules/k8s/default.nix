{
  config,
  inputs,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    inputs.sops-nix.nixosModules.sops
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
      clustercidr = "10.244.0.0/16";
      cafile = config.sops.secrets.ca.path;
    };
  };
}
