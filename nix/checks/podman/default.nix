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
      def launch_browser():
        """Launches the web browser with the correct options."""
        # Determine the name of the binary:
        binary = ${pkgs.chromium}/bin/chromium
        # Add optional CLI options:
        options = []
        if major_version > "95" and not pname.startswith("google-chrome"):
            # Workaround to avoid a GPU crash:
            options.append("--use-gl=swiftshader")
        # Launch the process:
        options.append("http://localhost:3000")
        machine.succeed(ru(f'ulimit -c unlimited; {binary} {shlex.join(options)} >&2 & disown'))
        if binary.startswith("google-chrome"):
            # Need to click away the first window:
            machine.wait_for_text("Make Google Chrome the default browser")
            machine.screenshot("google_chrome_default_browser_prompt")
            machine.send_key("ret")

      machine.wait_for_unit("multi-user.target")
      machine.succeed("cd")
      machine.succeed("curl -L https://raw.githubusercontent.com/AET-DevOps25/team-nixops/refs/heads/main/docker-compose.yml -o docker-compose.yml")
      machine.copy_from_host( "${./env.txt}", ".env")
      machine.succeed("docker compose pull")
      machine.succeed("docker compose up -d")

      launch_browser()

      machine.sleep(20)
      output = machine.succeed("docker ps -a --format '{{.Names}} {{.Status}}'").strip().splitlines()

      for line in output:
          name, status = line.split(" ", 1)
          print(f"Container {name} status: {status}")
          assert not status.startswith("Exited"), f"Container {name} exited or crashed: {status}"
    '';
}
