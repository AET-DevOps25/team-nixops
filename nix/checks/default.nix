{
  pkgs,
  self,
  ...
}: {
  nixos-k8s = pkgs.testers.runNixOSTest {
    imports = [./k8s];
    node.specialArgs = {inherit self;};
  };
  nixos-podman = pkgs.testers.runNixOSTest {
    imports = [./podman];
  };
}
