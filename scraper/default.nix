{
  lib,
  stdenv,
  makeBinaryWrapper,
  gradle,
  jre,
  jdk21,
  gradle2nix,
}:
let

  pname = "scraper";
  version = "0.0.1";

  self = gradle2nix.builders.x86_64-linux.buildGradlePackage {
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
  };
in
self
