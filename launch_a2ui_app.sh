#!/bin/bash

# A2UI Application Deployment Helper Script

echo "Setting up Android environment..."
export ANDROID_HOME=$HOME/Library/Android/sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0

echo "Built A2UI application APK is located at:"
echo "  app/build/outputs/apk/debug/app-debug.apk"
echo ""

echo "Current device status:"
adb devices

echo ""
echo "If you see an emulator listed as 'device', run the following command separately:"
echo "  ./gradlew installDebug"
echo ""
echo "If no device is listed, you need to first start an emulator:"
echo "  1. Use Android Studio -> Tools -> AVD Manager to start Pixel_9_Pro_XL"
echo "  2. Or run: emulator -avd Pixel_9_Pro_XL from command line"
echo "  3. Wait for the emulator to show 'Android' on screen (boot completed)"
echo ""
echo "Once device shows 'device' status, install and launch with:"
echo "  ./gradlew installDebug"
echo ""
echo "The A2UI Application is ready to be deployed!"