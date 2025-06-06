{
  lib,
  pkgs,
  pyproject-nix,
  uv2nix,
  pyproject-build-systems,
}: let
  python = pkgs.python312;

  workspace = uv2nix.lib.workspace.loadWorkspace {workspaceRoot = ./.;};

  # Create package overlay from workspace.
  overlay = workspace.mkPyprojectOverlay {
    sourcePreference = "wheel";
  };

  pythonSet =
    (pkgs.callPackage pyproject-nix.build.packages {
      inherit python;
    })
    .overrideScope
    (
      lib.composeManyExtensions [
        pyproject-build-systems.overlays.default
        overlay
      ]
    );
  venv = (pythonSet.mkVirtualEnv "genai-dev-env" workspace.deps.default).overrideAttrs {
    venvIgnoreCollisions = [
      # quick and dirty hack, will probably lead to some weird errors in the future
      # fixes collision between fastapi-cli and the fastapi python module
      "*"
    ];
  };

  app = pkgs.lib.fileset.toSource {
    root = ./.;
    fileset = ./.;
  };

  dockerImage =
    pkgs.dockerTools.buildLayeredImage
    {
      name = "genai";
      tag = "latest";
      config = {
        Cmd = [
          "${venv}/bin/fastapi"
          "run"
          "--port"
          "80"
          "src"
        ];
        WorkingDir = app;
        Env = [
          "PATH=/bin/"
        ];
        ExposedPorts = {
          "80/tcp" = {};
        };
      };
      contents = [venv pkgs.coreutils pkgs.util-linux pkgs.bash];
    };
in {
  # package doesn't really work (empty derivation), since server is executed by e.g. uvicorn
  package = dockerImage;
  devShell = pkgs.mkShell {
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
