{
  inputs,
  lib,
  ...
}: {
  flake.nixosModules = {
    hcloud.imports = [
      inputs.srvos.nixosModules.server
      inputs.sops-nix.nixosModules.sops
      inputs.srvos.nixosModules.hardware-hetzner-cloud
      ./single-disk-mbr.nix
      ./nix.nix
      {
        infra.rootDevice = "sda";
        sops.age.keyFile = "/var/lib/secrets/age";
      }
    ];
    k8s.imports = [
      inputs.sops-nix.nixosModules.sops
      ./k8s
      {
        sops.age.keyFile = "/var/lib/secrets/age";
      }
    ];
    aws.imports = [
      inputs.srvos.nixosModules.server
      inputs.sops-nix.nixosModules.sops
      inputs.srvos.nixosModules.hardware-amazon
      inputs.srvos.nixosModules.mixins-telegraf
      inputs.srvos.nixosModules.roles-prometheus
      inputs.srvos.nixosModules.mixins-nginx
      ./single-disk-uefi.nix
      ./nix.nix
      {
        ec2.efi = true;
        #for ssm
        security.sudo.execWheelOnly = lib.mkForce false;
        sops.age.keyFile = "/var/lib/secrets/age";
      }
    ];
  };
}
