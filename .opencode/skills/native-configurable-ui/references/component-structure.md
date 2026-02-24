# Component Configuration Structure

## Configuration Object Pattern

Define all component options in a single configuration object.

### Basic Structure

```swift
// iOS
struct ComponentConfig {
    // Visual style
    var style: ComponentStyle = .default
    
    // Size
    var size: ComponentSize = .medium
    
    // State
    var isEnabled: Bool = true
    var isLoading: Bool = false
    
    // Layout
    var alignment: Alignment = .center
    var spacing: CGFloat? = nil
    
    // Customization overrides
    var backgroundColor: Color? = nil
    var cornerRadius: CGFloat? = nil
}
```

```kotlin
// Android
data class ComponentConfig(
    // Visual style
    val style: ComponentStyle = ComponentStyle.Default,
    
    // Size
    val size: ComponentSize = ComponentSize.Medium,
    
    // State
    val isEnabled: Boolean = true,
    val isLoading: Boolean = false,
    
    // Layout
    val alignment: Alignment = Alignment.Center,
    val spacing: Dp? = null,
    
    // Customization overrides
    val backgroundColor: Color? = null,
    val cornerRadius: Dp? = null
)
```

## Configuration Inheritance

### Base Config with Extensions

```swift
// Base configuration
struct BaseComponentConfig {
    var isEnabled: Bool = true
    var semantics: SemanticsConfig = SemanticsConfig()
}

// Extended configuration
struct ButtonConfig: BaseComponentConfig {
    // Inherited
    var isEnabled: Bool = true
    
    // Button-specific
    var style: ButtonStyle = .primary
    var size: ButtonSize = .medium
    var icon: String? = nil
}

struct CardConfig: BaseComponentConfig {
    // Inherited
    var isEnabled: Bool = true
    
    // Card-specific
    var elevation: Elevation = .medium
    var padding: EdgeInsets = .init(16)
    var variant: CardVariant = .outlined
}
```

```kotlin
// Base configuration
interface BaseComponentConfig {
    val isEnabled: Boolean
    val semantics: SemanticsConfig
}

// Extended configuration
data class ButtonConfig(
    override val isEnabled: Boolean = true,
    override val semantics: SemanticsConfig = SemanticsConfig(),
    val style: ButtonStyle = ButtonStyle.Primary,
    val size: ButtonSize = ButtonSize.Medium,
    val icon: Int? = null
) : BaseComponentConfig

data class CardConfig(
    override val isEnabled: Boolean = true,
    override val semantics: SemanticsConfig = SemanticsConfig(),
    val elevation: Dp = DesignElevation.medium,
    val padding: PaddingValues = PaddingValues(DesignSpacing.md),
    val variant: CardVariant = CardVariant.Outlined
) : BaseComponentConfig
```

## Builder Pattern for Complex Configs

```swift
// iOS - Configuration Builder
struct ButtonConfig {
    var style: ButtonStyle = .primary
    var size: ButtonSize = .medium
    var icon: String? = nil
    var isLoading: Bool = false
    
    struct Builder {
        private var config = ButtonConfig()
        
        func style(_ style: ButtonStyle) -> Builder {
            var copy = self
            copy.config.style = style
            return copy
        }
        
        func size(_ size: ButtonSize) -> Builder {
            var copy = self
            copy.config.size = size
            return copy
        }
        
        func icon(_ icon: String) -> Builder {
            var copy = self
            copy.config.icon = icon
            return copy
        }
        
        func loading(_ isLoading: Bool) -> Builder {
            var copy = self
            copy.config.isLoading = isLoading
            return copy
        }
        
        func build() -> ButtonConfig {
            return config
        }
    }
}

// Usage
let config = ButtonConfig.Builder()
    .style(.secondary)
    .size(.large)
    .icon("star.fill")
    .build()
```

```kotlin
// Android - DSL Builder
data class ButtonConfig(
    val style: ButtonStyle = ButtonStyle.Primary,
    val size: ButtonSize = ButtonSize.Medium,
    val icon: Int? = null,
    val isLoading: Boolean = false
)

fun buttonConfig(block: ButtonConfigBuilder.() -> Unit): ButtonConfig {
    return ButtonConfigBuilder().apply(block).build()
}

class ButtonConfigBuilder {
    var style: ButtonStyle = ButtonStyle.Primary
    var size: ButtonSize = ButtonSize.Medium
    var icon: Int? = null
    var isLoading: Boolean = false
    
    fun build(): ButtonConfig {
        return ButtonConfig(
            style = style,
            size = size,
            icon = icon,
            isLoading = isLoading
        )
    }
}

// Usage
val config = buttonConfig {
    style = ButtonStyle.Secondary
    size = ButtonSize.Large
    icon = R.drawable.ic_star
}
```

## Configuration Validation

```swift
// iOS - Config Validation
struct ButtonConfig {
    var style: ButtonStyle = .primary
    var size: ButtonSize = .medium
    
    func validated() -> ButtonConfig {
        var validated = self
        
        // Ensure loading state disables button
        if isLoading {
            validated.isEnabled = false
        }
        
        // Ensure minimum touch target
        if size.touchTarget < 44 {
            validated.size = .medium
        }
        
        return validated
    }
    
    var isValid: Bool {
        size.touchTarget >= 44
    }
}
```

```kotlin
// Android - Config Validation
data class ButtonConfig(
    val style: ButtonStyle = ButtonStyle.Primary,
    val size: ButtonSize = ButtonSize.Medium,
    val isLoading: Boolean = false,
    val isEnabled: Boolean = true
) {
    init {
        require(size.minHeight >= 36.dp) {
            "Button height must be at least 36dp for accessibility"
        }
    }
    
    val validated: ButtonConfig
        get() = copy(
            isEnabled = isEnabled && !isLoading
        )
    
    val isValid: Boolean
        get() = size.minHeight >= 36.dp
}
```

## Slot-Based Configuration

For components with multiple content areas:

```swift
// iOS - Slot Configuration
struct CardConfig {
    var header: HeaderConfig? = nil
    var body: BodyConfig = BodyConfig()
    var footer: FooterConfig? = nil
    var actions: [ActionConfig] = []
    
    struct HeaderConfig {
        var title: String
        var subtitle: String? = nil
        var icon: String? = nil
        var alignment: Alignment = .leading
    }
    
    struct BodyConfig {
        var padding: EdgeInsets = .init(16)
        var backgroundColor: Color? = nil
    }
    
    struct FooterConfig {
        var text: String
        var style: FooterStyle = .secondary
    }
    
    struct ActionConfig {
        var title: String
        var style: ActionStyle = .default
        var action: () -> Void
    }
}
```

```kotlin
// Android - Slot Configuration
data class CardConfig(
    val header: HeaderConfig? = null,
    val body: BodyConfig = BodyConfig(),
    val footer: FooterConfig? = null,
    val actions: List<ActionConfig> = emptyList()
) {
    data class HeaderConfig(
        val title: String,
        val subtitle: String? = null,
        val icon: Int? = null,
        val alignment: Alignment = Alignment.Start
    )
    
    data class BodyConfig(
        val padding: PaddingValues = PaddingValues(DesignSpacing.md),
        val backgroundColor: Color? = null
    )
    
    data class FooterConfig(
        val text: String,
        val style: FooterStyle = FooterStyle.Secondary
    )
    
    data class ActionConfig(
        val title: String,
        val style: ActionStyle = ActionStyle.Default,
        val onClick: () -> Unit
    )
}
```

## JSON Configuration for Runtime

```json
{
  "component": "button",
  "config": {
    "style": "primary",
    "size": "medium",
    "icon": "star",
    "isLoading": false,
    "semantics": {
      "label": "Favorite",
      "hint": "Add to favorites"
    }
  }
}
```

Parse and use at runtime:

```swift
// iOS
struct RuntimeConfig: Decodable {
    let component: String
    let config: ButtonConfig
    
    enum CodingKeys: String, CodingKey {
        case component
        case config
    }
}

let decoder = JSONDecoder()
let runtimeConfig = try decoder.decode(RuntimeConfig.self, from: jsonData)
```

```kotlin
// Android
@Serializable
data class RuntimeConfig(
    val component: String,
    val config: ButtonConfig
)

val json = Json { ignoreUnknownKeys = true }
val runtimeConfig = json.decodeFromString<RuntimeConfig>(jsonString)
```

## Configuration Migration

For versioned configurations:

```swift
// iOS - Config Versioning
struct ButtonConfig: Codable {
    let version: Int
    private let styleRaw: String
    private let sizeRaw: String
    
    enum CodingKeys: String, CodingKey {
        case version
        case styleRaw = "style"
        case sizeRaw = "size"
    }
    
    var style: ButtonStyle {
        ButtonStyle(rawValue: styleRaw) ?? .primary
    }
    
    init(version: Int = 1, style: ButtonStyle, size: ButtonSize) {
        self.version = version
        self.styleRaw = style.rawValue
        self.sizeRaw = size.rawValue
    }
    
    // Migrate old configs
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let version = try container.decodeIfPresent(Int.self, forKey: .version) ?? 1
        
        self.version = version
        self.styleRaw = try container.decode(String.self, forKey: .styleRaw)
        self.sizeRaw = try container.decode(String.self, forKey: .sizeRaw)
        
        // Apply migrations
        if version < 2 {
            migrateV1toV2()
        }
    }
}
```
