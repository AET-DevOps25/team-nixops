{
  lib,
  config,
  ...
}: let
  cfg = config.infra;
in {
  imports = [
    ./base.nix
  ];

  config = let
    getIP = vars: (builtins.head vars.network).ip;

    etcds = lib.attrsets.filterAttrs (name: _: null != (builtins.match "etcd-.*" name)) cfg.nodeConfigs;

    thisNode = cfg.nodeConfigs.${config.networking.hostName};
  in
    lib.mkIf (cfg.role == "etcd") {
      sops.secrets = {
        server = {};
        server-key = {};
        peer = {};
        peer-key = {};
        etcd-ca = {};
      };

      services.etcd = {
        enable = true;
        openFirewall = true;

        initialCluster = lib.mkForce (
          lib.attrsets.mapAttrsToList
          (name: value: "${name}=${getIP value}")
          etcds
        );
        listenClientUrls = ["https://${getIP thisNode}:2379" "https://127.0.0.1:2379"];
        listenPeerUrls = ["https://${getIP thisNode}:2380" "https://127.0.0.1:2380"];

        clientCertAuth = true;
        peerClientCertAuth = true;

        certFile = config.sops.secrets.server.path;
        keyFile = config.sops.secrets.server-key.path;

        peerCertFile = config.sops.secrets.peer.path;
        peerKeyFile = config.sops.secrets.peer-key.path;

        peerTrustedCaFile = config.sops.secrets.etcd-ca.path;
        trustedCaFile = config.sops.secrets.etcd-ca.path;
      };

      systemd.services.etcd = {
        wants = ["network-online.target"];
        after = ["network-online.target"];
      };
    };
}
