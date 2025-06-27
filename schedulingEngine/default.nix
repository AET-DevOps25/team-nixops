{
  lib,
  stdenv,
  makeBinaryWrapper,
  gradle,
  jre,
  jdk21,
}: let
  self = stdenv.mkDerivation rec {
    pname = "schedulingEngine";
    version = "0.0.1-SNAPSHOT";

    src = ./.;

    nativeBuildInputs = [
      makeBinaryWrapper
      jdk21
      gradle
    ];

    # if the package has dependencies, mitmCache must be set
    mitmCache = gradle.fetchDeps {
      pkg = self;
      data = ./deps.json;
    };

    # this is required for using mitm-cache on Darwin
    __darwinAllowLocalNetworking = true;

    gradleFlags = ["-Dfile.encoding=utf-8"];

    # defaults to "assemble"
    gradleBuildTask = "bootJar";

    # will run the gradleCheckTask (defaults to "test")
    doCheck = true;

    installPhase = ''
      mkdir -p $out/{bin,share/${pname}}
      cp build/libs/${pname}-${version}.jar $out/share/${pname}

      makeWrapper ${lib.getExe jre} $out/bin/${pname} \
        --add-flags "-jar $out/share/${pname}/${pname}-${version}.jar"
    '';

    meta.sourceProvenance = with lib.sourceTypes; [
      fromSource
      binaryBytecode # mitm cache
    ];
  };
in
  self
