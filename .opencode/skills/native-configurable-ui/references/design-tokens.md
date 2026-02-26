# Design Tokens Guide (With Form Engine Integration)

## Overview

Design tokens represent the smallest unit of design in a system. In the A2UI renderer configuration system, design tokens serve as the atomic building blocks for all visual properties. When working with the NEW Form Engine architecture, all design token usage flows through centralized Form State management.

## Traditional vs Form Engine Approach

### BEFORE (Traditional Distributed Approach):
```
Component Renderer 
├─▶ Reads token directly from Theme config
├─▶ Converts to Material property manually
└─▶ Applies directly to UI
```

### AFTER (Form Engine Centralized Approach):
```
Form Engine (Central Authority) 
├─▶ Evaluates theme expressions and tokens from configuration
├─▶ Calculates all visual properties and applies to Form State
├─▶ Component Renderers consume calculated values from Form State
├─▶ Component Renderers no longer make direct design token calculations
└─▶ Single authoritative source of visual properties
```

## Token Categories

### Color Tokens
Color tokens define the complete color palette used in the application. The Form Engine manages these through centralized evaluation ensuring consistency. The traditional approach分散ly handled color lookup/resolution. The NEW approach centralizes in Form Engine:

#### Standard Color Tokens (Now managed by Form Engine)
```json
{
  "palette": {
    "primary": {
      "50": "#ffebee",
      "100": "#ffcdd2",
      "200": "#ef9a9a", 
      "300": "#e57373",
      "400": "#ef5350",
      "500": "#f44336",   // ← PRIMARY BRAND RED
      "600": "#e53935",   // ← DARKER VERSION
      "700": "#d32f2f",   // ← EVEN DARKER
      "800": "#c62828",   // ← DARKEST
      "900": "#b71c1c"
    },
    "secondary": {
      "500": "#9c27b0",
      "700": "#7b1fa2"
    }
  }
}
```

In the NEW Form Engine approach:
- The Form Engine reads color tokens from theme configuration
- Component rendering now uses Form State which has already processed theme tokens
- Form Engine calculates the final color value to apply based on theme selection

#### Semantic Color Tokens (With Form Engine Processing)
Instead of scattered color selection, the Form Engine processes semantic meanings:

```json
{
  "colors": {
    "background": "{palette.primary.50}",     // ← Now resolved by Form Engine
    "surface": "{palette.primary.100}",       // ← Now resolved by Form Engine  
    "onSurface": "{palette.primary.900}",     // ← Now resolved by Form Engine
    "error": "{palette.secondary.500}",       // ← Now resolved by Form Engine
    "primary": "{palette.primary.700}",       // ← Now resolved by Form Engine 
    "primaryContainer": "{palette.primary.200}" // ← Now resolved by Form Engine
  }
}
```

With NEW Form Engine Flow:
1. ConfigManager reads semantic color definitions
2. Form Engine calculates actual color values based on palette references
3. Component renderers get resolved colors from Form Engine state flow
4. Component no longer resolves design tokens directly

### Spacing Tokens 
Spacing tokens define the unit-based spacing system. NEW Form Engine manages spacing resolution:

#### Spacing Scale (Now with Form Engine Management)
```json
{
  "spacing": {
    "xs": "4dp",
    "sm": "8dp", 
    "md": "16dp",
    "lg": "24dp",
    "xl": "32dp",
    "xxl": "40dp"
  }
}
```

NEW Workflow:
- Form Engine reads spacing tokens at initialization
- Components get final spacing values from central Form State rather than token calculation

### Typography Tokens
Typography tokens define text hierarchy. The NEW Form Engine processes these:

#### Material 3 Typography Definitions (Form Engine-Processed)
```json
{
  "typography": {
    "displayLarge": {
      "fontFamily": "Roboto",
      "fontWeight": 300,
      "fontSize": 57,
      "letterSpacing": -0.25,
      "lineHeight": 64
    },
    "displayMedium": {
      "fontFamily": "Roboto", 
      "fontWeight": 300,
      "fontSize": 45,
      "letterSpacing": 0,
      "lineHeight": 52
    },
    "headlineLarge": {
      "fontFamily": "Roboto",
      "fontWeight": 400,
      "fontSize": 32,
      "letterSpacing": 0,
      "lineHeight": 40
    }
  }
}
```

Form Engine Process:
- Theme configuration loaded, typographic tokens evaluated by centralized engine
- Form State contains processed typography ready for component consumption
- Components access final typography from Form Engine state (no direct token lookup)

## Usage in Configuration
### Component Configuration with NEW Form Engine Processing

In the NEW approach with Form Engine, components reference design tokens but the Form Engine does the resolving:

```json
{
  "id": "button_primary_action",
  "type": "Button",
  "properties": {
    "text": {"literalString": "Submit Form"},
    "background-color": "semantic:primary",        // ← Form Engine resolves this
    "text-color": "semantic:onPrimary",           // ← Form Engine resolves this  
    "padding": "spacing:md"                        // ← Form Engine resolves this
  }
}
```

The NEW Form Engine workflow:
1. Component configuration specifies semantic color usage in background-color, etc.
2. Form Engine processes this configuration and resolves color tokens
3. Component renderer accesses final resolved values from Form Engine state
4. Component uses resolved values without needing token resolution logic

## Conversion to Native Properties

In the traditional approach, renderers individually converted tokens to native properties. With Form Engine:
- Form Engine performs centralized token-todynamic-property resolution
- Component renderers receive fully-processed values from Form State
- Single consistent resolution across all components

### For iOS Components
- UIColor creation from resolved hex values
- UIFont creation from resolved typography tokens  
- CGVector from spacing token resolution
- Form Engine manages these conversions

iOS Conversion via Form Engine:
```
Form Engine processes "semantic:primary" → resolves to #color_hex → stores in Form State 
→ iOS component renderer reads processed UIColor from Form Engine state
```

### For Android Components  
- Color creation from resolved hex values (`android.graphics.Color`)
- Typography from resolved font specifications (`androidx.compose.ui.text.TextStyle`)
- Spacing from resolved dp values (`androidx.compose.ui.unit.Dp`)
- All centralized in Form Engine processing

Android Conversion via Form Engine:
```
Form Engine processes "spacing:md" → resolves to Dp value → stores in Form State
→ Android component renderer reads processed Dp from Form Engine state
```

## Best Practices with NEW Form Engine Approach

### 1. Follow Atomic Design with Semantic Naming  
- Use semantic names that don't change with design updates (e.g., `semantic:primary` vs `HSBC-red`)
- The Form Engine maps semantic tokens to actual values
- Centralized token changes apply consistently through Form Engine

### 2. Prefer Semantic Over Literal
- Reference semantic tokens (`semantic:primaryContainer`) not literal values (`#f8bbd0`)  
- Form Engine handles translation and can manage consistency
- Design changes can be updated in one place in Form Engine

### 3. Maintain Token Specificity  
- Use specific tokens appropriate to context (`semantic:error` for error state icons)
- Leverage Form Engine's centralized state to ensure context-appropriate values
- Avoid reusing tokens for inappropriate contexts

### 4. Namespace Appropriately
- Prefix tokens properly (`semantic:primary`, `spacing:md`) so Form Engine can process correctly
- Maintain consistent token hierarchy across configurations read by Form Engine

## Migration to Form Engine  

When transitioning existing configurations to work with NEW Form Engine:

### 1. Review Token References
Check all `.color` and `.spacing` references in configuration and verify Form Engine can resolve them

### 2. Update Property Mappings
Ensure component properties reference tokens with proper format for Form Engine resolution

### 3. Update Component Renderers
Remove direct token resolution from component renderers (Form Engine does this)

### 4. Test Token Consistency
Verify that Form Engine resolves tokens consistently across all components

## Troubleshooting NEW Form Engine Token Issues

### Token Not Resolving
- Verify token exists in theme configurations loaded by Form Engine
- Check spelling matches Form Engine token dictionary
- Ensure configuration uses correct format for Form Engine processing

### Wrong Value Resolved
- Check semantic meaning in Form Engine context  
- Verify semantic token resolves to correct base token
- Review Form Engine's token resolution logic for this reference type

### Performance Issues
- Confirm Form Engine caching is optimized for frequently used tokens
- Verify resolved tokens are cached for reuse across component instances

## Key Takeaways with NEW Form Engine Integration

1. **Centralized Resolution**: The Form Engine performs all design token resolution instead of scattered component logic
2. **Single Authority**: Component renderers consume from Form Engine instead of independently processing tokens  
3. **Consistent Results**: All components receive same resolved values for tokens via Form Engine
4. **Easier Maintenance**: Changes to token definitions automatically propagate through Form Engine

The NEW Form Engine approach significantly reduces component complexity while ensuring consistent design token usage throughout the application.