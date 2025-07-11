{
  lib,
  pkgs,
  makeBinaryWrapper,
  jre,
  gradle2nix,
}: let
  pname = "embedder-bridge";
  version = "0.0.1";

  embedder-bridge = gradle2nix.builders.x86_64-linux.buildGradlePackage {
    inherit pname version;

    lockFile = ./gradle.lock;

    gradleBuildFlags = [
      "test"
      "bootJar"
    ];

    src = ./.;

    nativeBuildInputs = [makeBinaryWrapper];

    installPhase = ''
      mkdir -p $out/{bin,share/${pname}}
      cp build/libs/${pname}-${version}.jar $out/share/${pname}

      makeWrapper ${lib.getExe jre} $out/bin/${pname} \
        --add-flags "-jar $out/share/${pname}/${pname}-${version}.jar"
    '';

    passthru = {
      dockerImage = pkgs.dockerTools.buildImage {
        name = "nixops-${pname}";
        tag = version;

        copyToRoot = pkgs.buildEnv {
          name = "image-root";
          paths = [
            embedder-bridge
            (pkgs.runCommand "empty-tmp" {} ''
              mkdir -p $out/tmp
              chmod 1777 $out/tmp
            '')
          ];
          pathsToLink = ["/"];
        };

        config = {
          Cmd = ["/bin/${pname}"];
          WorkingDir = "/";
        };
      };
    };
  };
in {
  packages = {
    inherit embedder-bridge;
  };
}
