{
  description = "GenAI microservice";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixos-unstable";

    pyproject-nix = {
      url = "github:pyproject-nix/pyproject.nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    uv2nix = {
      url = "github:pyproject-nix/uv2nix";
      inputs.pyproject-nix.follows = "pyproject-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = inputs:
    let
      supportedSystems = [ "x86_64-linux" "aarch64-linux" "x86_64-darwin" "aarch64-darwin" ];
      forEachSupportedSystem = f:
        inputs.nixpkgs.lib.genAttrs supportedSystems (system:
          f {
            pkgs = import inputs.nixpkgs { inherit system; };
          });
      project = inputs.pyproject-nix.lib.project.loadPyproject {
        projectRoot = ./.;
      };

      /*
      * Change this value ({major}.{min}) to
      * update the Python virtual-environment
      * version. When you do this, make sure
      * to delete the `.venv` directory to
      * have the hook rebuild it for the new
      * version, since it won't overwrite an
      * existing one. After this, reload the
      * development shell to rebuild it.
      * You'll see a warning asking you to
      * do this when version mismatches are
      * present. For safety, removal should
      * be a manual step, even if trivial.
      */
      version = "3.12";
    in
    {
      devShells = forEachSupportedSystem ({ pkgs }:
        let
          concatMajorMinor = v:
            pkgs.lib.pipe v [
              pkgs.lib.versions.splitVersion
              (pkgs.lib.sublist 0 2)
              pkgs.lib.concatStrings
            ];

          python = pkgs."python${concatMajorMinor version}";
          arg = project.renderers.withPackages { inherit python; };
          pythonEnv = python.withPackages arg;
        in
        {
          default = pkgs.mkShell {
            venvDir = ".venv";

            postShellHook = ''
              venvVersionWarn() {
              	local venvVersion
              	venvVersion="$("$venvDir/bin/python" -c 'import platform; print(platform.python_version())')"

              	[[ "$venvVersion" == "${python.version}" ]] && return

              	cat <<EOF
              Warning: Python version mismatch: [$venvVersion (venv)] != [${python.version}]
                       Delete '$venvDir' and reload to rebuild for version ${python.version}
              EOF
              }

              venvVersionWarn
            '';

            packages = [
              python.pkgs.venvShellHook
              pkgs.pdm # python package manager
              pkgs.uv
              pkgs.openapi-generator-cli # generate endpoints using openapi spec
              pythonEnv # all python dependencies
            ];
          };
        });
    };
}
