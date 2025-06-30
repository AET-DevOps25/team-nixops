{
  lib,
  self,
  config,
  inputs,
  terraform,
  ...
}: let
  cfg = config.infra;
in {
  import = [./base.nix];

  config = let
    controls = lib.attrsets.filterAttrs (name: _: null != (builtins.match "control-.*" name)) cfg.nodesConfig;
  in
    lib.mkIf (cfg.role == "loadbalancer") {
      boot.kernel.sysctl."net.ipv4.ip_nonlocal_bind" = true;
      networking.firewall = {
        extraCommands = "iptables -A INPUT -p vrrp -j ACCEPT";
        extraStopCommands = "iptables -D INPUT -p vrrp -j ACCEPT || true";
        allowedTCPPorts = [443];
      };

      services.haproxy = {
        enable = true;
        # TODO: backend healthchecks
        config = let
          backends =
            lib.attrsets.mapAttrsToList
            (name: value: "server ${name} ${value.network.ip}:6443")
            controls;
        in ''
          defaults
            timeout connect 10s

          frontend k8s
            mode tcp
            bind *:443
            default_backend controlplanes

          backend controlplanes
            mode tcp
            ${builtins.concatStringsSep "\n  " backends}
        '';
      };

      services.keepalived = {
        enable = true;
        vrrpInstances.k8s = {
          # TODO: at least basic (hardcoded) auth or other protective measures
          interface = "ens3";
          priority =
            # Prioritize loadbalancer1 over loadbalancer2 over loadbalancer3, etc.
            let
              number = lib.strings.toInt (lib.strings.removePrefix "loadbalancer" self.values.name);
            in
              200 - number;
          virtualRouterId = 42;
          virtualIps = [
            {
              addr = cfg.apiAdress;
            }
          ];
        };
      };
    };
}
