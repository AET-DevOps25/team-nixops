{
  lib,
  pkgs,
  makeBinaryWrapper,
  jre,
  gradle2nix,
}:

let

  pname = "scraper";
  version = "0.0.1";

  scraper = gradle2nix.builders.x86_64-linux.buildGradlePackage {
    inherit pname version;

    lockFile = ./gradle.lock;

    gradleBuildFlags = [ "bootJar" ];

    src = ./.;

    nativeBuildInputs = [ makeBinaryWrapper ];

    installPhase = ''
      mkdir -p $out/{bin,share/${pname}}
      cp build/libs/${pname}-${version}.jar $out/share/${pname}

      makeWrapper ${lib.getExe jre} $out/bin/${pname} \
        --add-flags "-jar $out/share/${pname}/${pname}-${version}.jar"
    '';

    passthru = {
      dockerImage = pkgs.dockerTools.buildImage {
        name = "ghcr.io/aet-devops25/team-nixops-scraper";
        tag = "latest";

        copyToRoot = pkgs.buildEnv {
          name = "image-root";
          paths = [
            (pkgs.runCommand "empty-tmp" { } ''
              mkdir -p $out/tmp
              chmod 1777 $out/tmp
            '')
          ];
          pathsToLink = [ "/" ];
        };

        config = {
          Cmd = [
            "${placeholder "out"}/bin/${pname}"
          ];
          WorkingDir = "/";
        };
      };
    };
  };

in
{
  packages = {
    inherit scraper;
  };
}
