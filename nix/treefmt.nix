{inputs, ...}: {
  imports = [
    inputs.treefmt-nix.flakeModule
  ];
  perSystem = {...}: {
    treefmt = {
      programs = {
        alejandra.enable = true;
        ktfmt.enable = true;
        terraform.enable = true;
        black.enable = true; #python
      };
    };
  };
}
