{
  # One or more machines:
  name = "k8s";
  nodes = let
    mkModule = {
      self,
      name,
      role,
    }: {
      imports = [self.nixosModules.k8s];
      networking = {
        hostName = name;
      };
      infra = {
        inherit role;
        clusterConfigDir = ./nixos-vars;
        apiAdress = "10.0.0.254";
      };
      sops.defaultSopsFile = ./secrets/${name}.yaml;
    };
  in {
    worker = {self, ...}: {
      imports = [
        (mkModule {
          inherit self;
          name = "worker-0";
          role = "worker";
        })
      ];
    };
    control = {self, ...}: {
      imports = [
        (mkModule {
          inherit self;
          name = "control-0";
          role = "control";
        })
      ];
    };
    etcd = {self, ...}: {
      imports = [
        (mkModule {
          inherit self;
          name = "etcd-0";
          role = "etcd";
        })
      ];
    };
  };

  testScript = ''

  '';
}
