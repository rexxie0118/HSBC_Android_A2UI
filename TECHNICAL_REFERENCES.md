# Technical References

## Component Specifications

### Core Component Properties
```kotlin
// Base component structure
data class ComponentConfig(
    val id: String,
    val type: String,
    val sectionId: String = "",
    val properties: ComponentProperties? = null,
    val action: ActionConfig? = null,
    val dependencies: FieldDependencies? = null,
    val validation: ValidationConfig? = null,
    val children: ChildrenSpecification? = null
)

data class ComponentProperties(
    val text: TextValue? = null,
    val color: String? = null,
    val textFieldType: TextFieldType? = null,
    val keyboardType: KeyboardType? = null,
    val placeholder: TextValue? = null,
    val label: TextValue? = null,
    val icon: String? = null,
    val image: String? = null,
    val usageHint: String? = null, // Maps to typography (h1, h2, body, caption, etc.)
    val shape: String? = null, // For card shapes, button styles, etc.
    val dataBinding: String? = null,
    val child: String? = null // Reference to other component IDs
)

// Text representation with flexible binding
sealed class TextValue {
    data class LiteralString(val literalString: String) : TextValue()
    data class Binding(val binding: String) : TextValue()
    data class LiteralNumber(val literalNumber: Number) : TextValue()
}

// Children specification for lists and containers
data class ChildrenSpecification(
    val explicitList: List<String>? = null,     // Direct component references
    val template: ChildrenTemplate? = null      // List template configuration
)

// List template for dynamic list iteration
data class ChildrenTemplate(
    val dataBinding: String,      // Path to array data("$.users")
    val componentId: String,      // Template component ID
    val itemVar: String? = null   // Item variable name (defaults to "item")
)
```

### Validation Specification
```kotlin
data class ValidationConfig(
    val required: Boolean? = null,
    val rules: List<ValidationRule>? = null,
    val customValidation: CustomValidation? = null
)

sealed class ValidationRule {
    data class Pattern(
        val pattern: String,
        val message: TextValue
    ) : ValidationRule()
    
    data class MinLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()
    
    data class MaxLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()
    
    data class MinValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()
    
    data class MaxValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()
    
    data class Required(
        val message: TextValue
    ) : ValidationRule()
    
    data class Custom(
        val nativeFunction: String,
        val parameters: List<String>
    ) : ValidationRule()
    
    data class CrossField(
        val expression: String,
        val message: TextValue
    ) : ValidationRule()
}
```

### Field Dependencies Specification
```kotlin
data class FieldDependencies(
    val visible: VisibilityRule? = null,
    val enabled: EnabledRule? = null,
    val required: RequiredRule? = null,
    val value: ValueRule? = null,
    val options: OptionsRule? = null
)

data class VisibilityRule(
    val rule: String,      // Expression to evaluate
    val action: String = "hide" // "hide" or "disable"
)

data class EnabledRule(
    val rule: String
)

data class RequiredRule(
    val rule: String
)

data class ValueRule(
    val rule: String,      // Expression that returns new value
    val transform: String? = null // Optional transform function
)

data class OptionsRule(
    val rule: String,      // Expression that returns options list
    val source: String? = null // Data source for options
)
```

### Theme Configuration
```kotlin
data class Theme(
    val id: String,
    val name: String,
    val mode: String, // "light" or "dark"
    val colors: Map<String, String>,
    val typography: Map<String, TypographyDefinition>,
    val shadows: Map<String, ShadowConfig>
)

data class TypographyDefinition(
    val fontSize: Double,
    val fontWeight: FontWeight,
    val fontFamily: String,
    val lineHeight: Double
)

data class ShadowConfig(
    val color: String = "#000000",
    val alpha: Float = 0.1f,
    val blur: Int = 8,
    val offsetX: Int = 0,
    val offsetY: Int = 2
)
```

## Expression Language Syntax

### Supported Operators
| Category | Operators | Description |
|----------|-----------|-------------|
| Comparison | ==, !=, <, >, <=, >= | Equality and relational operations |
| Logical | &&, \|\|, ! | AND, OR, NOT operations |
| Arithmetic | +, -, *, / | Basic math operations |
| String | contains, startsWith, endsWith | String manipulation |

### Example Expressions
```kotlin
// Property access
"$.user.name"
"$.products.0.name"

// Comparisons
"$.payment_method.value == 'card'"
"$.amount.value > 100"

// Logical expressions
"$.terms_accepted.value == true && $.email.validated == true"

// String functions
"$.username.value.length() >= 3"
"$.country.value.contains('US')"

// Complex expressions
"($.user_type.value == 'business' && $.business_name.exists()) || ($.user_type.value == 'individual' && $.first_name.exists())"
```

## Builtin Native Functions

### Validation Functions
```kotlin
// Email validation
NativeFunctionRegistry.register("validateEmail") { params ->
    val email = params[0] as? String ?: return@register false
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

// Phone validation
NativeFunctionRegistry.register("validatePhone") { params ->
    val phone = params[0] as? String ?: return@register false
    android.util.Patterns.PHONE.matcher(phone).matches()
}

// Domain validation
NativeFunctionRegistry.register("validateEmailDomain") { params ->
    val email = params[0] as? String ?: return@register false
    val domain = email.substringAfter("@")
    domain !in listOf("tempmail.com", "throwaway.com")
}
```

### Formatting Functions
```kotlin
// Currency formatting
NativeFunctionRegistry.register("formatCurrency") { params ->
    val amount = params[0] as? Double ?: return@register ""
    val currency = params[1] as? String ?: "USD"
    NumberFormat.getCurrencyInstance().apply {
        currency = Currency.getInstance(currency)
    }.format(amount)
}

// Date formatting
NativeFunctionRegistry.register("formatDate") { params ->
    val timestamp = params[0] as? Long ?: return@register ""
    val format = params[1] as? String ?: "yyyy-MM-dd"
    SimpleDateFormat(format, Locale.getDefault()).format(Date(timestamp))
}
```

### Calculation Functions
```kotlin
// Tax calculation
NativeFunctionRegistry.register("calculateTax") { params ->
    val amount = params[0] as? Double ?: return@register 0.0
    val rate = params[1] as? Double ?: 0.0
    amount * rate
}

// Discount calculation
NativeFunctionRegistry.register("calculateDiscount") { params ->
    val amount = params[0] as? Double ?: return@register 0.0
    val percent = params[1] as? Double ?: 0.0
    amount * (percent / 100.0)
}
```

## UsageHint to Typography Mapping

| usageHint | Material3 Typography | Theme Config |
|-----------|---------------------|--------------|
| `h1` | displayLarge | typography.h1 |
| `h2` | displayMedium | typography.h2 |
| `h3` | displaySmall | typography.h3 |
| `h4` | headlineLarge | typography.h4 |
| `h5` | headlineMedium | typography.h5 |
| `h6` | headlineSmall | typography.h6 |
| `body` | bodyLarge | typography.body1 |
| `body2` | bodyMedium | typography.body2 |
| `caption` | bodySmall | typography.caption |
| `overline` | labelMedium | typography.overline |
| `button` | labelLarge | typography.button |

## Security Policies

### Allowed Operations
- Component property access (`$.path.property`)
- Comparison operations (`==`, `! =`, `<`, `>`, etc.)
- Logical operations (`&&`, `||`, `!`)
- String functions (length, contains, etc.)
- Allowed native functions only

### Blocked Operations
| Operation Type | Examples |
|---------------|----------|
| Code evaluation | `eval()`, `function()`, `=>` |
| System access | `System.exit`, `Runtime.getRuntime` |
| File access | `FileWriter`, `ProcessBuilder` |
| Network access | `Socket`, `URL.openConnection` |
| Script injection | `<script>`, `javascript:`, `onclick=` |

## Supported Components

### Content Components
- `Text` - Displays text with typography styles
- `Image` - Displays images from URLs or resources
- `Icon` - Displays material icons
- `Video` - Video player component

### Layout Components
- `Row` - Horizontal arrangement of components
- `Column` - Vertical arrangement of components
- `List` - Scrollable list of items
- `Card` - Elevated container with content
- `Tabs` - Tabbed interface components
- `Modal` - Overlay modal/popup

### Interactive Components
- `Button` - Pressable button with actions
- `TextField` - Text input field with validation
- `CheckBox` - Toggle selection component
- `DateTimeInput` - Date/time picker
- `MultipleChoice` - Radio/checkbox group
- `Slider` - Numeric range selector

### Utility Components
- `Divider` - Separation line
- `Spacer` - White space element
- `Box` - Generic container element

## Performance Metrics

### Target Benchmarks
| Metric | Target | Measurement |
|--------|--------|-------------|
| **Frame Time** | < 16ms (60fps) | `adb shell dumpsys gfxinfo` |
| **List Scroll FPS** | > 55fps | Profiler |
| **Cold Startup** | < 2s | `adb shell am start -W` |
| **First Content Paint** | < 100ms | Performance Monitor |
| **Theme Switch** | < 100ms | Custom benchmark |
| **Binding Resolve (cached)** | < 1ms | Unit test |
| **Memory Usage** | < 100MB | `adb shell dumpsys meminfo` |
| **GC Frequency** | < 1/min | Profiler |

### Memory Management Targets
| Mode | Update Frequency | Battery Usage |
|------|------------------|---------------|
| Active | Real-time | 100% |
| Idle | 1/min | 20% |
| Background | Paused | 5% |

## Journey Configuration

### Journey Structure
```json
{
  "type": "journey",
  "id": "banking_journey",
  "name": "HSBC Banking App",
  "pages": [
    "homepage",
    "wealth_page",
    "transfer_page",
    "settings_page"
  ],
  "defaultPage": "homepage",
  "navigation": {
    "allowBack": true,
    "allowForward": true,
    "preserveState": true,
    "transition": "slide"
  },
  "analytics": {
    "trackPageViews": true,
    "trackUserFlow": true
  }
}
```

### Multi-Domain JSON Configuration
```json
{
  "routes": {
    "/api/v2/accounts/*": {
      "class": "AccountDataProcessor",
      "fieldMapping": {
        "accountId": "account_id",      // API sends 'account_id', we use 'accountId'
        "balance": "current_balance",   // API sends 'current_balance', we use 'balance'
        "owner": "customer_name",       // Different naming convention
        "type": "account_type"
      },
      "cachingStrategy": "memory_first",
      "syncInterval": 30000
    },
    "/api/v3/users/*": {
      "class": "UserDataProcessor",
      "idPath": "$.user.id",
      "namePath": "$.user.profile.name",
      "emailPath": "$.user.profile.contact.email",
      "avatarPath": "$.user.assets.avatar.url"
    },
    "/api/products/*": {
      "class": "ProductDataProcessor",
      "transformationLogic": "price_calculator",
      "validation": ["required_fields", "type_check"]
    }
  },
  "globalConfig": {
    "defaultTimeout": 10000,
    "retryPolicy": {
      "maxRetries": 3,
      "backoffMultiplier": 1.5
    },
    "cache": {
      "enabled": true,
      "ttl": 3600000  // 1 hour
    }
  }
}
```

## Data Model Classes

### Domain Models
```kotlin
data class AccountModel(
    val id: String,
    val balance: Double,
    val owner: String,
    val type: String = "GENERIC",
    val cachingStrategy: String = "memory_first",
    val nextSyncTime: Long = 0L,
    val timestamp: Long = 0L
)

data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val avatarUrl: String? = null
)
```

## Android Specific

### Compose Integration
```kotlin
// Theme provider
@Composable
fun A2UIRendererTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = themeFlow.collectAsState(initial = defaultTheme).value?.getColors()
        ?: defaultDarkColors
    
    val typography = MaterialTheme.typography
    
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        content = content
    )
}
```

### Navigation Structure
```kotlin
sealed class Screen(val route: String) {
    object Homepage : Screen("homepage")
    object Wealth : Screen("wealth")
    object Transfer : Screen("transfer")
    object Settings : Screen("settings")
}
```

## Testing Approach

### Security Tests
```kotlin
@Test
fun `unauthorized component should be blocked`() {
    val malicious = ComponentConfig(type = "EvilScript", id = "x", sectionId = "")
    val result = ComponentWhitelist.validate(listOf(malicious))
    assertTrue(result is SecurityResult.Failure)
}

@Test
fun `script injection should be blocked`() {
    val malicious = "<script>alert('XSS')</script>"
    assertTrue(ScriptBlocker.containsScript(malicious))
}

@Test
fun `function call in expression should be blocked`() {
    val malicious = "$.user.constructor.constructor('alert(1)')()"
    val result = AndroidExpressionParser.parse(malicious)
    assertTrue(result is SecurityResult.Failure)
}
```

### Performance Tests
```kotlin
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmark {
    
    @get:Rule
    val benchmarkRule = BenchmarkRule()
    
    @Test
    fun benchmarkListScrollPerformance() {
        benchmarkRule.measureRepeated {
            lazyListState.scrollToItem(100)
        }
    }
    
    @Test
    fun benchmarkThemeSwitch() {
        benchmarkRule.measureRepeated {
            ConfigManager.setTheme("banking_dark")
            Thread.sleep(50)
        }
    }
}
```