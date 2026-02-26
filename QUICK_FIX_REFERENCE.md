# Quick Fix Reference - Page Not Found

## One-Line Fix

**Problem**: Menu click → "Page not found"

**Solution**: Fix journey config to use file names, not page IDs.

---

## The Fix (2 Files)

### 1. change_name_journey.jsonl

```json
{
  "pages": [
    "digital_forms_page",      // ← Add _page suffix
    "change_name_form_page",   // ← Add _page suffix  
    "acknowledgement_page"     // ← Add _page suffix
  ],
  "defaultPage": "digital_forms_page"
}
```

### 2. top_nav_section.jsonl

```json
{
  "id": "nav_menu",
  "action": {
    "event": "navigate:change_name_journey:digital_forms"  // ← Remove _page suffix
  }
}
```

---

## Rule of Thumb

| Where | Use | Example |
|-------|-----|---------|
| **Journey config** (`journey.pages`) | File names | `digital_forms_page` |
| **Navigation event** (`action.event`) | Page IDs | `digital_forms` |
| **Page JSON** (`id` field) | Page IDs | `digital_forms` |
| **File name** | File names | `digital_forms_page.jsonl` |

---

## Quick Test

```bash
# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.a2ui.renderer/.MainActivity

# Click menu
adb shell input tap 950 120

# Check logs (should see "Loaded", NOT "Page not found")
adb logcat -d | grep -E "Loaded|Page not found"
```

**Expected**:
```
✅ Loaded digital_forms_page: 1 lines
```

**Not this**:
```
❌ Page not found: digital_forms
```

---

## Verify It Works

**Screenshot size check**:
```bash
adb exec-out screencap -p > test.png
ls -lh test.png
```

- **100K+** = ✅ Working (full page)
- **30-40K** = ❌ Broken (error screen)

---

## Common Mistakes

### ❌ WRONG
```json
// Journey using page IDs
{"pages": ["digital_forms"]}

// Navigation using file names  
{"event": "navigate:journey:digital_forms_page"}
```

### ✅ CORRECT
```json
// Journey using file names
{"pages": ["digital_forms_page"]}

// Navigation using page IDs
{"event": "navigate:journey:digital_forms"}
```

---

## Quick Checklist

- [ ] Journey `pages` array uses `_page` suffix?
- [ ] Journey `defaultPage` uses `_page` suffix?
- [ ] Menu action `event` does NOT use `_page` suffix?
- [ ] Page JSON `id` field does NOT use `_page` suffix?
- [ ] APK rebuilt after changes?
- [ ] App reinstalled (uninstall first)?
- [ ] Logs show "Loaded" not "Page not found"?

---

## Debug Commands

```bash
# Check journey config
cat app/src/main/res/raw/change_name_journey.jsonl | python3 -c "
import sys,json
d=json.loads(sys.stdin.read())
print('Pages:', d.get('pages'))
"

# Check menu action
cat app/src/main/res/raw/top_nav_section.jsonl | python3 -c "
import sys,json
for line in sys.stdin.read().strip().split('\n'):
    d=json.loads(line)
    if d.get('id')=='nav_menu':
        print('Menu action:', d.get('action'))
"

# Check page ID
cat app/src/main/res/raw/digital_forms_page.jsonl | python3 -c "
import sys,json
d=json.loads(sys.stdin.read())
print('Page ID:', d.get('id'))
"
```

---

**Remember**: File names for loading, Page IDs for navigation!

---

**Last Updated**: 2025-02-25
**Status**: ✅ Fixed
