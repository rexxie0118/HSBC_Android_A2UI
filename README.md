# A2UI Renderer

Configuration-driven UI renderer for Android using Jetpack Compose.

## Documentation

| Document | Description | Lines |
|----------|-------------|-------|
| [architecture.md](architecture.md) | **System architecture**, data flow, components | 681 |
| [design.md](design.md) | **Design system**, tokens, theming | 647 |
| [remainingwork.md](remainingwork.md) | **Implementation roadmap**, iteration specs | 5,625 |

## Quick Start

```kotlin
// In MainActivity.kt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize ConfigManager (loads themes, settings, etc.)
        ConfigManager.init(this)
        
        setContent {
            A2UIRendererTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationHost()
                }
            }
        }
    }
}
```

## Key Features

- ✅ **Theme System** - JSON-driven theming with runtime switching
- ✅ **Data Binding** - `$.path` expressions for dynamic data
- ✅ **Dynamic Lists** - Template-based list rendering
- ✅ **Validation** - Input validation with cross-field support
- ✅ **Dependencies** - Field dependencies (visibility, enabled, required)
- ✅ **Security** - 8 security policies implemented

## Project Status

| Iteration | Focus | Status |
|-----------|-------|--------|
| 1 | Theme Integration | ✅ Complete |
| 2 | Runtime Theme Switching | ✅ Complete |
| 3 | Data Binding | ✅ Complete |
| 4 | Dynamic Lists | ✅ Complete |
| 5 | Dynamic UI Rules | ✅ Complete |
| 6-10 | Advanced Features | 📋 Specified |

## Build & Test

```bash
# Build
./gradlew assembleDebug

# Unit tests
./gradlew testDebugUnitTest

# UI tests
./gradlew connectedDebugAndroidTest

# Install on emulator
./gradlew installDebug
```

## Architecture

See [architecture.md](architecture.md) for complete system architecture including:
- Data flow diagrams
- Component structure
- State management (StateFlow)
- Security architecture
- Performance strategies

## Design System

See [design.md](design.md) for complete design system including:
- Color tokens (30+ tokens)
- Typography system (11 levels)
- Spacing scale
- Shadow/elevation system
- Accessibility guidelines

## License

MIT License
