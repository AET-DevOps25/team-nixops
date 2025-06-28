{ lib
, pkgs
,
}:
let
  npmDeps = pkgs.importNpmLock.buildNodeModules {
    npmRoot = ./.;
    nodejs = pkgs.nodejs;
  };

  pname = "client";
  drv = pkgs.buildNpmPackage {
    inherit pname;
    version = "0.0.1";
    meta.mainProgram = pname;
    src = ./.;

    npmDeps = pkgs.importNpmLock {
      npmRoot = ./.;
    };

    postInstall = ''
      mkdir -p $out/bin
      exe="$out/bin/${pname}"
      lib="$out/lib/node_modules/${pname}/.next"
      cp -r ./.next $lib
      touch $exe
      chmod +x $exe
      echo "
          #!/usr/bin/env bash
          cd $lib/..
          ${pkgs.nodePackages_latest.pnpm}/bin/pnpm run start" > $exe
    '';

    npmConfigHook = pkgs.importNpmLock.npmConfigHook;
  };

  dockerImage =
    pkgs.dockerTools.buildLayeredImage
      {
        name = "client";
        tag = "latest";
        config = {
          Cmd = [
            "${lib.getExe pkgs.bash}"
            "-c"
            "${lib.getExe drv}"
          ];
          Env = [
            "PATH=/bin/"
          ];
          ExposedPorts = {
            "3000/tcp" = { };
          };
        };
        contents = [ pkgs.coreutils pkgs.util-linux pkgs.bash drv ];
      };
in
lib.extendDerivation true
{
  inherit dockerImage npmDeps;
}
  drv
