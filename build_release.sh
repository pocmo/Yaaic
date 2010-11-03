KEYSTORE=
BUILD_DIRECTORY=builds/ant
RELEASE_KEY=
ANDROID_SDK=

if [ -z $1 ]; then
  echo "Release version is missing."
  echo "Usage: ./build_release.sh <version>"
  exit 1
fi

echo "Building yaaic $1"
echo ""
ant release
jarsigner -verbose -keystore $KEYSTORE "$BUILD_DIRECTORY/Yaaic-unsigned.apk" release
jarsigner -verify "$BUILD_DIRECTORY/Yaaic-unsigned.apk"
"$ANDROID_SDK/tools/zipalign" -v 4 "$BUILD_DIRECTORY/Yaaic-unsigned.apk" "$BUILD_DIRECTORY/yaaic-$1.apk"
echo ""
echo "Build ready: $BUILD_DIRECTORY/yaaic-$1.apk"
