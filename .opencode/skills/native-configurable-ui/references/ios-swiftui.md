# iOS SwiftUI Component Patterns

## Component Structure

```swift
// ConfigurableButton.swift
import SwiftUI

struct ConfigurableButton: View {
    // Configuration
    let config: ButtonConfig
    
    // Content
    let title: LocalizedStringKey
    let icon: String?
    let action: () -> Void
    
    // Computed properties from config
    private var backgroundColor: Color { config.style.backgroundColor }
    private var foregroundColor: Color { config.style.foregroundColor }
    private var cornerRadius: CGFloat { config.size.cornerRadius }
    private var horizontalPadding: CGFloat { config.size.horizontalPadding }
    private var verticalPadding: CGFloat { config.size.verticalPadding }
    private var minHeight: CGFloat { config.size.minHeight }
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: DesignSpacing.sm) {
                if let icon = icon {
                    Image(systemName: icon)
                        .font(.system(size: config.iconSize))
                }
                
                Text(title)
                    .font(config.typography)
                
                if config.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: foregroundColor))
                }
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, horizontalPadding)
            .padding(.vertical, verticalPadding)
            .frame(minHeight: minHeight)
            .background(backgroundColor)
            .foregroundColor(foregroundColor)
            .cornerRadius(cornerRadius)
            .opacity(config.isEnabled ? 1.0 : 0.5)
            .disabled(!config.isEnabled || config.isLoading)
        }
    }
}
```

## Configuration Types

```swift
// ButtonConfig.swift
struct ButtonConfig {
    var style: ButtonStyle = .primary
    var size: ButtonSize = .medium
    var icon: String? = nil
    var iconPosition: IconPosition = .leading
    var isLoading: Bool = false
    var isEnabled: Bool = true
    var iconSize: CGFloat = 16
    
    enum ButtonStyle {
        case primary
        case secondary
        case tertiary
        case destructive
        
        var backgroundColor: Color {
            switch self {
            case .primary: return DesignColors.primary
            case .secondary: return DesignColors.surface
            case .tertiary: return Color.clear
            case .destructive: return DesignColors.error
            }
        }
        
        var foregroundColor: Color {
            switch self {
            case .primary, .destructive: return .white
            case .secondary: return DesignColors.primary
            case .tertiary: return DesignColors.textPrimary
            }
        }
    }
    
    enum ButtonSize {
        case small
        case medium
        case large
        
        var cornerRadius: CGFloat {
            switch self {
            case .small: return DesignCornerRadius.small
            case .medium, .large: return DesignCornerRadius.medium
            }
        }
        
        var horizontalPadding: CGFloat {
            switch self {
            case .small: return DesignSpacing.md
            case .medium: return DesignSpacing.lg
            case .large: return DesignSpacing.xl
            }
        }
        
        var verticalPadding: CGFloat {
            switch self {
            case .small: return DesignSpacing.sm
            case .medium: return DesignSpacing.md
            case .large: return DesignSpacing.lg
            }
        }
        
        var minHeight: CGFloat {
            switch self {
            case .small: return 32
            case .medium: return 44
            case .large: return 56
            }
        }
    }
    
    enum IconPosition {
        case leading
        case trailing
    }
}
```

## Environment-Based Configuration

```swift
// ComponentEnvironment.swift
struct ComponentEnvironment {
    var buttonStyle: ButtonConfig.ButtonStyle = .primary
    var buttonSize: ButtonConfig.ButtonSize = .medium
    var animationDuration: Double = 0.3
}

private struct ComponentEnvironmentKey: EnvironmentKey {
    static let defaultValue = ComponentEnvironment()
}

extension EnvironmentValues {
    var componentConfig: ComponentEnvironment {
        get { self[ComponentEnvironmentKey.self] }
        set { self[ComponentEnvironmentKey.self] = newValue }
    }
}

// Usage in component
struct ConfigurableButton: View {
    @Environment(\.componentConfig) var environment
    
    var body: some View {
        // Use environment defaults if not explicitly set
        let style = config?.style ?? environment.buttonStyle
        let size = config?.size ?? environment.buttonSize
    }
}

// Set at view hierarchy
ContentView()
    .environment(\.componentConfig, ComponentEnvironment(
        buttonStyle: .secondary,
        buttonSize: .large
    ))
```

## Component Variants

```swift
// ButtonVariants.swift
extension ConfigurableButton {
    // Primary button
    static func primary(
        _ title: LocalizedStringKey,
        icon: String? = nil,
        action: @escaping () -> Void
    ) -> ConfigurableButton {
        ConfigurableButton(
            config: ButtonConfig(style: .primary),
            title: title,
            icon: icon,
            action: action
        )
    }
    
    // Destructive button
    static func destructive(
        _ title: LocalizedStringKey,
        action: @escaping () -> Void
    ) -> ConfigurableButton {
        ConfigurableButton(
            config: ButtonConfig(style: .destructive),
            title: title,
            action: action
        )
    }
    
    // Loading button
    static func loading(
        _ title: LocalizedStringKey
    ) -> ConfigurableButton {
        ConfigurableButton(
            config: ButtonConfig(isLoading: true),
            title: title,
            action: {}
        )
    }
}

// Usage
ConfigurableButton.primary("Submit", icon: "checkmark") {
    // Action
}

ConfigurableButton.destructive("Delete") {
    // Action
}
```

## Protocol-Based Configuration

```swift
// ConfigurableComponent.swift
protocol ConfigurableComponent {
    associatedtype ConfigType
    associatedtype ContentView: View
    
    var config: ConfigType { get }
    @ViewBuilder var contentView: ContentView { get }
}

extension ConfigurableComponent {
    var body: some View {
        contentView
            .modifier(ComponentModifier(config: config))
    }
}

// Usage
struct ConfigurableCard: View, ConfigurableComponent {
    typealias ConfigType = CardConfig
    typealias ContentView = CardContent
    
    let config: CardConfig
    let title: String
    let subtitle: String
    
    var contentView: CardContent {
        CardContent(title: title, subtitle: subtitle)
    }
}
```

## Modifier-Based Configuration

```swift
// ComponentModifiers.swift
struct ButtonStyleModifier: ViewModifier {
    let style: ButtonConfig.ButtonStyle
    
    func body(content: Content) -> some View {
        content
            .background(style.backgroundColor)
            .foregroundColor(style.foregroundColor)
    }
}

struct ButtonSizeModifier: ViewModifier {
    let size: ButtonConfig.ButtonSize
    
    func body(content: Content) -> some View {
        content
            .padding(.horizontal, size.horizontalPadding)
            .padding(.vertical, size.verticalPadding)
            .frame(minHeight: size.minHeight)
    }
}

// Usage
Button("Submit") {
    // Action
}
.modifier(ButtonStyleModifier(style: .primary))
.modifier(ButtonSizeModifier(size: .large))
```

## Preview with Configurations

```swift
// ConfigurableButton+Preview.swift
#if DEBUG
struct ConfigurableButton_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            // All styles
            VStack(spacing: DesignSpacing.md) {
                ConfigurableButton.primary("Primary") {}
                ConfigurableButton.secondary("Secondary") {}
                ConfigurableButton.tertiary("Tertiary") {}
                ConfigurableButton.destructive("Destructive") {}
            }
            .previewDisplayName("Styles")
            
            // All sizes
            VStack(spacing: DesignSpacing.md) {
                ConfigurableButton.primary("Small", icon: "star") {}
                    .config(ButtonConfig(size: .small))
                ConfigurableButton.primary("Medium", icon: "star") {}
                    .config(ButtonConfig(size: .medium))
                ConfigurableButton.primary("Large", icon: "star") {}
                    .config(ButtonConfig(size: .large))
            }
            .previewDisplayName("Sizes")
            
            // Loading state
            ConfigurableButton.primary("Submitting") {}
                .config(ButtonConfig(isLoading: true))
                .previewDisplayName("Loading")
        }
        .padding()
    }
}
#endif
```
