{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }:
    flake-utils.lib.eachDefaultSystem (
      system: let
        pkgs = import nixpkgs {inherit system;};
      in {
        devShells = with pkgs; {
          default = mkShell {
            buildInputs = [
              nodejs
              importNpmLock.hooks.linkNodeModulesHook
            ];

            npmDeps = importNpmLock.buildNodeModules {
              npmRoot = ./.;
              inherit nodejs;
            };

            shellHook = ''
              echo "NextJS development environment is ready."
              echo -e "\e[1;32mTo start the dev server, execute: npm run dev\e[0m"
            '';
          };
        };

        packages = with pkgs; {
          default = buildNpmPackage {
            pname = "nixops-client";
            version = "0.0.1";
            src = ./.;

            npmDeps = importNpmLock {
              npmRoot = ./.;
            };

            npmConfigHook = importNpmLock.npmConfigHook;
          };
        };
      }
    );
}
