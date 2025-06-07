{
  lib,
  pkgs,
  makeBinaryWrapper,
  jre,
  gradle2nix,
<<<<<<< HEAD
}: let
=======
}:

let

>>>>>>> e64354e (add dockerImage to scraper)
  pname = "scraper";
  version = "0.0.1";

  scraper = gradle2nix.builders.x86_64-linux.buildGradlePackage {
    inherit pname version;

    lockFile = ./gradle.lock;

    gradleBuildFlags = ["bootJar"];

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
        name = "ghcr.io/aet-devops25/team-nixops-scraper";
        tag = version;

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
<<<<<<< HEAD
  self
=======
{
  packages = {
    inherit scraper;
  };
}
>>>>>>> e64354e (add dockerImage to scraper)
