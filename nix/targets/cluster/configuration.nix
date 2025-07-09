{
  self,
  lib,
  terraform,
  ...
}: let
  nixosVars = builtins.fromJSON (builtins.readFile ./nixos-vars-${terraform.name}.json);
in {
  imports = [
    self.nixosModules.hcloud
    self.nixosModules.k8s
  ];
  users.users.root.openssh.authorizedKeys.keys = nixosVars.ssh_keys;
  system.stateVersion = "25.05";
  security.acme.defaults.email = "not.a.real.mail@nixops.aet.cit.tum.de";

  networking = {
    hostName = terraform.name;
    domain = "nixops.aet.cit.tum.de";
  };

  infra = {
    role = terraform.role;
    clusterConfigDir = ./nixos-vars;
    apiAdress = "10.0.0.254";
  };

  services.cloud-init.enable = lib.mkForce false;

  systemd.network.networks."10-wan" = {
    # match the interface by name
    # matchConfig.MACAddress = "00:00:00:00:00:00";
    matchConfig.Name = "enp1s0";
    address =
      [
        "${nixosVars.network.ip}/24"
        "${nixosVars.ipv6_address}/64"
      ]
      ++ (lib.optional (nixosVars ? ipv4_address) ["${nixosVars.ipv4_address}/32"]);
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

  sops.defaultSopsFile = ./secrets/${terraform.name}.yaml;
}
