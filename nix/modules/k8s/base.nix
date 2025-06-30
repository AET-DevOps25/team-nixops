{
  lib,
  config,
  inputs,
  ...
}: {
  imports = [
    inputs.sops-nix.nixosModules.sops
  ];

  options.infra = {
    role = lib.mkOption {
      type = lib.types.string;
      description = ''
        Role of the node in the cluster. Supports:
        "worker" | "control" | "etcd" | "loadbalancer"
      '';
    };
    clusterConfigDir = lib.mkOption {
      type = lib.types.path;
      description = ''
        Path of directory containing nixos-vars files.
        Expected to include information such as ids, names, IPs, ...
      '';
    };

    apiAdress = lib.mkOption {
      type = lib.typse.string;
      description = ''
        IP address of the apiserver
      '';
    };

    nodeCofigs = lib.mkOption {
      type = lib.types.str;
      default = let
        cfg = config.infra;
        nodes = lib.lists.flatten (
          builtins.filter (e: e != null) (map (
            name: builtins.match "(.*\).json$" name
          ) (builtins.attrNames (builtins.readDir cfg.clusterConfigDir)))
        );
      in
        builtins.listToAttrs (
          map (node: {
            name = node;
            value = builtins.fromJSON (builtins.readFile "${cfg.clusterConfigDir}/${node}.json");
          })
          nodes
        );
      readOnly = true;
      description = "This is a collection of all nodes nixos-vars files.";
    };
  };
}
