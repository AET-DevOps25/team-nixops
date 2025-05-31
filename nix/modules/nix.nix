{
  config,
  pkgs,
  ...
}: {
  # we need git for flakes
  environment.systemPackages = [pkgs.git];

  nix = {
    settings = {
      auto-optimise-store = true;
      sandbox = config.security.allowUserNamespaces;
      substituters = [
        # high priority since it's almost always used
        "https://cache.nixos.org?priority=10"
        "team-nixops.cachix.org"
        "https://mikilio.cachix.org"
        "https://nix-community.cachix.org"
      ];

      trusted-public-keys = [
        "cache.nixos.org-1:6NCHdD59X431o0gWypbMrAURkbJ16ZPMQFGspcDShjY="
        "team-nixops.cachix.org-1:Ph60J0kTI7bYJBCIK/5bGPPbMYKlAItI4GnKswjF4Ls="
        "mikilio.cachix.org-1:nYplhDMbu04QkMOJlCfSsEuFYFHp9VMKbChfL2nMKio="
        "nix-community.cachix.org-1:mB9FSh9qf2dCimDSUo8Zy7bkq5CX+/rkCWyvRCYg3Fs="
      ];
    };
  };
}
