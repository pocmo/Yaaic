###
# Run all unit and scenario tests
#
# TODO:
# - Build new version of Yaaic and Tests
# - Start device with -wipe-data
# - Deploy both APKs to device
#

if [ ! -f build.conf ]; then
  echo "Config file missing: build.conf"
  echo "Modify the build.conf.sample file and save it as build.conf"
  exit 1
fi

. build.conf

echo "Starting emulator"
"$ANDROID_SDK"/tools/emulator -avd "$AVD_TEST" &

sleep 40

echo "Unlocking emulator"
echo "event send EV_KEY:KEY_MENU:1 EV_KEY:KEY_MENU:0" | telnet localhost 5554

sleep 5

echo "Running tests"
"$ANDROID_SDK"/platform-tools/adb -e shell "am instrument -w org.yaaic.test/android.test.InstrumentationTestRunner"

sleep 3

echo "Killing emulator"
kill $(jobs -p)

