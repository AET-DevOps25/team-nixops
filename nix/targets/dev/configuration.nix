{
  self,
  lib,
  config,
  ...
}: let
  nixosVars = builtins.fromJSON (builtins.readFile ./nixos-vars.json);
in {
  imports = [
    self.nixosModules.hcloud
  ];
  users.users.root.openssh.authorizedKeys.keys = nixosVars.ssh_keys;
  system.stateVersion = "25.05";
  security.acme.defaults.email = "not.a.real.mail@nixops.aet.cit.tum.de";

  networking = {
    hostName = "dev";
    domain = "nixops.aet.cit.tum.de";
  };

  services.cloud-init.enable = lib.mkForce false;

  systemd.network.networks."10-wan" = {
    # match the interface by name
    # matchConfig.MACAddress = "00:00:00:00:00:00";
    matchConfig.Name = "enp1s0";
    address = [
      # configure addresses including subnet mask
      "${nixosVars.ipv4_address}/32"
      "${nixosVars.ipv6_address}/64"
    ];
    routes = [
      # create default routes for both IPv6 and IPv4
      {Gateway = "fe80::1";}
      # or when the gateway is not on the same network
      {
        Gateway = "172.31.1.1";
        GatewayOnLink = true;
      }
    ];
    # make the routes on this interface a dependency for network-online.target
    linkConfig.RequiredForOnline = "routable";
  };

  sops.defaultSopsFile = ./secrets/secrets.yaml;
}
