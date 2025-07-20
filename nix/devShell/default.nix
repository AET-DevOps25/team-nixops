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
          docker-compose
          compose2nix
        ];
        cachix = {
          enable = true;
          pull = [
            "pre-commit-hooks"
            "nix-community"
          ];
          push = "team-nixops";
        };
        git-hooks.hooks = {
          treefmt = {
            enable = true;
            packageOverrides.treefmt = config.treefmt.build.wrapper;
          };
          openapi-spec-validator.enable = true;
        };
        scripts. generate-sops = {
          exec = builtins.readFile ./scripts/generate-sops.py;
          package = pkgs.python3.withPackages (
            p:
              with p; [
                jinja2
                pyyaml
              ]
          );
          description = "Generate .sops.yaml";
        };
        tasks = {
          "setup:sops" = {
            exec = "generate-sops";
            before = ["devenv:enterShell"];
          };
        };
      };
      ops = {
        imports = [default];
        scripts = {
          provision-certificates = {
            exec = builtins.readFile ./scripts/provision-certificates.py;
            package = pkgs.python3.withPackages (
              p:
                with p; [
                  cryptography
                  ruamel-yaml
                ]
            );
            description = "Generate certificates for kubernetes cluster";
          };
        };
        languages = {
          opentofu.enable = true;
        };
        packages = with pkgs; [awscli2];
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
          pkgs.pdm
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
      helm-dev = {
        imports = [default];

        packages = with pkgs; [
          jq
          age
          sops
          kubectl
          kubernetes-helm
        ];
      };
    };
  };
}
