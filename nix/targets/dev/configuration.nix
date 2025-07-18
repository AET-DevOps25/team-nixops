{
  self,
  lib,
  pkgs,
  ...
}: let
  nixosVars = builtins.fromJSON (builtins.readFile ./nixos-vars.json);
in {
  imports = [
    self.nixosModules.aws
  ];
  users.users.root.openssh.authorizedKeys.keys =
    nixosVars.ssh_keys
    #TODO:remove when https://github.com/hashicorp/terraform-provider-aws/issues/43425 solved
    ++ [
      "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIEbn4CJG3JtDrziLAEQ21bZxL5w4+MkDwD17LoQeEuJc florian@nixoslaptop"
      "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIBKg7pWqDj8X+4YFbrL99PgwuIfV8W4J1tsClG2e1A8w openpgp:0x7505C713"
      #GitHub
      "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIL1xW4x7Kx2zWR6dH9immMbtqAzt5jXo/XfxsdsWVWu0 mikilio@elitebook"
    ];
  system.stateVersion = "25.05";
  security.acme.defaults.email = "not.a.real.mail@nixops.aet.cit.tum.de";

  networking = {
    hostName = "dev";
    domain = "nixops.aet.cit.tum.de";
    nftables.enable = true;
  };

  services.cloud-init.enable = lib.mkForce false;

  systemd.network.networks = {
    "10-wan" = {
      # match the interface by name
      # matchConfig.MACAddress = "00:00:00:00:00:00";
      matchConfig.Name = "ens5";
      DHCP = "ipv4";
      # address =
      #   []
      #   ++ (lib.optional (nixosVars ? private_ip) "${nixosVars.private_ip}/20")
      #   ++ (lib.optional (nixosVars ? public_ip) "${nixosVars.public_ip}");
      # routes = [
      #   {Destination = nixosVars.cidr_block;}
      # ];
      # # make the routes on this interface a dependency for network-online.target
      # linkConfig.RequiredForOnline = "routable";
    };
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

  services.nginx.config =
    #nginx
    ''
      events {
          worker_connections 1024;
      }

      http {
          default_type  application/octet-stream;

          # This is where your `server` block goes:
          server {
              listen 80;
              server_name _;

              location / {
                  proxy_pass http://localhost:3000;
                  proxy_set_header Host $host;
                  proxy_set_header X-Real-IP $remote_addr;
              }

              location /genai/ {
                  rewrite ^/genai(/.*)$ $1 break;
                  proxy_pass http://localhost:8000;
                  proxy_set_header Host $host;
                  proxy_set_header X-Real-IP $remote_addr;
              }

              location /schedule/ {
                  rewrite ^/schedule(/.*)$ $1 break;
                  proxy_pass http://localhost:8042;
                  proxy_set_header Host $host;
                  proxy_set_header X-Real-IP $remote_addr;
              }

              location /scraper/ {
                  rewrite ^/scraper(/.*)$ $1 break;
                  proxy_pass http://localhost:8080;
                  proxy_set_header Host $host;
                  proxy_set_header X-Real-IP $remote_addr;
              }
          }
      }
    '';

  environment.systemPackages = with pkgs; [
    podman-compose
  ];

  sops.secrets.env.path = "/root/.env";
  sops.defaultSopsFile = ./secrets/secrets.yaml;
}
