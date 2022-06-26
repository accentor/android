{
  description = "Accentor android app";

  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    devshell = {
      url = "github:numtide/devshell";
      inputs = {
        flake-utils.follows = "flake-utils";
        nixpkgs.follows = "nixpkgs";
      };
    };
    flake-utils.url = "github:numtide/flake-utils";
  };
  outputs = { self, nixpkgs, devshell, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs { inherit system; config = { android_sdk.accept_license = true; allowUnfree = true; }; overlays = [ devshell.overlay ]; };
        buildToolsVersion = "32.0.0";
        composed = pkgs.androidenv.composeAndroidPackages {
          toolsVersion = "26.1.1";
          platformToolsVersion = "33.0.1";
          buildToolsVersions = [ buildToolsVersion ];
          platformVersions = [ "32" ];
        };
        fhsEnv = pkgs.buildFHSUserEnv {
          name = "android-sdk-env";
          targetPkgs = pkgs: (with pkgs; [ glibc ]);
          profile = ''
            export ANDROID_SDK_ROOT="${composed.androidsdk}/libexec/android-sdk/"
          '';
        };
      in
      {
        devShells = rec {
          default = accentor-android;
          accentor-android = pkgs.devshell.mkShell {
            name = "Accentor Android";
            packages = [ pkgs.jdk11 pkgs.kotlin-language-server pkgs.nixpkgs-fmt ];
            env = [
              { name = "ANDROID_SDK_ROOT"; eval = "${composed.androidsdk}/libexec/android-sdk/"; }
              { name = "BUILD_TOOLS_PATH"; eval = "$ANDROID_SDK_ROOT/build-tools/${buildToolsVersion}"; }
              { name = "APK_DIR"; eval = "$PRJ_ROOT/app/build/outputs/apk/release"; }
            ];
            commands = [
              {
                name = "gradle";
                category = "tools";
                help = "Working gradle invocation";
                command = "${fhsEnv}/bin/android-sdk-env \"$PRJ_ROOT/gradlew\" $@";
              }
              {
                name = "sign-release";
                category = "tools";
                help = "Build a signed APK";
                command = ''
                  rm -f "$APK_DIR/"*
                  gradle assembleRelease
                  "$BUILD_TOOLS_PATH/zipalign" -v -p 4 "$APK_DIR/app-release-unsigned.apk" "$APK_DIR/app-release-unsigned-aligned.apk"

                  "$BUILD_TOOLS_PATH/apksigner" sign --ks "$PRJ_ROOT/keystore.jks" --out "$APK_DIR/app-release.apk" "$APK_DIR/app-release-unsigned-aligned.apk"
                  "$BUILD_TOOLS_PATH/apksigner" verify "$APK_DIR/app-release.apk"
                '';
              }
            ];
          };
        };
      }
    );
}
