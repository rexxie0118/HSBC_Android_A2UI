# Design Tokens

Design tokens are named variables that store visual design attributes. They replace hardcoded values with meaningful names.

## Token Categories

### Colors

```swift
// iOS - Assets.xcassets or Swift enum
enum DesignColors {
    // Brand colors
    static let primary = Color("PrimaryColor")
    static let secondary = Color("SecondaryColor")
    static let accent = Color("AccentColor")
    
    // Semantic colors (auto-resolve light/dark)
    static let background = Color("BackgroundColor")
    static let surface = Color("SurfaceColor")
    static let elevated = Color("ElevatedColor")
    
    // Text colors
    static let textPrimary = Color("TextPrimaryColor")
    static let textSecondary = Color("TextSecondaryColor")
    static let textDisabled = Color("TextDisabledColor")
    
    // Status colors
    static let success = Color("SuccessColor")
    static let warning = Color("WarningColor")
    static let error = Color("ErrorColor")
    static let info = Color("InfoColor")
}
```

```kotlin
// Android - colors.xml or Kotlin object
object DesignColors {
    // Brand colors
    val primary = Color(R.color.primary)
    val secondary = Color(R.color.secondary)
    val accent = Color(R.color.accent)
    
    // Semantic colors
    val background = Color(R.color.background)
    val surface = Color(R.color.surface)
    val elevated = Color(R.color.elevated)
    
    // Text colors
    val textPrimary = Color(R.color.text_primary)
    val textSecondary = Color(R.color.text_secondary)
    val textDisabled = Color(R.color.text_disabled)
    
    // Status colors
    val success = Color(R.color.success)
    val warning = Color(R.color.warning)
    val error = Color(R.color.error)
    val info = Color(R.color.info)
}
```

### Spacing

Use a consistent scale (4px or 8px base):

```swift
// iOS
enum DesignSpacing {
    static let xs: CGFloat = 4
    static let sm: CGFloat = 8
    static let md: CGFloat = 16
    static let lg: CGFloat = 24
    static let xl: CGFloat = 32
    static let xxl: CGFloat = 48
}
```

```kotlin
// Android
object DesignSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 16.dp
    val lg = 24.dp
    val xl = 32.dp
    val xxl = 48.dp
}
```

### Typography

```swift
// iOS
enum DesignTypography {
    struct FontStyle {
        let font: Font
        let lineHeight: CGFloat
        let letterSpacing: CGFloat
    }
    
    // Display
    static let displayLarge = FontStyle(font: .system(size: 57, weight: .regular), lineHeight: 64, letterSpacing: -0.25)
    static let displayMedium = FontStyle(font: .system(size: 45, weight: .regular), lineHeight: 52, letterSpacing: 0)
    
    // Headlines
    static let headlineLarge = FontStyle(font: .system(size: 32, weight: .semibold), lineHeight: 40, letterSpacing: 0)
    static let headlineMedium = FontStyle(font: .system(size: 28, weight: .semibold), lineHeight: 36, letterSpacing: 0)
    static let headlineSmall = FontStyle(font: .system(size: 24, weight: .semibold), lineHeight: 32, letterSpacing: 0)
    
    // Body
    static let bodyLarge = FontStyle(font: .system(size: 16, weight: .regular), lineHeight: 24, letterSpacing: 0.5)
    static let bodyMedium = FontStyle(font: .system(size: 14, weight: .regular), lineHeight: 20, letterSpacing: 0.25)
    static let bodySmall = FontStyle(font: .system(size: 12, weight: .regular), lineHeight: 16, letterSpacing: 0.4)
    
    // Labels
    static let labelLarge = FontStyle(font: .system(size: 14, weight: .medium), lineHeight: 20, letterSpacing: 0.1)
    static let labelMedium = FontStyle(font: .system(size: 12, weight: .medium), lineHeight: 16, letterSpacing: 0.5)
    static let labelSmall = FontStyle(font: .system(size: 11, weight: .medium), lineHeight: 16, letterSpacing: 0.5)
}
```

```kotlin
// Android
object DesignTypography {
    data class FontStyle(
        val fontSize: TextUnit,
        val fontWeight: FontWeight,
        val lineHeight: TextUnit,
        val letterSpacing: TextUnit = TextUnit.Unspecified
    )
    
    // Display
    val displayLarge = FontStyle(57.sp, FontWeight.Normal, 64.sp)
    val displayMedium = FontStyle(45.sp, FontWeight.Normal, 52.sp)
    
    // Headlines
    val headlineLarge = FontStyle(32.sp, FontWeight.SemiBold, 40.sp)
    val headlineMedium = FontStyle(28.sp, FontWeight.SemiBold, 36.sp)
    val headlineSmall = FontStyle(24.sp, FontWeight.SemiBold, 32.sp)
    
    // Body
    val bodyLarge = FontStyle(16.sp, FontWeight.Normal, 24.sp, 0.5.sp)
    val bodyMedium = FontStyle(14.sp, FontWeight.Normal, 20.sp, 0.25.sp)
    val bodySmall = FontStyle(12.sp, FontWeight.Normal, 16.sp, 0.4.sp)
    
    // Labels
    val labelLarge = FontStyle(14.sp, FontWeight.Medium, 20.sp, 0.1.sp)
    val labelMedium = FontStyle(12.sp, FontWeight.Medium, 16.sp, 0.5.sp)
    val labelSmall = FontStyle(11.sp, FontWeight.Medium, 16.sp, 0.5.sp)
}
```

### Corner Radius

```swift
enum DesignCornerRadius {
    static let none: CGFloat = 0
    static let small: CGFloat = 4
    static let medium: CGFloat = 8
    static let large: CGFloat = 12
    static let xl: CGFloat = 16
    static let xxl: CGFloat = 24
    static let full: CGFloat = 9999  // Circular
}
```

```kotlin
object DesignCornerRadius {
    val none = 0.dp
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val xl = 16.dp
    val xxl = 24.dp
    val full = 9999.dp  // Circular
}
```

### Shadows / Elevation

```swift
// iOS - Shadows
enum DesignShadows {
    struct ShadowConfig {
        let color: Color
        let offset: CGSize
        let radius: CGFloat
        let opacity: Float
    }
    
    static let none = ShadowConfig(color: .clear, offset: .zero, radius: 0, opacity: 0)
    static let small = ShadowConfig(color: .black, offset: CGSize(width: 0, height: 2), radius: 4, opacity: 0.05)
    static let medium = ShadowConfig(color: .black, offset: CGSize(width: 0, height: 4), radius: 8, opacity: 0.1)
    static let large = ShadowConfig(color: .black, offset: CGSize(width: 0, height: 8), radius: 16, opacity: 0.15)
    static let xl = ShadowConfig(color: .black, offset: CGSize(width: 0, height: 16), radius: 32, opacity: 0.2)
}
```

```kotlin
// Android - Elevation
object DesignElevation {
    val none = 0.dp
    val small = 2.dp
    val medium = 4.dp
    val large = 8.dp
    val xl = 16.dp
    val xxl = 24.dp
}
```

## Configuration File Format

Define tokens in a shared JSON format for cross-platform consistency:

```json
{
  "colors": {
    "primary": { "light": "#007AFF", "dark": "#0A84FF" },
    "background": { "light": "#FFFFFF", "dark": "#000000" },
    "surface": { "light": "#F2F2F7", "dark": "#1C1C1E" }
  },
  "spacing": {
    "unit": "dp",
    "base": 4,
    "scale": { "xs": 1, "sm": 2, "md": 4, "lg": 6, "xl": 8, "xxl": 12 }
  },
  "typography": {
    "fontFamily": "SF Pro Text",
    "sizes": {
      "displayLarge": { "size": 57, "weight": "regular", "lineHeight": 64 }
    }
  },
  "cornerRadius": {
    "unit": "dp",
    "values": { "none": 0, "small": 4, "medium": 8, "large": 12 }
  }
}
```

## Usage in Components

```swift
// iOS - Using tokens in a card component
struct ConfigurableCard: View {
    let config: CardConfig
    
    var body: some View {
        VStack(alignment: .leading, spacing: DesignSpacing.sm) {
            Image(config.icon)
                .resizable()
                .frame(width: 40, height: 40)
            
            Text(config.title)
                .font(DesignTypography.headlineSmall.font)
                .foregroundColor(DesignColors.textPrimary)
            
            Text(config.subtitle)
                .font(DesignTypography.bodyMedium.font)
                .foregroundColor(DesignColors.textSecondary)
        }
        .padding(DesignSpacing.md)
        .background(DesignColors.surface)
        .cornerRadius(DesignCornerRadius.medium)
        .shadow(color: DesignShadows.medium.color,
                radius: DesignShadows.medium.radius,
                x: DesignShadows.medium.offset.width,
                y: DesignShadows.medium.offset.height)
    }
}
```

```kotlin
// Android - Using tokens in a card component
@Composable
fun ConfigurableCard(
    config: CardConfig,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = DesignColors.surface,
                shape = RoundedCornerShape(DesignCornerRadius.medium)
            )
            .shadow(
                elevation = DesignElevation.medium,
                shape = RoundedCornerShape(DesignCornerRadius.medium)
            )
            .padding(DesignSpacing.md),
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.sm)
    ) {
        Icon(
            painter = painterResource(config.icon),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = DesignColors.primary
        )
        
        Text(
            text = config.title,
            style = MaterialTheme.typography.headlineSmall,
            color = DesignColors.textPrimary
        )
        
        Text(
            text = config.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = DesignColors.textSecondary
        )
    }
}
```

## Token Generation Scripts

Generate platform-specific token files from a single JSON source:

```bash
# Generate iOS tokens
./scripts/generate-tokens --input tokens.json --platform ios --output DesignTokens.swift

# Generate Android tokens
./scripts/generate-tokens --input tokens.json --platform android --output DesignTokens.kt
```
