# A2UI Renderer - Architecture Documentation

## Overview

This document defines the complete architecture of the A2UI Renderer, including data flow, component structure, state management, and navigation patterns.

---

## Architecture Decision

> **Local-First Configuration**: Server communication is intentionally **LOW PRIORITY**. All configuration is stored locally in `res/raw/*.jsonl` files. This is a valid implementation choice for offline-first or embedded renderer scenarios.

---

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    JSON Configuration Files                      │
│  themes.jsonl │ global_settings.jsonl │ sections/*.jsonl        │
└────────────────────┬────────────────────────────────────────────┘
                     │ Load at init
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ConfigManager (Singleton)                   │
│  ┌────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ themes: Map    │  │ uiConfig: UIConfig│  │ preferences     │ │
│  │ allComponents  │  │ globalSettings   │  │ PreferencesMgr  │ │
│  └────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ themeFlow: StateFlow<Theme?>                             │  │
│  │ • Emits when theme changes                               │  │
│  │ • Observed by A2UIRendererTheme                          │  │
│  │ • Persists to SharedPreferences                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────┬──────────────────────────────────────────────────┘
               │ themeFlow.collectAsState()
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                   A2UIRendererTheme (Composable)                │
│  • Observes themeFlow                                          │
│  • Builds ColorScheme from theme.colors                        │
│  • Builds Typography from theme.typography                     │
│  • Provides MaterialTheme to children                          │
└──────────────┬──────────────────────────────────────────────────┘
               │ MaterialTheme.current
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Component Renderers                           │
│  renderText() │ renderCard() │ renderButton() │ renderList()   │
│  • Access MaterialTheme.colorScheme                            │
│  • Access MaterialTheme.typography                             │
│  • Resolve bindings via BindingResolver                        │
│  • Apply validation rules                                      │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│              DataModelStore + BindingResolver                    │
│  ┌──────────────────┐         ┌─────────────────────────────┐  │
│  │ _data:StateFlow  │◄───────►│ resolve("$.user.name")      │  │
│  │ setData()        │         │ updateAtPath()              │  │
│  │ getAtPath()      │         │ resolveText()               │  │
│  └──────────────────┘         └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Core Components

### ConfigManager

**Purpose**: Central configuration loader and state manager

**Responsibilities**:
- Load JSON configuration from `res/raw/*.jsonl`
- Manage theme state via `StateFlow<Theme?>`
- Provide component lookup by ID
- Handle theme persistence via `PreferencesManager`

**Key Properties**:
```kotlin
object ConfigManager {
    private var themes: Map<String, Theme> = emptyMap()
    private var allComponents: Map<String, ComponentConfig> = emptyMap()
    private var currentTheme: Theme? = null
    
    private val _themeFlow = MutableStateFlow<Theme?>(null)
    val themeFlow: StateFlow<Theme?> = _themeFlow.asStateFlow()
    
    private var preferencesManager: PreferencesManager? = null
}
```

**Key Methods**:
```kotlin
fun init(context: Context)
fun getCurrentTheme(): Theme?
fun setTheme(themeId: String)
fun getComponent(componentId: String): ComponentConfig?
fun resolveColor(token: String?): String?
fun resolveSpacing(token: String?): Dp
```

---

### A2UIRendererTheme

**Purpose**: Theme provider composable that observes `themeFlow` and provides MaterialTheme

**Implementation**:
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
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFD32F2F),
            secondary = Color(0xFF1976D2),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF),
        )
    }
    
    val typography = currentTheme?.typography?.toMaterialTypography() ?: Typography()
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
```

---

### Component Renderers

**Location**: `app/src/main/java/com/a2ui/renderer/renderer/ComponentRenderer.kt`

**Purpose**: Render components based on JSON configuration

**Supported Components**:
- Content: Text, Image, Icon
- Layout: Column, Row, Card, Tabs
- Interactive: Button, TextField, CheckBox
- Utility: Divider, Spacer, Box

**Render Function Signature**:
```kotlin
@Composable
fun renderComponent(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
)
```

---

### DataModelStore

**Location**: `app/src/main/java/com/a2ui/renderer/binding/DataModelStore.kt`

**Purpose**: Reactive runtime data storage

**Key Features**:
- Stores nested JSON data structures
- Provides path-based access (`$.user.name`)
- Emits changes via `StateFlow<Map<String, Any>>`
- Supports array indices (`$.products.0.name`)

**Implementation**:
```kotlin
class DataModelStore {
    private val _data = MutableStateFlow<Map<String, Any>>(emptyMap())
    val data: StateFlow<Map<String, Any>> = _data.asStateFlow()
    
    fun setData(newData: Map<String, Any>)
    fun updateAtPath(path: String, value: Any?)
    fun getAtPath(path: String): Any?
    fun mergeData(newData: Map<String, Any>)
    fun clear()
}
```

---

### BindingResolver

**Location**: `app/src/main/java/com/a2ui/renderer/binding/BindingResolver.kt`

**Purpose**: Resolve `$.path` expressions to actual values

**Key Methods**:
```kotlin
object BindingResolver {
    fun resolve(path: String, dataModel: DataModelStore): Any?
    fun resolveText(textValue: TextValue?, dataModel: DataModelStore): String
    fun resolveColor(colorRef: String?, dataModel: DataModelStore): String?
    fun resolveProperties(properties: ComponentProperties?, dataModel: DataModelStore): ComponentProperties?
}
```

---

### ValidationEngine

**Location**: `app/src/main/java/com/a2ui/renderer/rules/ValidationEngine.kt`

**Purpose**: Validate input components based on JSON rules

**Supported Rule Types**:
- `required` - Field must not be empty
- `pattern` - Regex pattern validation
- `minLength` / `maxLength` - Length validation
- `minValue` / `maxValue` - Numeric validation
- `email` / `phone` - Format validation
- `crossField` - Cross-field validation with expressions
- `customValidation` - Native function validation

**Usage**:
```kotlin
val errors = ValidationEngine.validateField(component, dataModel)
if (errors.isNotEmpty()) {
    showError(errorMessage = errors.first().message)
}
```

---

### DependencyResolver

**Location**: `app/src/main/java/com/a2ui/renderer/rules/DependencyResolver.kt`

**Purpose**: Resolve field dependencies (visibility, enabled, required, etc.)

**Dependency Types**:
- `visible` - Show/hide field based on expression
- `enabled` - Enable/disable field
- `required` - Make required conditionally
- `value` - Transform field value
- `options` - Filter options list

**Optimization**:
- Builds dependency graph for efficient updates
- Batches dependency updates (50ms debounce)
- Supports transitive dependencies

---

### ListTemplateRenderer

**Location**: `app/src/main/java/com/a2ui/renderer/renderer/ListTemplateRenderer.kt`

**Purpose**: Render dynamic lists from templates

**Template Configuration**:
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

**Features**:
- LazyColumn for vertical lists
- LazyRow for horizontal lists
- Item-scoped data models
- Efficient diffing and recycling

---

## Data Flow Patterns

### Theme Switching Flow

```
User clicks theme toggle
        ↓
ThemeToggleButton.onClick()
        ↓
ConfigManager.setTheme("banking_dark")
        ↓
├─► Update currentTheme
├─► preferencesManager.setSelectedTheme()
└─► _themeFlow.value = newTheme  ◄── Emits!
        ↓
A2UIRendererTheme collects change
        ↓
Recompose with new ColorScheme
        ↓
All composables using MaterialTheme rebuild
        ↓
UI updated with dark theme colors
```

**Implementation**:
```kotlin
@Composable
fun ThemeToggleButton() {
    val currentTheme = ConfigManager.themeFlow.collectAsState(initial = null).value
    val isDarkTheme = currentTheme?.mode == "dark"
    
    IconButton(onClick = {
        val newTheme = if (isDarkTheme) "banking_light" else "banking_dark"
        ConfigManager.setTheme(newTheme)
    }) {
        Icon(
            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
            contentDescription = "Toggle theme"
        )
    }
}
```

---

### Data Binding Flow

```
Agent provides data JSON
        ↓
DataModelStore.setData()
        ↓
├─► _data.value = newData
└─► Emits via StateFlow
        ↓
Component renders with binding
        ↓
BindingResolver.resolve("$.products.0.name")
        ↓
├─► Parse path: ["products", "0", "name"]
├─► Traverse: data["products"][0]["name"]
└─► Return: "Widget"
        ↓
Text component displays "Widget"
```

**Implementation**:
```kotlin
@Composable
fun renderText(component: ComponentConfig) {
    val textValue = component.properties?.text ?: return
    val text = BindingResolver.resolveText(textValue, dataModel)
    
    Text(text = text)
}
```

---

### List Template Flow

```
Component has children.template
        ↓
ListTemplateRenderer.RenderList()
        ↓
├─► getListData("$.products") → List<Map>
├─► For each item in list:
│   ├─► Create item DataModelStore
│   ├─► Get component template
│   └─► renderComponent()
│
└─► LazyColumn/LazyRow displays items
```

**Implementation**:
```kotlin
@Composable
fun RenderList(
    template: ChildrenTemplate,
    dataModel: DataModelStore,
    components: Map<String, ComponentConfig>
) {
    val dataList = getListData(template.dataBinding, dataModel)
    
    LazyColumn {
        itemsIndexed(dataList) { index, item ->
            val itemDataModel = DataModelStore()
            itemDataModel.setData(item)
            
            val componentConfig = components[template.componentId]
            renderComponent(componentConfig, itemDataModel)
        }
    }
}
```

---

### Validation Flow

```
User types in TextField
        ↓
onValueChange triggers
        ↓
Update DataModelStore
        ↓
Debounce (300ms)
        ↓
ValidationEngine.validateField()
        ↓
├─► Check required
├─► Check rules (pattern, length, etc.)
└─► Check custom validation
        ↓
Update UI state (showError, errorMessage)
```

**Implementation**:
```kotlin
@Composable
fun ValidatedTextField(component: ComponentConfig) {
    var value by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    TextField(
        value = value,
        onValueChange = { newValue ->
            value = newValue
            
            // Debounced validation
            CoroutineScope(Dispatchers.Main).launch {
                delay(300)
                val errors = ValidationEngine.validateField(component, dataModel)
                showError = errors.isNotEmpty()
                errorMessage = errors.firstOrNull()?.message ?: ""
            }
        },
        isError = showError,
        supportingText = if (showError) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null
    )
}
```

---

## Multi-Page Journey Architecture

### Current Implementation

```
┌─────────────────────────────────────────────────────────────────┐
│                    NavigationHost.kt                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ NavHost with Compose Navigation                          │  │
│  │ • startDestination: "homepage"                           │  │
│  │ • Routes: Screen.Homepage, Screen.Wealth                 │  │
│  └──────────────────────────────────────────────────────────┘  │
└──────────────┬──────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Page Routes                                  │
│  ┌──────────────┐              ┌──────────────┐                │
│  │ homepage     │◄────────────►│ wealth       │                │
│  │ (Start)      │  navigate    │ (Secondary)  │                │
│  └──────┬───────┘              └──────┬───────┘                │
│         │                             │                         │
│         ▼                             ▼                         │
│  ConfigManager.getPage()      ConfigManager.getPage()          │
│  "banking_journey"            "banking_journey"                │
│  "homepage"                   "wealth_page"                    │
└──────────────┬──────────────────────────────┬──────────────────┘
               │                              │
               ▼                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PageConfig Objects                           │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ PageConfig:                                              │  │
│  │ • id: String                                             │  │
│  │ • journeyId: String                                      │  │
│  │ • sections: List<SectionConfig>                          │  │
│  │ • statusBar: StatusBarConfig                             │  │
│  │ • navigationBar: NavigationBarConfig                     │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PageRenderer.kt                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ Renders all sections in page                             │  │
│  │ • Top navigation                                           │  │
│  │ • Content sections (Column/LazyColumn)                   │  │
│  │ • Bottom navigation                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Navigation Action Flow

```
User taps button with action
        ↓
Component action triggers
        ↓
onAction(event, data) callback
        ↓
NavigationHost handles event
        ↓
├─► "navigate_wealth" → navController.navigate("wealth")
├─► "navigate_back" → navController.popBackStack()
└─► "navigate_home" → navController.navigate("home") {
       popUpTo(0) { inclusive = true }
   }
        ↓
NavHost updates current composable
        ↓
New page loads from ConfigManager
        ↓
PageRenderer renders new page sections
```

### Page State Management

| Feature | Status | Notes |
|---------|--------|-------|
| **Multiple Pages** | ✅ Implemented | homepage, wealth_page configured |
| **Navigation Routes** | ✅ Compose NavHost | Type-safe sealed Screen routes |
| **Page Loading** | ✅ Config-driven | Loads from JSON configuration |
| **Back Stack** | ✅ Compose Navigation | Automatic back stack management |
| **State Preservation** | ⚠️ Partial | NavHost preserves, page state not persisted |
| **Deep Linking** | ❌ Not implemented | Would need intent filters |
| **Page Transitions** | ⚠️ Default | Could add custom animations |

---

## Security Architecture

### 8 Security Policies

1. **Strict Component Whitelisting** - Only allowed components can render
2. **Sandboxed Logic Execution** - No external system access
3. **Restricted Expressions** - Safe expressions only (no eval/exec)
4. **No Dynamic Scripts** - Block all script injection
5. **Declarative Interactions** - Actions must be declarative
6. **Content Security Policy** - Whitelist content sources
7. **Data Minimization** - Only collect necessary data
8. **Native Permission Gates** - Gate and justify permissions

### Expression Security

```kotlin
val BLOCKED_PATTERNS = listOf(
    Regex(".*\\beval\\b.*"),      // eval()
    Regex(".*\\bexec\\b.*"),      // exec()
    Regex(".*\\bnew\\b.*"),       // new Object()
    Regex(".*\\bfunction\\b.*"),  // function() {}
    Regex(".*\\bclass\\b.*"),     // class MyClass {}
    Regex(".*\\bimport\\b.*"),    // import java.*
    Regex(".*\\brequire\\b.*")    // require('module')
)
```

---

## Performance Architecture

### 5 Optimization Strategies

1. **Streaming & Native Rendering** - Render as data arrives
2. **Efficient Parsing Pipeline** - Diffing, incremental updates
3. **Memory Management** - View recycling, multi-level caching
4. **Startup & UX** - Bundled components, skeleton screens
5. **Resource Optimization** - Connection reuse, smart idling

### Performance Targets

| Metric | Target | Measurement |
|--------|--------|-------------|
| **Frame Time** | < 16ms (60fps) | `adb shell dumpsys gfxinfo` |
| **List Scroll FPS** | > 55fps | Profiler |
| **Cold Startup** | < 2s | `adb shell am start -W` |
| **First Content Paint** | < 100ms | Performance Monitor |
| **Theme Switch** | < 100ms | Custom benchmark |
| **Binding Resolve (cached)** | < 1ms | Unit test |
| **Memory Usage** | < 100MB | `adb shell dumpsys meminfo` |

---

## File Structure

```
app/src/main/java/com/a2ui/renderer/
├── config/
│   ├── ConfigManager.kt          # Central config loader
│   ├── UIConfig.kt               # Config data classes
│   └── ValidationConfig.kt       # Validation configs
├── binding/
│   ├── DataModelStore.kt         # Runtime data storage
│   └── BindingResolver.kt        # Path resolution
├── rules/
│   ├── ValidationEngine.kt       # Validation logic
│   ├── DependencyResolver.kt     # Field dependencies
│   └── ExpressionEvaluator.kt    # Safe expressions
├── bridge/
│   └── NativeFunctionRegistry.kt # Native function bridge
├── renderer/
│   ├── ComponentRenderer.kt      # Main renderer
│   └── ListTemplateRenderer.kt   # Dynamic lists
├── ui/
│   ├── theme/
│   │   ├── Theme.kt              # Theme composables
│   │   ├── Color.kt              # Color definitions
│   │   └── Type.kt               # Typography
│   └── components/
│       └── ThemePicker.kt        # Theme toggle UI
├── data/
│   └── PreferencesManager.kt     # Settings persistence
└── journey/
    ├── JourneyManager.kt         # Journey state (planned)
    └── PageLifecycle.kt          # Page lifecycle (planned)
```

---

## State Management

### StateFlow Pattern

```kotlin
// ConfigManager
private val _themeFlow = MutableStateFlow<Theme?>(null)
val themeFlow: StateFlow<Theme?> = _themeFlow.asStateFlow()

// DataModelStore
private val _data = MutableStateFlow<Map<String, Any>>(emptyMap())
val data: StateFlow<Map<String, Any>> = _data.asStateFlow()

// DependencyResolver
private val _fieldStates = MutableStateFlow<Map<String, FieldState>>(emptyMap())
val fieldStates: StateFlow<Map<String, FieldState>> = _fieldStates.asStateFlow()
```

### Observation Pattern

```kotlin
@Composable
fun MyComposable() {
    val currentTheme by ConfigManager.themeFlow.collectAsState(initial = null)
    val data by dataModel.data.collectAsState()
    val fieldStates by DependencyResolver.fieldStates.collectAsState()
    
    // Reactively update UI
}
```

---

## References

- [Design Tokens](../.config/opencode/skills/native-configurable-ui/references/design-tokens.md)
- [Validation & Dependencies](../.config/opencode/skills/native-configurable-ui/references/validation-dependencies.md)
- [Security Policies](../.config/opencode/skills/native-configurable-ui/references/security-policies.md)
- [Performance Strategies](../.config/opencode/skills/native-configurable-ui/references/performance-strategies.md)
- [Remaining Work](remainingwork.md)
