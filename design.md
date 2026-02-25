# A2UI Renderer - Design System

> **Note**: This document defines the A2UI-specific design system implementation. For platform-agnostic design token guidance with iOS/Android/SwiftUI examples, see [design-tokens.md](../.config/opencode/skills/native-configurable-ui/references/design-tokens.md).

## Overview

This document defines the complete design system for the A2UI Renderer, including design tokens, theming architecture, typography system, and visual guidelines.

---

## Design Tokens

Design tokens are the atomic units of the design system, stored in JSON configuration files and consumed by the renderer.

### Token Categories

| Category | Description | Example |
|----------|-------------|---------|
| **Color** | Semantic color definitions | `primary`, `secondary`, `background` |
| **Typography** | Font sizes, weights, line heights | `h1`, `body1`, `caption` |
| **Spacing** | Margins, padding, gaps | `small`, `medium`, `large` |
| **Border Radius** | Corner rounding | `small`, `medium`, `large` |
| **Shadows** | Elevation and depth | `card`, `elevated`, `floating` |
| **Animation** | Duration, easing curves | `fast`, `normal`, `slow` |

---

## Color System

### Color Token Structure

```json
{
  "type": "theme",
  "id": "banking_light",
  "mode": "light",
  "colors": {
    "primary": "#D32F2F",
    "primaryVariant": "#B71C1C",
    "secondary": "#1976D2",
    "secondaryVariant": "#1565C0",
    "background": "#F5F5F5",
    "surface": "#FFFFFF",
    "error": "#B00020",
    "onPrimary": "#FFFFFF",
    "onSecondary": "#FFFFFF",
    "onBackground": "#000000",
    "onSurface": "#000000",
    "onError": "#FFFFFF",
    "outline": "#BDBDBD",
    "outlineVariant": "#E0E0E0",
    "divider": "#E8E8E8",
    "textPrimary": "#212121",
    "textSecondary": "#757575",
    "textTertiary": "#9E9E9E",
    "textDisabled": "#BDBDBD",
    "cardBackground": "#FFFFFF",
    "cardBorder": "#BDBDBD"
  }
}
```

### Color Token Reference

| Token | Light Theme | Dark Theme | Usage |
|-------|-------------|------------|-------|
| `primary` | #D32F2F (HSBC Red) | #FF5252 (Bright Red) | Primary actions, brand |
| `primaryVariant` | #B71C1C | #FF1744 | Primary hover states |
| `secondary` | #1976D2 (Blue) | #64B5F6 (Light Blue) | Secondary actions |
| `background` | #F5F5F5 | #121212 | App background |
| `surface` | #FFFFFF | #1E1E1E | Cards, sheets |
| `error` | #B00020 | #CF6679 | Error states |
| `outline` | #BDBDBD | #757575 | Borders, dividers |

### Color Application Guidelines

#### Primary Color
- **Usage**: Brand identity, primary buttons, active states
- **Contrast**: Must meet WCAG AA (4.5:1) with text
- **Don't**: Use for backgrounds (too dominant)

#### Secondary Color
- **Usage**: Secondary actions, accents, highlights
- **Contrast**: Pair with onSecondary for text
- **Don't**: Overuse (dilutes brand impact)

#### Background & Surface
- **Usage**: Content containers, page backgrounds
- **Elevation**: Surface should be lighter than background in light mode
- **Don't**: Use primary colors for large surfaces

---

## Typography System

### Typography Token Structure

```json
{
  "typography": {
    "fontFamily": "system",
    "h1": {
      "size": 36,
      "weight": "bold",
      "lineHeight": 44,
      "letterSpacing": 0
    },
    "h2": {
      "size": 28,
      "weight": "bold",
      "lineHeight": 36,
      "letterSpacing": 0
    },
    "h3": {
      "size": 24,
      "weight": "bold",
      "lineHeight": 32,
      "letterSpacing": 0
    },
    "h4": {
      "size": 22,
      "weight": "bold",
      "lineHeight": 28,
      "letterSpacing": 0.15
    },
    "h5": {
      "size": 18,
      "weight": "bold",
      "lineHeight": 24,
      "letterSpacing": 0
    },
    "h6": {
      "size": 16,
      "weight": "bold",
      "lineHeight": 22,
      "letterSpacing": 0.15
    },
    "body1": {
      "size": 16,
      "weight": "regular",
      "lineHeight": 24,
      "letterSpacing": 0.5
    },
    "body2": {
      "size": 14,
      "weight": "regular",
      "lineHeight": 20,
      "letterSpacing": 0.25
    },
    "caption": {
      "size": 12,
      "weight": "regular",
      "lineHeight": 16,
      "letterSpacing": 0.4
    },
    "overline": {
      "size": 11,
      "weight": "medium",
      "lineHeight": 16,
      "letterSpacing": 1.5
    },
    "button": {
      "size": 14,
      "weight": "medium",
      "lineHeight": 20,
      "letterSpacing": 1.25
    }
  }
}
```

### Typography Usage Mapping

| UsageHint | Material3 Token | Size | Weight | Use Case |
|-----------|-----------------|------|--------|----------|
| `h1` | displayLarge | 36sp | Bold | Page titles |
| `h2` | displayMedium | 28sp | Bold | Section headers |
| `h3` | displaySmall | 24sp | Bold | Sub-sections |
| `h4` | headlineLarge | 22sp | Bold | Card titles |
| `h5` | headlineMedium | 18sp | Bold | Group headers |
| `h6` | headlineSmall | 16sp | Bold | Item headers |
| `body` | bodyLarge | 16sp | Regular | Main content |
| `body2` | bodyMedium | 14sp | Regular | Secondary text |
| `caption` | bodySmall | 12sp | Regular | Captions, hints |
| `overline` | labelMedium | 11sp | Medium | Labels, tags |
| `button` | labelLarge | 14sp | Medium | Button text |

### Font Weight Values

| Value | CSS Equivalent | Usage |
|-------|---------------|-------|
| `thin` | 100 | Rarely used |
| `light` | 300 | Subtle emphasis |
| `regular` | 400 | Body text |
| `medium` | 500 | Labels, buttons |
| `bold` | 700 | Headings |
| `black` | 900 | Rarely used |

### Typography Best Practices

1. **Hierarchy**: Use heading levels consistently (h1 > h2 > h3)
2. **Line Length**: Optimal 45-75 characters per line
3. **Line Height**: 1.4-1.6x font size for readability
4. **Contrast**: Text must meet WCAG AA standards
5. **Scaling**: Support dynamic type / font scaling

---

## Spacing System

### Spacing Tokens

```json
{
  "spacing": {
    "xs": 4,
    "sm": 8,
    "md": 16,
    "lg": 24,
    "xl": 32,
    "xxl": 48
  }
}
```

### Spacing Scale

| Token | Value (dp) | Usage |
|-------|------------|-------|
| `xs` | 4dp | Tight spacing, icon padding |
| `sm` | 8dp | Compact layouts |
| `md` | 16dp | Standard spacing |
| `lg` | 24dp | Section separation |
| `xl` | 32dp | Major sections |
| `xxl` | 48dp | Page margins |

### Spacing Application

```kotlin
// Component spacing example
Column(
    modifier = Modifier.padding(16.dp), // md
    verticalArrangement = Arrangement.spacedBy(8.dp) // sm
) {
    Text("Title")
    Text("Content")
}
```

---

## Shadow & Elevation System

### Shadow Token Structure

```json
{
  "shadows": {
    "card": {
      "color": "#000000",
      "alpha": 0.1,
      "blur": 8,
      "offsetX": 0,
      "offsetY": 2
    },
    "elevated": {
      "color": "#000000",
      "alpha": 0.15,
      "blur": 12,
      "offsetX": 0,
      "offsetY": 4
    },
    "floating": {
      "color": "#000000",
      "alpha": 0.2,
      "blur": 16,
      "offsetX": 0,
      "offsetY": 8
    },
    "pressed": {
      "color": "#000000",
      "alpha": 0.05,
      "blur": 4,
      "offsetX": 0,
      "offsetY": 1
    }
  }
}
```

### Elevation Levels

| Level | Shadow Token | Blur | Offset | Use Case |
|-------|-------------|------|--------|----------|
| 0 | None | 0 | 0 | Flat surfaces |
| 1 | card | 8dp | 2dp | Cards, list items |
| 2 | elevated | 12dp | 4dp | Floating panels |
| 3 | floating | 16dp | 8dp | FABs, modals |
| 4 | pressed | 4dp | 1dp | Pressed states |

### Shadow Implementation

```kotlin
@Composable
fun CardWithShadow(
    modifier: Modifier = Modifier,
    elevation: Int = 1 // 0-4
) {
    val shadowConfig = when (elevation) {
        0 -> ShadowConfig(alpha = 0f, blur = 0)
        1 -> ShadowConfig(alpha = 0.1f, blur = 8, offsetY = 2)
        2 -> ShadowConfig(alpha = 0.15f, blur = 12, offsetY = 4)
        3 -> ShadowConfig(alpha = 0.2f, blur = 16, offsetY = 8)
        4 -> ShadowConfig(alpha = 0.05f, blur = 4, offsetY = 1)
        else -> ShadowConfig()
    }
    
    Card(
        modifier = modifier.shadow(shadowConfig),
        // ... rest of card
    )
}
```

---

## Border Radius System

### Radius Tokens

```json
{
  "borderRadius": {
    "none": 0,
    "small": 4,
    "medium": 8,
    "large": 12,
    "xl": 16,
    "full": 9999
  }
}
```

### Radius Application

| Token | Value | Usage |
|-------|-------|-------|
| `none` | 0dp | Dividers, strict layouts |
| `small` | 4dp | Small buttons, chips |
| `medium` | 8dp | Standard cards, buttons |
| `large` | 12dp | Large cards, modals |
| `xl` | 16dp | Hero cards, banners |
| `full` | 9999dp | Circular elements, pills |

---

## Animation System

### Animation Tokens

```json
{
  "animation": {
    "duration": {
      "fast": 150,
      "normal": 300,
      "slow": 500
    },
    "easing": {
      "linear": [0, 0, 1, 1],
      "easeIn": [0.42, 0, 1, 1],
      "easeOut": [0, 0, 0.58, 1],
      "easeInOut": [0.42, 0, 0.58, 1]
    }
  }
}
```

### Animation Guidelines

| Duration | Value | Usage |
|----------|-------|-------|
| `fast` | 150ms | Micro-interactions, hover |
| `normal` | 300ms | Standard transitions |
| `slow` | 500ms | Major state changes |

### Easing Curves

| Curve | Bezier | Usage |
|-------|--------|-------|
| `linear` | [0, 0, 1, 1] | Mechanical, progress |
| `easeIn` | [0.42, 0, 1, 1] | Accelerating out |
| `easeOut` | [0, 0, 0.58, 1] | Decelerating in |
| `easeInOut` | [0.42, 0, 0.58, 1] | Standard transitions |

---

## Theme Architecture

### Theme Configuration

```json
{
  "type": "theme",
  "id": "banking_light",
  "mode": "light",
  "colors": { ... },
  "shadows": { ... },
  "typography": { ... }
}
```

### Theme Modes

| Mode | ID | Usage |
|------|----|-------|
| Light | `banking_light` | Default, daytime |
| Dark | `banking_dark` | Night, OLED screens |

### Theme Switching Flow

```
User clicks theme toggle
        ↓
ConfigManager.setTheme("banking_dark")
        ↓
_themeFlow.value = newTheme (StateFlow emits)
        ↓
A2UIRendererTheme collects change
        ↓
Recompose with new ColorScheme
        ↓
All components update automatically
```

### Theme Implementation

```kotlin
@Composable
fun A2UIRendererTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val currentTheme = ConfigManager.themeFlow.collectAsState(initial = null).value
    
    val colorScheme = if (currentTheme?.mode == "dark") {
        darkColorScheme(
            primary = Color(0xFFFF5252),
            secondary = Color(0xFF64B5F6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            // ... all tokens
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFD32F2F),
            secondary = Color(0xFF1976D2),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
            // ... all tokens
        )
    }
    
    val typography = currentTheme?.typography?.toMaterialTypography() 
        ?: Typography()
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
```

---

## Component Design Patterns

### Button Variants

```json
{
  "button": {
    "primary": {
      "backgroundColor": "primary",
      "textColor": "onPrimary",
      "elevation": 1,
      "borderRadius": "medium"
    },
    "secondary": {
      "backgroundColor": "transparent",
      "textColor": "primary",
      "border": {
        "color": "primary",
        "width": 1
      },
      "borderRadius": "medium"
    },
    "text": {
      "backgroundColor": "transparent",
      "textColor": "primary",
      "borderRadius": "medium"
    }
  }
}
```

### Card Variants

```json
{
  "card": {
    "standard": {
      "backgroundColor": "cardBackground",
      "borderRadius": "large",
      "elevation": 1,
      "padding": "md"
    },
    "elevated": {
      "backgroundColor": "cardBackground",
      "borderRadius": "large",
      "elevation": 2,
      "padding": "md"
    }
  }
}
```

---

## Accessibility Guidelines

### Color Contrast

| Element | Minimum Ratio | Target |
|---------|--------------|--------|
| Normal text | 4.5:1 (AA) | 7:1 (AAA) |
| Large text (18pt+) | 3:1 (AA) | 4.5:1 (AAA) |
| UI components | 3:1 (AA) | 4.5:1 (AAA) |

### Touch Targets

| Element | Minimum Size | Recommended |
|---------|-------------|-------------|
| Buttons | 48x48dp | 56x56dp |
| Icons | 24x24dp | 32x32dp |
| Form inputs | 48dp height | 56dp height |

### Typography Accessibility

- **Minimum size**: 12sp (caption), 14sp (body)
- **Maximum line length**: 75 characters
- **Line height**: Minimum 1.4x font size
- **Letter spacing**: Adjust for readability at small sizes

---

## Design Token Resolution

### Token Reference Syntax

```json
{
  "color": "primary",
  "spacing": "md",
  "typography": "h1",
  "shadow": "card",
  "borderRadius": "medium"
}
```

### Resolution Process

```
Component JSON references token
        ↓
ConfigManager.resolveColor("primary")
        ↓
Looks up in current theme
        ↓
Returns actual color value (#D32F2F)
        ↓
Applied to component
```

### Implementation

```kotlin
object ConfigManager {
    fun resolveColor(token: String?): String? {
        if (token == null) return null
        if (!token.startsWith("$.")) return token
        
        // Resolve from theme
        return currentTheme?.colors?.get(token)
    }
    
    fun resolveSpacing(token: String?): Dp {
        return when (token) {
            "xs" -> 4.dp
            "sm" -> 8.dp
            "md" -> 16.dp
            "lg" -> 24.dp
            "xl" -> 32.dp
            "xxl" -> 48.dp
            else -> 0.dp
        }
    }
}
```

---

## Design System Best Practices

### 1. Consistency
- Use tokens consistently across all components
- Never hardcode values in components
- Follow established patterns

### 2. Scalability
- Design tokens should be themeable
- Support multiple screen sizes
- Consider internationalization

### 3. Accessibility
- Meet WCAG AA standards minimum
- Support dynamic type
- Ensure touch targets are large enough

### 4. Performance
- Cache resolved tokens
- Avoid runtime calculations
- Use Compose remember for expensive operations

### 5. Documentation
- Document all tokens
- Provide usage examples
- Maintain changelog

---

## References

- [Material Design 3](https://m3.material.io/)
- [WCAG 2.1 Guidelines](https://www.w3.org/WAI/WCAG21/quickref/)
- [A2UI Spec v0.8](https://a2ui.org/guides/theming/)
