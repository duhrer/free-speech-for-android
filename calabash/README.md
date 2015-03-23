This directory contains functional tests that are run using [Calabash](http://calaba.sh/) via [Calabash for Android](https://github.com/calabash/calabash-android).  The tests are written in [Cucumber](https://cukes.info/).  Follow all installation instructions for both Calabash and Calabash for Android, and then:

1. Rebuild the source using Android Studio or the command line.
2. Start an emulator or connect a physical device.
2. Run the following command: `android-calabash run ../freespeech/build/outputs/apk/freespeech-debug-unaligned.apk`

The app should be deployed to the emulator, and the tests should run as expected