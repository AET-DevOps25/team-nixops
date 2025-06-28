{ lib
, pkgs
,
}:
let
  npmDeps = pkgs.importNpmLock.buildNodeModules {
    npmRoot = ./.;
    inherit (pkgs) nodejs;
  };

  package = lib.importJSON ./package.json;
  pname = package.name;
  inherit (package) version;

  drv = pkgs.buildNpmPackage {
    inherit pname;
    inherit version;

    meta.mainProgram = package.name;
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

    inherit (pkgs.importNpmLock) npmConfigHook;
  };

  dockerImage =
    pkgs.dockerTools.buildLayeredImage
      {
        name = "nixops-${pname}";
        tag = version;
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
