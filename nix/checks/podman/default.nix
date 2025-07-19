{pkgs, ...}: {
  name = "podman";
  nodes = {
    machine = {...}: {
      imports = [
        ../../modules/podman.nix
      ];
      system.stateVersion = "25.05";

      virtualisation.diskSize = 12288;

      networking = {
        hostName = "test";
        nftables.enable = true;
      };

      systemd.network.networks = {
        "10-wan" = {
          matchConfig.Name = "ens5";
          DHCP = "ipv4";
        };
      };
    };
  };

  testScript =
    #python
    ''
      machine.wait_for_unit("multi-user.target")
      machine.succeed("cd")
      machine.succeed("curl -L https://raw.githubusercontent.com/AET-DevOps25/team-nixops/refs/heads/main/docker-compose.yml -o docker-compose.yml")
      machine.copy_from_host( "${./env.txt}", ".env")
      machine.succeed("docker compose pull")
      machine.succeed("docker compose up -d")

      machine.sleep(20)
      output = machine.succeed("docker ps -a --format '{{.Names}} {{.Status}}'").strip().splitlines()

      for line in output:
          name, status = line.split(" ", 1)
          print(f"Container {name} status: {status}")
          assert not status.startswith("Exited"), f"Container {name} exited or crashed: {status}"
    '';
}
