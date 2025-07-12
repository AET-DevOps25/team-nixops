{
  description = "Description for the project";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

    disko = {
      url = "github:nix-community/disko";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    devenv = {
      url = "github:cachix/devenv";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    flake-parts = {
      url = "github:hercules-ci/flake-parts";
      inputs.nixpkgs-lib.follows = "nixpkgs";
    };
    gradle2nix = {
      url = "github:tadfisher/gradle2nix/v2";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    mk-shell-bin.url = "github:rrbutani/nix-mk-shell-bin";
    nix2container = {
      url = "github:nlewo/nix2container";
      inputs = {
        nixpkgs.follows = "nixpkgs";
      };
    };
    pyproject-build-systems = {
      url = "github:pyproject-nix/build-system-pkgs";
      inputs = {
        pyproject-nix.follows = "pyproject-nix";
        uv2nix.follows = "uv2nix";
        nixpkgs.follows = "nixpkgs";
      };
    };
    pyproject-nix = {
      url = "github:pyproject-nix/pyproject.nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    sops-nix = {
      url = "github:Mic92/sops-nix";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    srvos = {
      url = "github:nix-community/srvos";
      inputs.nixpkgs.follows = "nixpkgs";
    };
    treefmt-nix.url = "github:numtide/treefmt-nix";
    uv2nix = {
      url = "github:pyproject-nix/uv2nix";
      inputs.pyproject-nix.follows = "pyproject-nix";
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
          ./nix/targets/flake-module.nix
          ./nix/modules/flake-module.nix
          ./nix/devShell
          ./nix/treefmt.nix
        ];
        systems = [
          "x86_64-linux"
          "aarch64-linux"
          "aarch64-darwin"
          "x86_64-darwin"
        ];
        perSystem = {
          pkgs,
          system,
          self',
          ...
        }: let
          scraper = pkgs.callPackage ./scraper {
            inherit (inputs) gradle2nix;
          };
          embedding-bridge = pkgs.callPackage ./embedding-bridge {
            inherit (inputs) gradle2nix;
          };
          schedule-manager = pkgs.callPackage ./schedule-manager {
            inherit (inputs) gradle2nix;
          };
        in {
          checks = let
            nixosMachines = import ./nix/checks {inherit pkgs self;};
            packages = lib.mapAttrs' (n: lib.nameValuePair "package-${n}") self'.packages;
            devShells = lib.mapAttrs' (n: lib.nameValuePair "devShell-${n}") self'.devShells;
          in
            nixosMachines // packages // devShells;

          packages = {
            schedulingEngine = pkgs.callPackage ./schedulingEngine {};
            genai = pkgs.callPackage ./genai {
              inherit (inputs) pyproject-nix uv2nix pyproject-build-systems;
            };
            client = pkgs.callPackage ./client {};
            scraper = scraper.packages.scraper;
            embedding-bridge = embedding-bridge.packages.embedding-bridge;
            schedule-manager = schedule-manager.packages.schedule-manager;
          };
        };
      }
    );
}
