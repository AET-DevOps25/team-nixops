{
  lib,
  config,
  ...
}: {
  options.infra = {
    role = lib.mkOption {
      type = lib.types.enum ["worker" "control" "etcd" "loadbalancer"];
      description = ''
        Role of the node in the cluster.
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
      type = lib.types.str;
      description = ''
        IP address of the apiserver
      '';
    };

    nodeConfigs = lib.mkOption {
      type = lib.types.attrs;
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
