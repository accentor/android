{
  description = "Accentor android app";

  inputs = {
    nixpkgs.url = "github:numinit/nixpkgs/update-androidenv";
    flake-utils.url = "github:numtide/flake-utils/master";
  };
  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; config.android_sdk.accept_license = true; };
        buildToolsVersion = "30.0.3";
        composed = pkgs.androidenv.composeAndroidPackages {
          toolsVersion = "26.1.1";
          platformToolsVersion = "30.0.5";
          buildToolsVersions = [ buildToolsVersion ];
          platformVersions = [ "30" ];
          includeSources = true;
        };
        fhsEnv = pkgs.buildFHSUserEnv {
          name = "android-sdk-env";
          targetPkgs = pkgs: (with pkgs; [ glibc ]);
          profile = ''
            export ANDROID_SDK_ROOT="${composed.androidsdk}/libexec/android-sdk/"
          '';
        };
        gradle-run-script = pkgs.writeShellScriptBin "gradle" ''
          ${fhsEnv}/bin/android-sdk-env "$REPO_ROOT/gradlew" $@
        '';
      in
      {
        devShell = pkgs.mkShell rec {
          ANDROID_SDK_ROOT = "${composed.androidsdk}/libexec/android-sdk/";
          BUILD_TOOLS_PATH = "${composed.androidsdk}/libexec/android-sdk/build-tools/${buildToolsVersion}";
          buildInputs = with pkgs;
            [
              gradle-run-script
              (writeShellScriptBin "sign-release" ''
                rm "$APK_DIR/"*
                ${gradle-run-script}/bin/gradle assembleRelease
                "$BUILD_TOOLS_PATH/zipalign" -v -p 4 "$APK_DIR/app-release-unsigned.apk" "$APK_DIR/app-release-unsigned-aligned.apk"

                "$BUILD_TOOLS_PATH/apksigner" sign --ks "$REPO_ROOT/keystore.jks" --out "$APK_DIR/app-release.apk" "$APK_DIR/app-release-unsigned-aligned.apk"
                "$BUILD_TOOLS_PATH/apksigner" verify "$APK_DIR/app-release.apk"
              '')
              jdk11
            ];
          shellHook = ''
            export REPO_ROOT="$(git rev-parse --show-toplevel)"
            export APK_DIR="$REPO_ROOT/app/build/outputs/apk/release"
          '';
        };
      }
    );
}
