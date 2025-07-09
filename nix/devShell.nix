{inputs, ...}: {
  imports = [
    inputs.devenv.flakeModule
  ];
  perSystem = {
    pkgs,
    self',
    config,
    ...
  }: {
    devenv.shells = rec {
      default = {
        packages = with pkgs; [
          jq
          age
          sops
          kubectl
          kubernetes-helm
        ];
        cachix = {
          enable = true;
          pull = ["pre-commit-hooks" "nix-community"];
          push = "team-nixops";
        };
        git-hooks.hooks.treefmt = {
          enable = true;
          packageOverrides.treefmt = config.treefmt.build.wrapper;
        };
      };
      ops = {
        imports = [default];
        languages = {
          opentofu.enable = true;
        };
      };
      kotlin-dev = {
        imports = [default];
        languages = {
          kotlin.enable = true;
          java = {
            enable = true;
            gradle.enable = true;
            maven.enable = true;
          };
        };
      };
      client-dev = let
        inherit (self'.packages.client) npmDeps;
      in {
        packages = [
          pkgs.nodejs
          pkgs.importNpmLock.hooks.linkNodeModulesHook
          pkgs.openapi-generator-cli
        ];
        env = {
          inherit npmDeps;
        };
        enterShell = ''
          # normally executed automatically, but not if other shell hook already exists
          linkNodeModulesHook
        '';
      };
      genai-dev = let
        inherit (self'.packages.genai) venv;
      in {
        packages = [
          venv
          pkgs.uv
          pkgs.openapi-generator-cli
        ];
        env = {
          UV_NO_SYNC = "1";
          UV_PYTHON = "${venv}/bin/python";
          UV_PYTHON_DOWNLOADS = "never";
          PYTHONPATH = "${venv}/bin/python";
        };

        enterShell = ''
          export REPO_ROOT=$(git rev-parse --show-toplevel)
        '';
      };
    };
  };
}
