# Remaining Work - A2UI Renderer

## Analysis Summary

This document tracks remaining work to complete the A2UI renderer implementation, based on comparison of:
- **A2UI Spec v0.8** (https://a2ui.org/guides/renderer-development/)
- **Native Configurable UI Skills** (design tokens, theming, component patterns)
- **Current Implementation** (this codebase)

---

## Architecture Decision

> **Local-First Configuration**: Server communication is intentionally **LOW PRIORITY**. All configuration is stored locally in `res/raw/*.jsonl` files. This is a valid implementation choice for offline-first or embedded renderer scenarios.

---

## Priority Legend

| Priority | Meaning |
|----------|---------|
| 🔴 **P0** | Critical - blocks core functionality |
| 🟠 **P1** | High - major gaps in user experience |
| 🟡 **P2** | Medium - should have for completeness |
| 🟢 **P3** | Low - nice to have / future enhancement |

---

## P0: Critical - Theme System Integration

### 1. Connect Theme JSON to Compose MaterialTheme

**Problem**: `themes.jsonl` is loaded by `ConfigManager` but never reaches the actual UI.

**Files Involved**:
- `app/src/main/java/com/a2ui/renderer/ui/theme/Theme.kt` (lines 12-24)
- `app/src/main/java/com/a2ui/renderer/ui/theme/Color.kt` (hardcoded values)
- `app/src/main/res/raw/themes.jsonl` (rich theme data unused)

**Current State**:
```kotlin
// Theme.kt - Hardcoded colors
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,      // ❌ Hardcoded
    secondary = PurpleGrey80,
    tertiary = Pink80
)
```

**Required**:
```kotlin
// Theme.kt - Dynamic from ConfigManager
private fun getColorScheme(theme: Theme): ColorScheme {
    return if (theme.mode == "dark") {
        darkColorScheme(
            primary = Color(android.graphics.Color.parseColor(theme.colors["primary"])),
            secondary = Color(android.graphics.Color.parseColor(theme.colors["secondary"])),
            // ... all 30+ color tokens
        )
    } else {
        lightColorScheme(...)
    }
}
```

**Tasks**:
- [ ] Map all 30+ color tokens from `themes.jsonl` to Material3 `ColorScheme`
- [ ] Create dynamic `ColorScheme` builder from `Theme` data class
- [ ] Update `A2UIRendererTheme` to use `ConfigManager.getCurrentTheme()`
- [ ] Map typography from JSON to Material3 `Typography`

**Estimated Effort**: 2-3 hours

---

### 2. Runtime Theme Switching

**Problem**: Theme is read once at initialization, no way to switch at runtime.

**Files Involved**:
- `app/src/main/java/com/a2ui/renderer/config/ConfigManager.kt`
- `app/src/main/java/com/a2ui/renderer/MainActivity.kt`

**Required**:
- [ ] Add `StateFlow<Theme>` or `LiveData<Theme>` to `ConfigManager`
- [ ] Add `setTheme(themeId: String)` method
- [ ] Observe theme changes in `MainActivity.setContent { }`
- [ ] Add theme picker UI component for testing

**Estimated Effort**: 2 hours

---

## P1: High Priority - Data Binding & Dynamic Content

### 3. Implement Data Binding Resolver

**Problem**: `ListDataBinding` is parsed but never resolves data at runtime.

**Files Involved**:
- `app/src/main/java/com/a2ui/renderer/config/UIConfig.kt` (line 83-90)
- `app/src/main/java/com/a2ui/renderer/config/ConfigManager.kt` (resolve functions)

**Required**:
- [ ] Create `DataModelStore` class to hold runtime data per surface/page
- [ ] Implement `resolveBinding(path: String, dataModel: DataModel): Any?`
- [ ] Support both `path`-only and `path + literal*` binding modes
- [ ] Update component renderers to call binding resolver

**Estimated Effort**: 3-4 hours

---

### 4. Dynamic List Rendering

**Problem**: Lists render static children, can't iterate over data arrays.

**A2UI Spec Requirement**:
```json
{
  "children": {
    "template": {
      "dataBinding": "$.products",
      "componentId": "product_card",
      "itemVar": "product"
    }
  }
}
```

**Required**:
- [ ] Add `DataBinding` data class for template bindings
- [ ] Update `ComponentProperties` to include `childrenTemplate`
- [ ] Implement list iteration in `List.kt` renderer
- [ ] Support relative path resolution within template scope

**Estimated Effort**: 4-5 hours

---

### 5. UsageHint → Typography Mapping

**Problem**: A2UI spec uses `usageHint: "h1"` but renderer doesn't map to typography.

**Files Involved**:
- `app/src/main/java/com/a2ui/renderer/components/Text.kt`
- `app/src/main/res/raw/themes.jsonl` (typography config exists)

**Required**:
- [ ] Add `usageHint` field to `ComponentProperties`
- [ ] Create mapping: `h1` → `Typography.h1`, `body` → `Typography.body1`, etc.
- [ ] Apply typography from current theme (not hardcoded `Typography` object)
- [ ] Support all hints: h1-h5, body, caption, overline, button

**Estimated Effort**: 2 hours

---

## P2: Medium Priority - Missing Components & Features

### 6. Missing Standard Components

**A2UI Required Components Not Implemented**:

| Component | Priority | Notes |
|-----------|----------|-------|
| CheckBox | P2 | Basic toggle input |
| Slider | P2 | Numeric range input |
| Modal/Dialog | P2 | Overlay dialogs |
| Video | P3 | Video player |
| AudioPlayer | P3 | Audio player |
| DateTimeInput | P2 | Date/time picker |
| MultipleChoice | P2 | Radio/checkbox group |

**Tasks**:
- [ ] Implement CheckBox component
- [ ] Implement Slider component
- [ ] Implement Modal/Dialog component
- [ ] Implement DateTimeInput component
- [ ] Implement MultipleChoice component

**Estimated Effort**: 8-10 hours total

---

### 7. Shadow/Elevation System

**Problem**: `ShadowConfig` is parsed from JSON but never applied.

**Files Involved**:
- `app/src/main/res/raw/themes.jsonl` (shadow definitions exist)
- `app/src/main/java/com/a2ui/renderer/config/ConfigManager.kt` (parsed but unused)

**Required**:
- [ ] Create `ShadowModifier` composable
- [ ] Apply shadows to Card components based on theme
- [ ] Support shadow variants: card, elevated, floating

**Estimated Effort**: 2 hours

---

### 8. Component Catalog System

**Problem**: No way to register/use custom component catalogs.

**A2UI Spec**:
```json
{
  "beginRendering": {
    "catalogId": "custom-banking-components",
    "root": "home_page"
  }
}
```

**Required**:
- [ ] Add `CatalogRegistry` to manage component factories
- [ ] Support multiple catalogs (standard + custom)
- [ ] Add `catalogId` to rendering context

**Estimated Effort**: 3 hours

---

## P3: Low Priority - Server Communication (Optional)

> **Note**: Marked as **LOW PRIORITY** per architecture decision to use local-first configuration.

### 9. Server Communication Protocol

**A2UI Spec Features** (Optional for local-first):

| Feature | Status | Priority |
|---------|--------|----------|
| JSONL Stream Parsing | ✅ Implemented | - |
| beginRendering message | ❌ Not needed (local) | P3 |
| surfaceUpdate message | ❌ Not needed (static) | P3 |
| dataModelUpdate message | ❌ Not needed (local) | P3 |
| deleteSurface message | ❌ Not needed (single surface) | P3 |
| userAction to server | ❌ Local actions only | P3 |
| clientCapabilities | ❌ Not needed | P3 |
| Error reporting | ⚠️ Basic logging only | P3 |

**If implementing server support later**:
- [ ] Add HTTP client for A2A protocol
- [ ] Implement SSE stream parser
- [ ] Add surface management (create/update/delete)
- [ ] Implement userAction network sender
- [ ] Add client capabilities reporting

**Estimated Effort**: 20+ hours (deferred)

---

## P2: Medium Priority - Architecture Improvements

### 10. Surface Management (Optional)

**Problem**: Single hardcoded journey, no multi-surface support.

**Only needed if**: Supporting multiple concurrent UI surfaces or progressive rendering.

**Required** (if needed):
- [ ] Add `SurfaceManager` class
- [ ] Implement surface buffer (Map<surfaceId, ComponentBuffer>)
- [ ] Support multiple active surfaces
- [ ] Add surface lifecycle management

**Estimated Effort**: 6-8 hours (deferred unless needed)

---

### 11. Theme Manager ViewModel

**Skill Pattern Alignment**:

```kotlin
class ThemeManager @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {
    val currentTheme: StateFlow<ThemeType>
    val colors: ThemeColors
    val typography: ThemeTypography
    
    fun setTheme(theme: ThemeType)
}
```

**Required**:
- [ ] Create `ThemeManager` ViewModel class
- [ ] Add theme preferences persistence
- [ ] Expose theme state via StateFlow
- [ ] Integrate with `A2UIRendererTheme` composable

**Estimated Effort**: 3 hours

---

## Testing Checklist

### Theme Testing
- [ ] Light theme displays correct colors
- [ ] Dark theme displays correct colors
- [ ] Theme switch animates smoothly
- [ ] Typography matches theme config
- [ ] Shadows apply correctly to cards

### Component Testing
- [ ] All standard components render correctly
- [ ] UsageHint maps to correct typography
- [ ] Data binding resolves paths correctly
- [ ] Dynamic lists iterate properly
- [ ] Actions trigger correctly

### Accessibility Testing
- [ ] Color contrast meets WCAG AA (4.5:1)
- [ ] Minimum touch target 48dp
- [ ] Screen reader compatibility
- [ ] Keyboard navigation support

---

## Quick Wins Summary

| Task | Impact | Effort |
|------|--------|--------|
| Connect theme to Compose | 🔴 High | 2-3h |
| Add usageHint mapping | 🟠 High | 2h |
| Runtime theme switching | 🟠 High | 2h |
| Data binding resolver | 🟠 High | 3-4h |
| Shadow system | 🟡 Medium | 2h |
| Missing components | 🟡 Medium | 8-10h |

---

## File Reference Map

| Feature | Files to Modify |
|---------|-----------------|
| Theme Integration | `Theme.kt`, `Color.kt`, `ConfigManager.kt` |
| Typography | `Type.kt`, `Text.kt`, `UIConfig.kt` |
| Data Binding | `ConfigManager.kt`, new `DataModelStore.kt` |
| Dynamic Lists | `List.kt`, `UIConfig.kt` |
| Theme Switching | `MainActivity.kt`, new `ThemeManager.kt` |
| Shadows | `Card.kt`, new `ShadowModifier.kt` |
| Missing Components | New component files in `components/` |

---

## Last Updated

2026-02-24

---

## Notes

- Server communication intentionally deferred (local-first architecture)
- Focus on making local theme system fully functional first
- Component completeness is good, priority is integration over new features
- All JSON configs are well-structured, just need to connect to UI layer
