{
  lib,
  pkgs,
  pyproject-nix,
  uv2nix,
  pyproject-build-systems,
}: let
  python = pkgs.python313;

  workspace = uv2nix.lib.workspace.loadWorkspace {workspaceRoot = ./.;};

  # Create package overlay from workspace.
  overlay = workspace.mkPyprojectOverlay {
    sourcePreference = "wheel";
  };

  pyprojectOverrides = final: prev: {
    psycopg2 = prev.psycopg2.overrideAttrs (old: {
      buildInputs = (old.buildInputs or []) ++ [pkgs.libpq] ++ final.resolveBuildSystem {setuptools = [];};
    });
  };

  pythonSet = (pkgs.callPackage pyproject-nix.build.packages {inherit python;}).overrideScope (
    lib.composeManyExtensions [
      pyproject-build-systems.overlays.default
      overlay
      pyprojectOverrides
    ]
  );
  venv = (pythonSet.mkVirtualEnv "genai-dev-env" workspace.deps.default).overrideAttrs {
    venvIgnoreCollisions = [
      # quick and dirty hack, will probably lead to some weird errors in the future
      # fixes collision between fastapi-cli and the fastapi python module
      "*"
    ];
  };

  inherit (pkgs.callPackages pyproject-nix.build.util {}) mkApplication;
  drv = mkApplication {
    inherit venv;
    package = pythonSet.genai;
  };

  dockerImage =
    pkgs.dockerTools.buildLayeredImage
    {
      name = "nixops-genai";
      tag = "${pythonSet.genai.version}";
      config = {
        Cmd = [
          "${lib.getExe drv}"
        ];
        Env = [
          "PATH=/bin/"
        ];
        ExposedPorts = {
          "8000/tcp" = {};
        };
      };
      contents = [pkgs.coreutils pkgs.util-linux pkgs.bash drv];
    };
in
  lib.extendDerivation true
  {
    inherit dockerImage venv;
  }
  drv
