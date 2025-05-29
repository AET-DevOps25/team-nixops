{
  description = "Description for the project";

  inputs = {
    flake-parts.url = "github:hercules-ci/flake-parts";
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    devenv.url = "github:cachix/devenv";

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

  outputs = inputs @ { flake-parts, ... }:
    flake-parts.lib.mkFlake { inherit inputs; } {
      imports = [
        inputs.devenv.flakeModule
      ];
      systems = [ "x86_64-linux" "aarch64-linux" "aarch64-darwin" "x86_64-darwin" ];
      perSystem =
        { pkgs
        , pyproject-nix
        , uv2nix
        , pyproject-build-systems
        , ...
        }:
        let
          genai = pkgs.callPackage ./genai {
            inherit (inputs) pyproject-nix uv2nix pyproject-build-systems;
          };
        in
        {
          packages.donna = pkgs.callPackage server/donna { };
          devShells.genai = genai.devShell;
          packages.genai = genai.package;
          devenv.shells.default = {
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
            };
          };
        };
    };
}
