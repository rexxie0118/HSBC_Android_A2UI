# Theming System

A robust theming system supports multiple themes with runtime switching.

## Theme Architecture

### Theme Types

```swift
// iOS
enum ThemeType: String, CaseIterable, Codable {
    case system      // Follows OS setting
    case light
    case dark
    case highContrast
    case brand       // Custom brand theme
}
```

```kotlin
// Android
enum class ThemeType {
    SYSTEM,      // Follows OS setting
    LIGHT,
    DARK,
    HIGH_CONTRAST,
    BRAND        // Custom brand theme
}
```

## Theme Manager Implementation

### iOS (SwiftUI)

```swift
// ThemeManager.swift
import SwiftUI
import Combine

class ThemeManager: ObservableObject {
    static let shared = ThemeManager()
    
    @Published var currentTheme: ThemeType {
        didSet {
            UserDefaults.standard.set(currentTheme.rawValue, forKey: "selectedTheme")
            applyTheme()
        }
    }
    
    var colorScheme: ColorScheme? {
        switch currentTheme {
        case .system: return nil
        case .light: return .light
        case .dark: return .dark
        case .highContrast: return .dark
        case .brand: return .dark
        }
    }
    
    var colors: ThemeColors {
        ThemeResolver.colors(for: currentTheme)
    }
    
    var typography: ThemeTypography {
        ThemeResolver.typography(for: currentTheme)
    }
    
    private init() {
        let stored = UserDefaults.standard.string(forKey: "selectedTheme")
        self.currentTheme = ThemeType(rawValue: stored ?? "system") ?? .system
    }
    
    private func applyTheme() {
        // Update UIAppearance for UIKit components
        UIView.appearance().tintColor = colors.primary
        UINavigationBar.appearance().tintColor = colors.primary
    }
}

// ThemeResolver.swift
struct ThemeResolver {
    static func colors(for theme: ThemeType) -> ThemeColors {
        switch theme {
        case .system, .light:
            return ThemeColors.light
        case .dark, .highContrast, .brand:
            return ThemeColors.dark
        }
    }
    
    static func typography(for theme: ThemeType) -> ThemeTypography {
        switch theme {
        case .highContrast:
            return ThemeTypography.highContrast
        default:
            return ThemeTypography.default
        }
    }
}

// ThemeColors.swift
struct ThemeColors {
    let primary: Color
    let secondary: Color
    let background: Color
    let surface: Color
    let textPrimary: Color
    let textSecondary: Color
    
    static let light = ThemeColors(
        primary: Color("PrimaryColor"),
        secondary: Color("SecondaryColor"),
        background: Color("BackgroundColor"),
        surface: Color("SurfaceColor"),
        textPrimary: Color("TextPrimaryColor"),
        textSecondary: Color("TextSecondaryColor")
    )
    
    static let dark = ThemeColors(
        primary: Color("PrimaryColorDark"),
        secondary: Color("SecondaryColorDark"),
        background: Color("BackgroundColorDark"),
        surface: Color("SurfaceColorDark"),
        textPrimary: Color("TextPrimaryColorDark"),
        textSecondary: Color("TextSecondaryColorDark")
    )
}
```

### Android (Jetpack Compose)

```kotlin
// ThemeManager.kt
class ThemeManager @Inject constructor(
    private val preferences: Preferences
) : ViewModel() {
    
    val currentTheme: StateFlow<ThemeType> = preferences.themeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ThemeType.SYSTEM
        )
    
    val colors: ThemeColors
        @Composable get() = ThemeColors.resolve(currentTheme.value)
    
    val typography: ThemeTypography
        @Composable get() = ThemeTypography.resolve(currentTheme.value)
    
    fun setTheme(theme: ThemeType) {
        viewModelScope.launch {
            preferences.saveTheme(theme)
        }
    }
}

// ThemeColors.kt
data class ThemeColors(
    val primary: Color,
    val secondary: Color,
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color
) {
    companion object {
        @Composable
        fun resolve(theme: ThemeType): ThemeColors {
            return when (theme) {
                ThemeType.SYSTEM, ThemeType.LIGHT -> LightColors
                ThemeType.DARK, ThemeType.HIGH_CONTRAST, ThemeType.BRAND -> DarkColors
            }
        }
        
        private val LightColors = ThemeColors(
            primary = Color(0xFF007AFF),
            secondary = Color(0xFF5856D6),
            background = Color(0xFFFFFFFF),
            surface = Color(0xFFF2F2F7),
            textPrimary = Color(0xFF000000),
            textSecondary = Color(0xFF666666)
        )
        
        private val DarkColors = ThemeColors(
            primary = Color(0xFF0A84FF),
            secondary = Color(0xFF5E5CE6),
            background = Color(0xFF000000),
            surface = Color(0xFF1C1C1E),
            textPrimary = Color(0xFFFFFFFF),
            textSecondary = Color(0xFF98989D)
        )
    }
}

// Theme.kt - Compose Theme
@Composable
fun AppTheme(
    themeType: ThemeType = ThemeType.SYSTEM,
    content: @Composable () -> Unit
) {
    val colors = ThemeColors.resolve(themeType)
    val typography = ThemeTypography.resolve(themeType)
    
    MaterialTheme(
        colorScheme = colors.toMaterialColorScheme(),
        typography = typography.toMaterialTypography()
    ) {
        content()
    }
}

private fun ThemeColors.toMaterialColorScheme(): ColorScheme {
    return ColorScheme(
        primary = primary,
        secondary = secondary,
        background = background,
        surface = surface,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = textPrimary,
        onSurface = textPrimary
    )
}
```

## Usage in Components

### iOS

```swift
// ContentView.swift
struct ContentView: View {
    @EnvironmentObject var themeManager: ThemeManager
    
    var body: some View {
        VStack(spacing: DesignSpacing.md) {
            Text("Welcome")
                .font(DesignTypography.headlineLarge.font)
                .foregroundColor(themeManager.colors.textPrimary)
            
            ConfigurableButton(
                config: ButtonConfig(style: .primary),
                title: "Get Started"
            ) {
                // Action
            }
            
            ThemePicker(selectedTheme: $themeManager.currentTheme)
        }
        .padding(DesignSpacing.lg)
        .background(themeManager.colors.background)
    }
}

// ThemePicker.swift
struct ThemePicker: View {
    @Binding var selectedTheme: ThemeType
    
    var body: some View {
        Picker("Theme", selection: $selectedTheme) {
            ForEach(ThemeType.allCases, id: \.self) { theme in
                Text(theme.displayName).tag(theme)
            }
        }
        .pickerStyle(.segmented)
    }
}
```

### Android

```kotlin
// ContentScreen.kt
@Composable
fun ContentScreen(
    themeManager: ThemeManager = viewModel()
) {
    val colors = themeManager.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(DesignSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DesignSpacing.md)
    ) {
        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineLarge,
            color = colors.textPrimary
        )
        
        ConfigurableButton(
            config = ButtonConfig(style = ButtonStyle.Primary),
            text = "Get Started",
            onClick = { }
        )
        
        ThemePicker(
            selectedTheme = themeManager.currentTheme.collectAsState().value,
            onThemeSelected = { themeManager.setTheme(it) }
        )
    }
}

// ThemePicker.kt
@Composable
fun ThemePicker(
    selectedTheme: ThemeType,
    onThemeSelected: (ThemeType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedTheme.name,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            ThemeType.entries.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(theme.name) },
                    onClick = {
                        onThemeSelected(theme)
                        expanded = false
                    }
                )
            }
        }
    }
}
```

## Runtime Theme Switching

### iOS - Animation Support

```swift
// Animate theme changes
withAnimation(.easeInOut(duration: 0.3)) {
    themeManager.currentTheme = .dark
}
```

### Android - Animation Support

```kotlin
// Animate theme changes
AnimatedContent(
    targetState = currentTheme,
    transitionSpec = { fadeIn() togetherWith fadeOut() }
) { theme ->
    ThemedContent(theme = theme)
}
```

## Configuration-Driven Themes

Load themes from JSON configuration:

```json
{
  "themes": {
    "light": {
      "colors": {
        "primary": "#007AFF",
        "background": "#FFFFFF",
        "surface": "#F2F2F7"
      }
    },
    "dark": {
      "colors": {
        "primary": "#0A84FF",
        "background": "#000000",
        "surface": "#1C1C1E"
      }
    },
    "brand": {
      "colors": {
        "primary": "#FF6B00",
        "background": "#1A1A2E",
        "surface": "#16213E"
      }
    }
  }
}
```

Parse and apply at runtime:

```swift
// Load theme from JSON
let themeData = try JSONDecoder().decode(ThemeConfig.self, from: jsonData)
ThemeRegistry.shared.register(themes: themeData.themes)
```
