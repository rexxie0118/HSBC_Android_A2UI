# Android A2UI Application Build & Emulator Skill

## Environment Setup

First, ensure Android SDK environment is configured:
```
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/build-tools/34.0.0
```

## Project Build Status

✅ **Built Successfully**: The A2UI application was successfully compiled.

Built APK location: `app/build/outputs/apk/debug/app-debug.apk` (19.4MB)

## Prerequisites for Emulator

Before running on an emulator, install required packages:
```
# Install command line tools if not present
mkdir -p $ANDROID_HOME/cmdline-tools/latest
# On macOS: Download Android SDK command line tools and extract to cmdline-tools/latest

# Install required SDK components
sdkmanager "platform-tools" "emulator" "platforms;android-34" "system-images;android-34;google_apis;x86_64" "build-tools;34.0.0"
```

## Create Android Virtual Device

Create an appropriate AVD for this application (requires minimum minSdk = 26, optimal would be API 34):
```
avdmanager create avd -n A2UI_API34 -k "system-images;android-34;google_apis;x86_64" -d pixel_4
```

## Available AVDs

List available AVDs:
```
avdmanager list avd
```

## Start Emulator

1. Start emulator with specific AVD:
```
emulator -avd A2UI_API34
# Or with additional options:
emulator -avd A2UI_API34 -memory 4096 -cores 4 -skin 1080x1920
```

2. Wait for device to fully boot:
```
adb wait-for-device
adb shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done; input keyevent 82'
```

Or in second terminal, after starting emulator:
```
adb wait-for-device shell 'while [ "$(adb shell getprop sys.boot_completed | tr -d '\r')" != "1" ]; do echo "Waiting for boot completion..."; sleep 2; done; echo "Boot completed"'
```

## Alternative: Use Android Studio

If command-line tools are difficult to configure, use Android Studio:
1. Open Android Studio
2. Go to Tools → AVD Manager
3. Create Virtual Device (select Pixel 4 or newer, API 34)
4. Once configured, start emulator from AVD Manager

## Current Build and Installation Commands

Application has already been built successfully. To deploy:

1. Once an emulator/device is connected, install and run:
```
./gradlew installDebug
```

Or manually install:
```
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

Then launch:
```
adb shell am start -n com.a2ui.renderer/.MainActivity
```

## Verify Running

Check that the app is on the emulator:
```
adb devices
adb shell pm list packages | grep a2ui
```

## Quick Test Commands

After running:
```
# Check logs for A2UI-related info
adb logcat | grep -E "System.out|A2UI|com.a2ui"

# Take screenshot
adb exec-out screencap -p > a2ui_emulator_screenshot.png

# Check app processes
adb shell ps | grep a2ui

# Pull logs of just the app
adb logcat --pid=$(adb shell pidof com.a2ui.renderer)
```

## Expected Functionality

Based on README.md and test results, after launching you should see:
1. Home screen with menu icon (top-right corner)
2. Click menu → Digital Forms page should appear
3. All UI components should render from JSON configuration
4. Full journey from homepage → digital forms → change name form → acknowledgment should work

## Known Working Configuration

According to JOURNEY_EMULATOR_TEST_FINAL.md, the following worked:
- Emulator: Medium_Phone_API_34 (API version 34)
- Complete journey: Homepage → Menu → Digital Forms → Change Name Form → Acknowledgement
- All JSON-driven UI components functioning
- All navigation working correctly

## Troubleshooting

1. If seeing "command not found" errors:
```
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin
```

2. If build fails after code changes:
```
./gradlew clean
./gradlew assembleDebug
```

3. If emulator won't boot properly:
- Try Cold Boot option in Emulator Actions (when emulator is already running)
- Or wipe data and restart
- Increase RAM if system is slow: emulator -avd [AVD_NAME] -memory 4096

4. If install fails on connected device:
```
adb kill-server
adb start-server
./gradlew installDebug
```

5. Force stop app if needed:
```
adb shell am force-stop com.a2ui.renderer
```