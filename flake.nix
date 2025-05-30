{
  description = "Description for the project";

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
    flake-parts = {
      url = "github:hercules-ci/flake-parts";
      inputs.nixpkgs-lib.follows = "nixpkgs";
    };
    devenv = {
      url = "github:cachix/devenv";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    disko = {
      url = "github:nix-community/disko";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    srvos = {
      url = "github:nix-community/srvos";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    sops-nix = {
      url = "github:Mic92/sops-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = inputs @ {flake-parts, ...}:
    flake-parts.lib.mkFlake {inherit inputs;} (
      {
        self,
        lib,
        ...
      }: {
        debug = true;
        imports = [
          inputs.devenv.flakeModule
          ./nix/targets/flake-module.nix
          ./nix/modules/flake-module.nix
        ];
        systems = ["x86_64-linux" "aarch64-linux" "aarch64-darwin" "x86_64-darwin"];
        perSystem = {
          pkgs,
          system,
          self',
          ...
        }: let
          genai = pkgs.callPackage ./genai {
            inherit (inputs) pyproject-nix uv2nix pyproject-build-systems;
          };
        in {
          checks = let
            nixosMachines = lib.mapAttrs' (
              name: config: lib.nameValuePair "nixos-${name}" config.config.system.build.toplevel
            ) ((lib.filterAttrs (_: config: config.pkgs.system == system)) self.nixosConfigurations);
            packages = lib.mapAttrs' (n: lib.nameValuePair "package-${n}") self'.packages;
            devShells = lib.mapAttrs' (n: lib.nameValuePair "devShell-${n}") self'.devShells;
          in
            nixosMachines // packages // devShells;

          packages = {
            donna = pkgs.callPackage server/donna {};
            genai = genai.package;
          };

          devShells.genai = genai.devShell;
          devenv.shells.default = {
            packages = with pkgs; [
              jq
              age
              sops
            ];
            languages = {
              kotlin.enable = true;
              java = {
                enable = true;
                gradle.enable = true;
                maven.enable = true;
              };
              python = {
                uv.enable = true;
              };
              opentofu.enable = true;
            };
          };
        };
      }
    );
}
