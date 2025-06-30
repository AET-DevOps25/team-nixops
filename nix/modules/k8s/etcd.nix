{
  lib,
  config,
  inputs,
  terraform,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    inputs.sops-nix.nixosModules.sops
    ./base.nix
  ];

  config = let
    etcds = lib.attrsets.filterAttrs (name: _: null != (builtins.match "etcd-.*" name)) cfg.nodesConfig;

    thisNode = cfg.nodesConfig.${terraform.name};
  in
    lib.mkIf (cfg.role == "etcd") {
      networking.firewall = {
        allowedTCPPorts = [2379 2380];
      };

      sops.secrets = {
        server = {};
        server-key = {};
        peer = {};
        peer-key = {};
      };

      services.etcd = {
        enable = true;

        initialcluster = lib.mkforce (
          lib.attrsets.mapAttrsToList
          (name: value: "${name}=${value.network.ip}")
          etcds
        );
        listenClientUrls = ["https://${thisNode.network.ip}:2379" "https://127.0.0.1:2379"];
        listenPeerUrls = ["https://${thisNode.network.ip}:2380" "https://127.0.0.1:2380"];

        clientcertauth = true;
        peerclientcertauth = true;

        certfile = config.sops.secrets.server.path;
        keyfile = config.sops.secrets.server-key.path;

        peercertfile = config.sops.secrets.peer.path;
        peerkeyfile = config.sops.secrets.peer-key.path;

        peertrustedcafile = config.sops.secrets.ca.path;
        trustedcafile = config.sops.secrets.ca.path;
      };

      systemd.services.etcd = {
        wants = ["network-online.target"];
        after = ["network-online.target"];
      };
    };
}
