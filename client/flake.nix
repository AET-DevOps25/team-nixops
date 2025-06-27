{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs =
    { self
    , nixpkgs
    , flake-utils
    ,
    }:
    flake-utils.lib.eachDefaultSystem (
      system:
      let
        pkgs = import nixpkgs { inherit system; };
      in
      {
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

            # shellHook = ''
            #   echo "NextJS development environment is ready."
            #   echo -e "\e[1;32mTo start the dev server, execute: npm run dev\e[0m"
            # '';
          };
        };

        packages = with pkgs; let
          pname = "client";
          drv = buildNpmPackage {
            inherit pname;
            version = "0.0.1";
            meta.mainProgram = pname;
            src = ./.;

            npmDeps = importNpmLock {
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

            npmConfigHook = importNpmLock.npmConfigHook;
          };
        in
        {
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
          default = drv;
        };
      }
    );
}
