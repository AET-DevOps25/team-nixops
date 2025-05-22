{
  description = "GenAI microservice";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

    pyproject-nix = {
      url = "github:pyproject-nix/pyproject.nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    uv2nix = {
      url = "github:pyproject-nix/uv2nix";
      inputs.pyproject-nix.follows = "pyproject-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    pyproject-build-systems = {
      url = "github:pyproject-nix/build-system-pkgs";
      inputs = {
        pyproject-nix.follows = "pyproject-nix";
        uv2nix.follows = "uv2nix";
        nixpkgs.follows = "nixpkgs";
      };
    };
  };

  outputs =
    { nixpkgs
    , uv2nix
    , pyproject-nix
    , pyproject-build-systems
    , ...
    }:
    let
      inherit (nixpkgs) lib;
      forAllSystems = lib.genAttrs lib.systems.flakeExposed;

      workspace = uv2nix.lib.workspace.loadWorkspace { workspaceRoot = ./.; };

      overlay = workspace.mkPyprojectOverlay {
        sourcePreference = "wheel";
      };

      # Python sets grouped per system
      pythonSets = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};

          # Base Python package set from pyproject.nix
          baseSet = pkgs.callPackage pyproject-nix.build.packages {
            python = pkgs.python312;
          };
        in
        baseSet.overrideScope (
          lib.composeManyExtensions [
            pyproject-build-systems.overlays.default
            overlay
          ]
        )
      );
    in
    {
      devShells = forAllSystems (
        system:
        let
          pkgs = nixpkgs.legacyPackages.${system};

          pythonSet = pythonSets.${system};

          venv = (pythonSet.mkVirtualEnv "genai-dev-env" workspace.deps.all).overrideAttrs {
            venvIgnoreCollisions = [
              # quick and dirty hack, will probably lead to some weird errors in the future
              # fixes collision between fastapi-cli and the fastapi python module
              "*"
            ];
          };
        in
        {
          default = pkgs.mkShell {
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

            shellHook = ''
              export REPO_ROOT=$(git rev-parse --show-toplevel)
            '';
          };
        }
      );
    };
}
