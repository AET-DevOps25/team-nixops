{pkgs, ...}: {
  networking = {
    nftables.enable = true;
    firewall = {
      interfaces."podman*".allowedUDPPorts = [53];
    };
  };
  systemd.network.networks = {
    "11-podman" = {
      matchConfig.Name = "podman*";
      linkConfig.Unmanaged = true;
    };
  };

  virtualisation = {
    containers = {
      enable = true;
    };
    oci-containers.backend = "podman";
    podman = {
      enable = true;
      autoPrune = {
        enable = true;
        dates = "daily";
        flags = ["--all"];
      };
      dockerCompat = true;
      dockerSocket.enable = true;
    };
  };

  environment.systemPackages = with pkgs; [
    podman-compose
  ];
}
