# Android Jetpack Compose Component Patterns

## Component Structure

```kotlin
// ConfigurableButton.kt
@Composable
fun ConfigurableButton(
    config: ButtonConfig = ButtonConfig(),
    text: String,
    modifier: Modifier = Modifier,
    icon: painterResource(id: R.drawable.ic_icon)? = null,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    val colors = config.style.colors
    val shapes = config.size.shapes
    val typography = config.typography
    
    Surface(
        modifier = modifier
            .defaultMinSize(minHeight = config.size.minHeight)
            .then(if (!config.isEnabled) Modifier.alpha(0.5f) else Modifier),
        shape = shapes.shape,
        color = if (config.isEnabled) colors.backgroundColor else colors.disabledBackgroundColor,
        enabled = config.isEnabled && !isLoading,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = config.size.horizontalPadding, vertical = config.size.verticalPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            contentPadding = config.size.contentPadding
        ) {
            // Icon
            icon?.let {
                Icon(
                    painter = it,
                    contentDescription = null,
                    tint = colors.foregroundColor,
                    modifier = Modifier.size(config.iconSize)
                )
                Spacer(modifier = Modifier.width(DesignSpacing.sm))
            }
            
            // Text
            Text(
                text = text,
                style = typography,
                color = colors.foregroundColor
            )
            
            // Loading indicator
            if (isLoading) {
                Spacer(modifier = Modifier.width(DesignSpacing.sm))
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = colors.foregroundColor,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}
```

## Configuration Types

```kotlin
// ButtonConfig.kt
data class ButtonConfig(
    val style: ButtonStyle = ButtonStyle.Primary,
    val size: ButtonSize = ButtonSize.Medium,
    val iconPosition: IconPosition = IconPosition.Start,
    val isEnabled: Boolean = true,
    val iconSize: Dp = 16.dp,
    val typography: TextStyle = MaterialTheme.typography.labelLarge
) {
    data class ButtonStyle(
        val backgroundColor: Color,
        val foregroundColor: Color,
        val disabledBackgroundColor: Color,
        val disabledForegroundColor: Color
    ) {
        companion object {
            val Primary = ButtonStyle(
                backgroundColor = DesignColors.primary,
                foregroundColor = Color.White,
                disabledBackgroundColor = DesignColors.primary.copy(alpha = 0.38f),
                disabledForegroundColor = Color.White.copy(alpha = 0.38f)
            )
            
            val Secondary = ButtonStyle(
                backgroundColor = DesignColors.surface,
                foregroundColor = DesignColors.primary,
                disabledBackgroundColor = DesignColors.surface.copy(alpha = 0.38f),
                disabledForegroundColor = DesignColors.textPrimary.copy(alpha = 0.38f)
            )
            
            val Tertiary = ButtonStyle(
                backgroundColor = Color.Transparent,
                foregroundColor = DesignColors.primary,
                disabledBackgroundColor = Color.Transparent,
                disabledForegroundColor = DesignColors.textPrimary.copy(alpha = 0.38f)
            )
            
            val Destructive = ButtonStyle(
                backgroundColor = DesignColors.error,
                foregroundColor = Color.White,
                disabledBackgroundColor = DesignColors.error.copy(alpha = 0.38f),
                disabledForegroundColor = Color.White.copy(alpha = 0.38f)
            )
        }
    }
    
    data class ButtonSize(
        val shape: Shape,
        val horizontalPadding: Dp,
        val verticalPadding: Dp,
        val contentPadding: PaddingValues,
        val minHeight: Dp
    ) {
        companion object {
            val Small = ButtonSize(
                shape = RoundedCornerShape(DesignCornerRadius.small),
                horizontalPadding = DesignSpacing.md,
                verticalPadding = DesignSpacing.sm,
                contentPadding = PaddingValues(horizontal = DesignSpacing.md),
                minHeight = 32.dp
            )
            
            val Medium = ButtonSize(
                shape = RoundedCornerShape(DesignCornerRadius.medium),
                horizontalPadding = DesignSpacing.lg,
                verticalPadding = DesignSpacing.md,
                contentPadding = PaddingValues(horizontal = DesignSpacing.lg),
                minHeight = 44.dp
            )
            
            val Large = ButtonSize(
                shape = RoundedCornerShape(DesignCornerRadius.medium),
                horizontalPadding = DesignSpacing.xl,
                verticalPadding = DesignSpacing.lg,
                contentPadding = PaddingValues(horizontal = DesignSpacing.xl),
                minHeight = 56.dp
            )
        }
    }
    
    enum class IconPosition { Start, End }
}
```

## Composition Local Configuration

```kotlin
// ComponentCompositionLocals.kt
data class ComponentConfig(
    val buttonStyle: ButtonConfig.ButtonStyle = ButtonConfig.ButtonStyle.Primary,
    val buttonSize: ButtonConfig.ButtonSize = ButtonConfig.ButtonSize.Medium,
    val animationDuration: Int = 300
)

val LocalComponentConfig = compositionLocalOf { ComponentConfig() }

// Usage in component
@Composable
fun ConfigurableButton(
    config: ButtonConfig? = null,
    text: String,
    onClick: () -> Unit
) {
    val environment = LocalComponentConfig.current
    
    val effectiveConfig = config ?: ButtonConfig(
        style = environment.buttonStyle,
        size = environment.buttonSize
    )
    
    // Use effectiveConfig...
}

// Set at composition
CompositionLocalProvider(
    LocalComponentConfig provides ComponentConfig(
        buttonStyle = ButtonConfig.ButtonStyle.Secondary,
        buttonSize = ButtonConfig.ButtonSize.Large
    )
) {
    Content()
}
```

## Component Variants with Defaults

```kotlin
// ButtonDefaults.kt
object ButtonDefaults {
    @Composable
    fun PrimaryButton(
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        ConfigurableButton(
            config = ButtonConfig(
                style = ButtonConfig.ButtonStyle.Primary,
                isEnabled = enabled
            ),
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    }
    
    @Composable
    fun SecondaryButton(
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        ConfigurableButton(
            config = ButtonConfig(
                style = ButtonConfig.ButtonStyle.Secondary,
                isEnabled = enabled
            ),
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    }
    
    @Composable
    fun DestructiveButton(
        text: String,
        modifier: Modifier = Modifier,
        enabled: Boolean = true,
        onClick: () -> Unit
    ) {
        ConfigurableButton(
            config = ButtonConfig(
                style = ButtonConfig.ButtonStyle.Destructive,
                isEnabled = enabled
            ),
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    }
    
    @Composable
    fun LoadingButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit
    ) {
        ConfigurableButton(
            config = ButtonConfig(isLoading = true),
            text = text,
            modifier = modifier,
            onClick = onClick
        )
    }
}

// Usage
ButtonDefaults.PrimaryButton("Submit") { }
ButtonDefaults.SecondaryButton("Cancel") { }
```

## Modifier Extension Functions

```kotlin
// ButtonModifierExtensions.kt
fun Modifier.buttonStyle(
    style: ButtonConfig.ButtonStyle
): Modifier = this.then(
    Modifier
        .background(
            color = style.backgroundColor,
            shape = RoundedCornerShape(DesignCornerRadius.medium)
        )
)

fun Modifier.buttonSize(
    size: ButtonConfig.ButtonSize
): Modifier = this.then(
    Modifier
        .defaultMinSize(minHeight = size.minHeight)
        .padding(horizontal = size.horizontalPadding, vertical = size.verticalPadding)
)

// Usage
Text("Submit")
    .modifier(
        Modifier
            .buttonStyle(ButtonConfig.ButtonStyle.Primary)
            .buttonSize(ButtonConfig.ButtonSize.Large)
            .clickable { }
    )
```

## State Management in Components

```kotlin
// Button with internal state
@Composable
fun ConfigurableButton(
    config: ButtonConfig = ButtonConfig(),
    text: String,
    modifier: Modifier = Modifier,
    onClick: suspend () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    
    LaunchedEffect(isLoading) {
        if (isLoading) {
            try {
                onClick()
            } catch (e: Exception) {
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
    
    ConfigurableButton(
        config = config.copy(
            isLoading = isLoading,
            isEnabled = config.isEnabled && !isLoading
        ),
        text = text,
        modifier = modifier,
        onClick = { isLoading = true }
    )
}
```

## Preview with Configurations

```kotlin
// ConfigurableButtonPreview.kt
@Preview(showBackground = true)
@Composable
fun ButtonStylesPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(DesignSpacing.md)) {
        ConfigurableButton(
            config = ButtonConfig(style = ButtonConfig.ButtonStyle.Primary),
            text = "Primary",
            onClick = {}
        )
        ConfigurableButton(
            config = ButtonConfig(style = ButtonConfig.ButtonStyle.Secondary),
            text = "Secondary",
            onClick = {}
        )
        ConfigurableButton(
            config = ButtonConfig(style = ButtonConfig.ButtonStyle.Tertiary),
            text = "Tertiary",
            onClick = {}
        )
        ConfigurableButton(
            config = ButtonConfig(style = ButtonConfig.ButtonStyle.Destructive),
            text = "Destructive",
            onClick = {}
        )
    }
    .padding(DesignSpacing.lg)
}

@Preview
@Composable
fun ButtonSizesPreview() {
    Column(verticalArrangement = Arrangement.spacedBy(DesignSpacing.md)) {
        ConfigurableButton(
            config = ButtonConfig(size = ButtonConfig.ButtonSize.Small),
            text = "Small",
            onClick = {}
        )
        ConfigurableButton(
            config = ButtonConfig(size = ButtonConfig.ButtonSize.Medium),
            text = "Medium",
            onClick = {}
        )
        ConfigurableButton(
            config = ButtonConfig(size = ButtonConfig.ButtonSize.Large),
            text = "Large",
            onClick = {}
        )
    }
    .padding(DesignSpacing.lg)
}

@Preview
@Composable
fun LoadingButtonPreview() {
    ConfigurableButton(
        config = ButtonConfig(isLoading = true),
        text = "Loading",
        onClick = {}
    )
    .padding(DesignSpacing.lg)
}
```
