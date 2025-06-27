{
  lib,
  self,
  ...
}: let
  entries = builtins.attrNames (builtins.readDir ./.);
  result = lib.lists.partition (dir: builtins.pathExists (./. + "/${dir}/configuration.nix")) entries;
  single =
    map (dir: {
      name = dir;
      node = null;
    })
    result.right;
  multi = builtins.filter (entry: builtins.readFileType ./${entry} == "directory") result.wrong;
  nodes = lib.lists.flatten (map (dir:
    map (e: {
      name = dir;
      node = builtins.head e;
    }) (builtins.filter (e: e != null) (map (entry: builtins.match "(.*\).nix$" entry) (builtins.attrNames (builtins.readDir ./${dir})))))
  multi);
in {
  flake.nixosConfigurations = lib.listToAttrs (
    map (
      conf:
        with conf; {
          name =
            if (isNull node)
            then name
            else "${name}-${node}";

          value = lib.nixosSystem {
            system = "x86_64-linux";
            # Make flake available in modules
            specialArgs = {
              self = {
                inputs = self.inputs;
                nixosModules = self.nixosModules;
              };
            };

            modules = [
              (./.
                + (
                  if (isNull node)
                  then "/${name}/configuration.nix"
                  else "/${name}/${node}.nix"
                ))
            ];
          };
        }
    )
    (single ++ nodes)
  );
}
