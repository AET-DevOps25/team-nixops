{
  lib,
  pkgs,
  pyproject-nix,
  uv2nix,
  pyproject-build-systems,
}: let
  python = pkgs.python313;

  workspace = uv2nix.lib.workspace.loadWorkspace {workspaceRoot = ./.;};

  overlay = workspace.mkPyprojectOverlay {
    sourcePreference = "wheel";
  };

  pyprojectOverrides = final: prev: {
    psycopg2 = prev.psycopg2.overrideAttrs (old: {
      buildInputs =
        (old.buildInputs or [])
        ++ [pkgs.libpq.pg_config]
        ++ final.resolveBuildSystem {setuptools = [];};
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
    venvIgnoreCollisions = ["*"];
  };

  inherit (pkgs.callPackages pyproject-nix.build.util {}) mkApplication;

  appDrv = mkApplication {
    inherit venv;
    package = pythonSet.genai;
  };

  testDrv = pkgs.stdenv.mkDerivation {
    name = "${pythonSet.genai.pname}-tests";
    src = ./.;
    buildInputs = [venv];
    doCheck = true;
    checkPhase = ''
      echo "Running pytest..."
      ${venv}/bin/python -m pytest
    '';
    installPhase = "mkdir -p $out";
  };

  drv = pkgs.stdenv.mkDerivation {
    pname = pythonSet.genai.pname;
    version = pythonSet.genai.version;
    src = ./.;

    nativeBuildInputs = [
      venv
      testDrv
    ];

    installPhase = ''
      mkdir -p $out/bin
      cat > $out/bin/${pythonSet.genai.pname} <<EOF
      #!${pkgs.bash}/bin/bash
      exec ${venv}/bin/python -m ${pythonSet.genai.pname}.main "\$@"
      EOF
      chmod +x $out/bin/${pythonSet.genai.pname}

      cp -r ${appDrv}/* $out/ || true
    '';

    passthru = {
      inherit appDrv testDrv venv;
    };

    meta = {
      mainProgram = "${pythonSet.genai.pname}";
    };
  };

  dockerImage = pkgs.dockerTools.buildLayeredImage {
    name = "nixops-${pythonSet.genai.pname}";
    tag = "${pythonSet.genai.version}";
    config = {
      Cmd = ["${lib.getExe drv}"];
      Env = ["PATH=/bin/"];
      ExposedPorts = {
        "8000/tcp" = {};
      };
    };
    contents = [
      pkgs.coreutils
      pkgs.util-linux
      pkgs.bash
      drv
    ];
  };
in
  drv.overrideAttrs (old: {
    passthru =
      (old.passthru or {})
      // {
        inherit dockerImage venv;
      };
  })
