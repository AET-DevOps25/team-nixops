{self, ...}: let
  partitions = {
    ESP = {
      size = "500M";
      type = "EF00"; # for grub MBR
      content = {
        type = "filesystem";
        format = "vfat";
        mountpoint = "/boot";
      };
    };
    root = {
      size = "100%";
      content = {
        type = "filesystem";
        format = "ext4";
        mountpoint = "/";
      };
    };
  };
in {
  imports = [
    self.inputs.disko.nixosModules.disko
  ];
  disko.devices = {
    disk.nvme0n1 = {
      type = "disk";
      device = "/dev/nvme0n1";
      content = {
        type = "gpt";
        inherit partitions;
      };
    };
  };
}
