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
        scripts.generate-sops = {
          exec =
            #python
            ''
              import os
              import yaml
              from jinja2 import Environment, FileSystemLoader

              # Read DEVENV_ROOT environment variable
              dev_env_root = os.getenv("DEVENV_ROOT")
              if not dev_env_root:
                  raise EnvironmentError("DEVENV_ROOT environment variable is not set")

              # Construct paths
              keys_yaml_path = os.path.join(dev_env_root, "devShell", "keys.yaml")
              template_path = os.path.join(dev_env_root, "devShell")
              output_path = os.path.join(dev_env_root, ".sops.yaml")

              # Load keys.yaml
              with open(keys_yaml_path) as f:
                  keys = yaml.safe_load(f)["keys"]

              # Setup Jinja2 environment and load template
              env = Environment(loader=FileSystemLoader(template_path))
              template = env.get_template("sops.yaml.j2")

              # Render
              output = template.render(keys=keys)

              # Write output file
              with open(output_path, "w") as f:
                  f.write(output)

              print(f"Generated {output_path}")

            '';
          package = pkgs.python3.withPackages (p: with p; [jinja2 pyyaml]);
          description = "Generate .sops.yaml";
        };
        tasks = {
          "setup:sops" = {
            exec = "generate-sops";
            before = ["devenv:enterShell"];
          };
          languages = {
            opentofu.enable = true;
          };
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
