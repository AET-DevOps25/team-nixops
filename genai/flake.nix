{
  description = "GenAI microservice";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";

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
    { self
    , nixpkgs
    , flake-utils
    , pyproject-nix
    , uv2nix
    , pyproject-build-systems
    ,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        inherit (nixpkgs) lib;

        pkgs = nixpkgs.legacyPackages.${system};

        python = pkgs.python312;

        workspace = uv2nix.lib.workspace.loadWorkspace { workspaceRoot = ./.; };

        # Create package overlay from workspace.
        overlay = workspace.mkPyprojectOverlay {
          sourcePreference = "wheel";
        };

        pythonSet =
          (pkgs.callPackage pyproject-nix.build.packages {
            inherit python;
          }).overrideScope
            (
              lib.composeManyExtensions [
                pyproject-build-systems.overlays.default
                overlay
              ]
            );
        venv = (pythonSet.mkVirtualEnv "genai-dev-env" workspace.deps.all).overrideAttrs {
          venvIgnoreCollisions = [
            # quick and dirty hack, will probably lead to some weird errors in the future
            # fixes collision between fastapi-cli and the fastapi python module
            "*"
          ];
        };

        inherit (pkgs.callPackages pyproject-nix.build.util { }) mkApplication;
        package = mkApplication {
          inherit venv;
          package = pythonSet.genai;
        };
      in
      {
        # package doesn't really work (empty derivation), since server is executed by e.g. uvicorn
        packages = {
          genai = package;
          default = package;
        };
        devShells.default = pkgs.mkShell {
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
}
