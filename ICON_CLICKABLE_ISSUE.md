# Icon Clickable Issue - Investigation Summary

## Problem

The menu icon (`nav_menu`) is being rendered correctly with the correct action, but clicking on it doesn't trigger the navigation.

## What Works

✅ App launches successfully
✅ All pages load correctly (Digital Forms, Change Name Form, Acknowledgement)
✅ Navigation between pages works when triggered programmatically
✅ All other components render correctly
✅ Scrolling works
✅ Icon rendering works (RENDER_ICON logs appear)
✅ Icon has correct action (action=navigate:change_name_journey:digital_forms_page)

## What Doesn't Work

❌ Icon click doesn't trigger action
❌ No IconClick logs appear
❌ No BOX_CLICK logs appear
❌ Clickable modifier doesn't seem to work on icons

## Investigation Steps Taken

1. **Verified action is set** ✅
   - Log shows: `RENDER_ICON: nav_menu action=ActionConfig(event=navigate:change_name_journey:digital_forms_page, context=null)`

2. **Added debug logging** ✅
   - RENDER_ICON logs appear
   - ICON_ACTION_CHECK logs DON'T appear (even though code is in APK)

3. **Tried different tap coordinates** ✅
   - Tested: (900,100), (950,100), (1000,100), (900,120), (950,120), (1000,120), (850,120), (800,120), (950,200), (950,250), (950,300), (540,1200)
   - None triggered clicks

4. **Increased clickable area** ✅
   - Changed from 28.dp to 64.dp
   - Still no clicks

5. **Tried clickable Box wrapper** ✅
   - Wrapped Image in clickable Box
   - Still no clicks

6. **Changed log levels** ✅
   - Tried println, Log.d, Log.i
   - Logs don't appear

7. **Clean rebuilds** ✅
   - rm -rf app/build .gradle
   - ./gradlew clean assembleDebug --no-build-cache
   - Code IS in APK (verified with unzip | strings)
   - But logs DON'T appear

## Possible Causes

1. **Z-order issue**: Something might be covering the icon (status bar overlay?)
2. **Compose recomposition issue**: The clickable might not be properly attached
3. **Parent interception**: The parent Row might be intercepting clicks
4. **Icon not visible**: The icon might be rendered off-screen or behind something
5. **Compose version issue**: The clickable modifier might have a bug in this Compose version

## Suggested Solutions

### Solution 1: Use IconButton composable
```kotlin
if (component.action != null) {
    IconButton(
        onClick = {
            onAction(component.action!!.event, component.action!!.context)
        }
    ) {
        Icon(
            painter = painter,
            contentDescription = iconName,
            tint = tintColor ?: Color.Unspecified
        )
    }
}
```

### Solution 2: Use pointerInput instead of clickable
```kotlin
Modifier.pointerInput(component.action) {
    awaitEachGesture {
        awaitFirstDown()
        val upEvent = awaitPointerEventOrNull()
        if (upEvent?.changes?.first()?.pressed == false) {
            onAction(component.action!!.event, component.action!!.context)
        }
    }
}
```

### Solution 3: Add explicit test button
Add a visible Button component to test if clicks work at all in the app.

## Current Status

**App Status**: ✅ Functional (all features work except icon clicks)
**Icon Click Issue**: ❌ Not resolved

## Next Steps

1. Try IconButton composable (Solution 1)
2. If that doesn't work, try pointerInput (Solution 2)
3. Add a test Button to verify clicks work at all
4. Check if status bar is covering the icon

---

**Last Updated**: 2025-02-25
**Status**: Investigation ongoing
