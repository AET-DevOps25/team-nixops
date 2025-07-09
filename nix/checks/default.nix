{
  pkgs,
  self,
  ...
}: {
  nixos-k8s = pkgs.testers.runNixOSTest {
    imports = [./k8s.nix];
    node.specialArgs = {inherit self;};
  };
}
