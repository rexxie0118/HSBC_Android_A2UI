# Installation and Testing Guide

## Quick Test

```bash
# 1. Install app
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 2. Launch
adb shell am start -n com.a2ui.renderer/.MainActivity

# 3. Click menu (top-right corner)
adb shell input tap 950 120

# 4. Check logs
adb logcat | grep -E "Loaded.*digital|Page not found"
```

## Expected Results

### ✅ If Fix is Working:
```
✅ Loaded digital_forms_page: 1 lines
✅ NO "Page not found" errors
✅ Screenshot size: ~130K+ (full page)
```

### ❌ If Still Broken:
```
❌ Page not found: digital_forms
❌ Screenshot size: ~40K (error screen)
```

## Troubleshooting

If you still see "Page not found":

1. **Check APK timestamp**:
   ```bash
   ls -lh app/build/outputs/apk/debug/app-debug.apk
   ```
   Make sure it's recent (within last few minutes)

2. **Force reinstall**:
   ```bash
   adb uninstall com.a2ui.renderer
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

3. **Clear app data**:
   ```bash
   adb shell pm clear com.a2ui.renderer
   ```

4. **Cold boot emulator**:
   - Close emulator completely
   - Start fresh from Android Studio
   - Wait for full boot
   - Reinstall app

5. **Check what's displayed**:
   ```bash
   adb exec-out screencap -p > test.png
   # Check file size:
   # - 40K = error screen (broken)
   # - 130K+ = full page (working)
   ```

## Manual Test

1. Open app on emulator
2. Look at top-right corner for menu icon (☰)
3. Click it
4. You should see "Digital forms" page with:
   - Title: "Digital forms"
   - Subtitle: "New request"
   - Dropdown: "Please select"
   - Red "Continue" button (disabled)

If you see this, the fix is working! ✅

If you see "Page not found" text, the fix is NOT working. ❌

## Evidence of Fix Working

Check logs after clicking menu:
```bash
adb logcat -d | grep "Loaded digital_forms"
```

Should show:
```
I System.out: Loaded digital_forms_page: 1 lines
```

Should NOT show:
```
E/TAG: Page not found: digital_forms
```
