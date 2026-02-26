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

### Journey Configuration Flow

The A2UI Renderer implements multi-page journey navigation where each journey and its constituent pages are defined in JSON configuration. The journey structure allows for hierarchical navigation between related page sections while maintaining state flow across page transitions.

#### Journey Definition Structure

```json
{
  "type": "journey",
  "id": "banking_journey",
  "name": "HSBC Banking App",
  "version": "1.0",
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
    "transition": "slide",
    "analytics": {
      "trackPageViews": true
    }
  }
}
```

### Multi-Page Architecture Pattern

```
┌─────────────────────────────────────────────────────────────────────────┐
│                    Journey Configuration Layer                          │
├─────────────────────────────────────────────────────────────────────────┤
│ JourneyConfig object with page list and navigation rules                │
│ JourneyManager central orchestrator for multi-page state                │
└─────────────────────────────────────────────────────────────────────────┘
                               │
        ┌─────────────────────┼───────────────────────┐                  
        │                     │                       │                  
┌───────▼────────┐      ┌─────▼──────┐        ┌──────▼─────────┐    
│ PAGE 1:        │      │ PAGE 2:    │        │ PAGE N:        │    
│ Homepage       │      │ Wealth     │        │ Settings       │    
│ Configuration  │      │ Configuration│        │ Configuration  │    
│ (JSON Config)  │      │ (JSON Config)│        │ (JSON Config)  │    
└────────────────┘      └────────────┘        └────────────────┘    
        │                     │                       │                  
        │                     │                       │                  
┌─────┬─▼─────────┐    ┌──────▼─────────┐    ┌──────▼─────────┐        
│   │ DataModel   │    │   DataModel    │    │   DataModel    │        
│   │ Validation  │    │ Validation     │    │ Validation     │        
│   │ Binding     │    │ Binding        │    │ Binding        │        
│   │ Flow        │    │ Flow           │    │ Flow           │        
└───┼─────────────┘    └────────────────┘    └────────────────┘        
    │                        │                       │                  
    │                        │                       │                  
    └── ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─│─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘                  
                             │                                          
                         ┌───▼───┐                                      
                         │ Data  │                                      
                         │ Model │                                      
                         │ Store │                                      
                         │ (Shared)                                     
                         └───────┘                                      
                              │                                         
        ┌─────────────────────┼───────────────────────┐                  
        │                     │                       │                  
┌───────▼────────┐      ┌─────▼──────┐        ┌──────▼─────────┐    
│ VALIDATION     │◄─────┤VALIDATION  │◄───────┤VALIDATION      │    
│ ENGINE FOR     │      │ENGINE FOR  │        │ENGINE FOR      │    
│ PAGE 1 BINDINGS│      │PAGE 2 BINDINGS│      │PAGE N BINDINGS │    
└────────────────┘      └────────────┘        └────────────────┘    
        │                     │                       │                  
        │                     │                       │                  
┌───────▼────────┐      ┌─────▼──────┐        ┌──────▼─────────┐    
│ DATA BINDING   │      │DATA BINDING│        │DATA BINDING    │    
│ FOR PAGE 1     │      │FOR PAGE 2  │        │FOR PAGE N      │    
│ WITH CONTEXT   │      │WITH CONTEXT│        │WITH CONTEXT    │    
└────────────────┘      └────────────┘        └────────────────┘    
```

### Component Architecture with Validation and Data Binding

Here is how data models, validation, and binding work across multiple pages in a journey:

#### DataModelStore with Journey Context

```kotlin
class JourneyDataModelStore {
    private val _journeyData = MutableStateFlow<Map<String, Any>>(emptyMap())
    private val _currentPageData = MutableStateFlow<Map<String, Any>>(emptyMap())
    
    // Global journey data flow accessible across all pages
    val journeyData: StateFlow<Map<String, Any>> = _journeyData.asStateFlow()
    
    // Current page-specific data flow for isolated state
    val currentPageData: StateFlow<Map<String, Any>> = _currentPageData.asStateFlow()
    
    /**
     * Set data for entire journey (persisted across pages)
     */
    fun setJourneyData(newData: Map<String, Any>) {
        _journeyData.value = _journeyData.value + newData
    }
    
    /**
     * Set data specific to current page (isolated to page lifecycle)
     */
    fun setCurrentPageData(newData: Map<String, Any>) {
        _currentPageData.value = _currentPageData.value + newData
    }
    
    /**
     * Resolves path across both journey and current page contexts
     * Priority: currentPage -> journeyData
     */
    fun resolveAtContextPath(path: String): Any? {
        // First look in current page context
        getAtPath(path, _currentPageData.value)?.let { return it }
        
        // Then fall back to journey-wide context
        return getAtPath(path, _journeyData.value)
    }
    
    /**
     * Updates data at path in current page context
     * Falls back to journey data if not present in page context
     */
    fun updatePath(path: String, value: Any) {
        val target = if (_currentPageData.value.containsKey(extractRootKey(path))) {
            _currentPageData.value + mapOf(extractRootKey(path) to traverseAndSetValue(path, value))
        } else {
            _journeyData.value + mapOf(extractRootKey(path) to traverseAndSetValue(path, value))
        }
        
        if (_currentPageData.value.containsKey(extractRootKey(path))) {
            _currentPageData.value = target
        } else {
            _journeyData.value = target
        }
    }
}
```

### Journey-Based Page Navigation and State Management

#### JourneyManager Implementation

```kotlin
object JourneyManager {
    private val _currentPage = MutableStateFlow<String>("")
    private val _journeyHistory = mutableStateListOf<String>()
    private val _pageStack = ArrayDeque<String>()
    private val _pageStates = mutableMapOf<String, PageState>()
    private val _dataModelStore = JourneyDataModelStore()
    
    valcurrentPage: StateFlow<String> = _currentPage.asStateFlow()
    
    fun navigateToPage(
        pageId: String,
        data: Map<String, Any>? = null,
        animateTransition: Boolean = true
    ) {
        // Save current page state before navigating
        persistCurrentPageState()
        
        // Store navigation metadata
        _pageStack.addLast(pageId)
        _journeyHistory.add(pageId)
        
        // Load page-specific data bindings
        data?.let { _dataModelStore.setCurrentPageData(it) }
        
        // Update current page
        _currentPage.value = pageId
        
        // Track analytics if configured
        Analytics.trackPageView(pageId)
        
        // Clear previous validation errors for new page context
        ValidationEngine.clearErrorsForPage(pageId)
    }
    
    fun navigateBack() {
        if (_pageStack.size <= 1) return
        if (_journeyHistory.size <= 1) return
        
        // Save current state
        persistCurrentPageState()
        
        // Remove current page from stack
        val leavingpageId = _pageStack.removeLast()
        _journeyHistory.removeAt(_journeyHistory.size - 1)
        
        // Navigate to previous page
        val previousPage = _pageStack.last()
        setCurrentPage(previousPage)
        
        // Restore page state if available
        restorePageState(previousPage)
        
        Analytics.trackBackNavigation(leavingpageId)
    }
    
    private fun setCurrentPage(pageId: String) {
        _currentPage.value = pageId
    }
    
    fun getCurrentDataModel(): JourneyDataModelStore = _dataModelStore
    
    fun persistCurrentPageState() {
        _currentPage.value.takeIf { it.isNotEmpty() }?.let { currentPageId ->
            _pageStates[currentPageId] = PageState(
                pageId = currentPageId,
                data = _dataModelStore.currentPageData.value,
                validationErrors = ValidationEngine.getPageErrors(currentPageId),
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    fun restorePageState(pageId: String) {
        _pageStates[pageId]?.let { pageState ->
            _dataModelStore.setCurrentPageData(pageState.data)
            ValidationEngine.restorePageValidationErrors(pageId, pageState.validationErrors)
        }
    }
    
    fun clearPageHistory() {
        _pageStack.clear()
        _journeyHistory.clear()
        _pageStates.clear()
    }
}
```

### Page Configuration Structure with Validation & Binding

#### Page Configuration Schema

```json
{
  "id": "account_overview", 
  "pageId": "account_overview_page",
  "journeyId": "banking_journey",
  "sections": [
    {
      "id": "top_nav",
      "type": "Row",
      "properties": {
        "backgroundColor": "#FFFFFF"
      }
    },
    {
      "id": "content_area",
      "type": "Column", 
      "children": {
        "explicitList": [
          {
            "id": "account_header",
            "type": "Text",
            "properties": {
              "text": {
                "binding": "$.user.displayName"
              }, 
              "fontFamily": "Roboto",
              "fontSize": 24
            }
          },
          {
            "id": "transfer_form",
            "type": "Column",
            "children": {
              "explicitList": [
                {
                  "id": "from_account",
                  "type": "Dropdown",
                  "properties": {
                    "label": {"literalString": "From Account"},
                    "placeholder": {"binding": "$.defaultAccount.label"}
                  }
                },
                {
                  "id": "to_account",
                  "type": "TextField", 
                  "properties": {
                    "label": {"literalString": "To Account"},
                    "placeholder": {"literalString": "Enter recipient account"}
                  },
                  "validation": {
                    "required": {
                      "message": {"literalString": "Please enter destination account"}
                    },
                    "rules": [
                      {
                        "type": "pattern",
                        "pattern": "^\\d{4,20}$",
                        "message": {"literalString": "Account number must be 4-20 digits"}
                      },
                      {
                        "type": "minLength",
                        "value": 4,
                        "message": {"literalString": "Minimum 4 digits required"}
                      }
                    ],
                    "customValidation": {
                      "nativeFunction": "validateAccountExists",
                      "parameters": ["$.to_account.value"]
                    }
                  }
                },
                {
                  "id": "amount",
                  "type": "TextField",
                  "properties": {
                    "label": {"literalString": "Amount"},
                    "textFieldType": "longNumber",
                    "placeholder": {"literalString": "Enter transfer amount"}
                  }, 
                  "validation": {
                    "required": {
                      "message": {"literalString": "Transfer amount is required"}
                    },
                    "rules": [
                      {
                        "type": "minValue",
                        "value": 1,
                        "message": {"literalString": "Minimum amount is $1"}
                      },
                      {
                        "type": "maxValue", 
                        "value": 100000,
                        "message": {"literalString": "Maximum amount is $100,000 per day"}
                      },
                      {
                        "type": "pattern",
                        "pattern": "^\\d+(\\.\\d{2})?$",
                        "message": {"literalString": "Enter valid monetary amount"}
                      }
                    ]
                  },
                  "dependencies": {
                    "enabled": {
                      "rule": "$.to_account.validated === true && $.to_account.value.length >= 4"
                    },
                    "required": {
                      "rule": "$.transfer_type.value === 'urgent'"
                    }
                  }
                }
              ]
            ]
          }
        ]
      }
    }
  ],
  "navigationBar": {
    "type": "bottom_navigation",
    "items": [
      {"icon": "ic_home", "label": "Home", "target": "navigate:home"},
      {"icon": "ic_accounts", "label": "Accounts", "target": "navigate:accounts"},
      {"icon": "ic_transfer", "label": "Transfer", "target": "navigate:transfer"}
    ]
  }
}
```

### Validation & Dependence Handling Architecture

#### Cross-Page Validation Flow

The Journey Manager coordinates validation between pages by maintaining context about user progression through forms. For example, validation errors entered on Page 2 remain visible when the user navigates back to Page 1 and then forward again.

```kotlin
class PageValidationContext {
    private val validationResultsCache = mutableMapOf<String, ValidationResult>()
    
    /**
     * Validate current page fields with cross-page dependency consideration
     */
    fun validateCurrentPage(): List<ValidationError> {
        val currentpageId = JourneyManager.currentPage.value
        val currentPageConfig = getPageConfig(currentpageId) 
        val dataModel = JourneyManager.getCurrentDataModel()
        
        val errors = mutableListOf<ValidationError>()
        
        // Identify fields in current page
        val fields = extractValidatableFields(currentPageConfig)
        
        fields.forEach { field ->
            // Validate basic rules
            val fieldErrors = validateFieldBasics(field, dataModel)
            
            // Check cross-page dependencies that might affect this field
            val crossPageErrors = validateCrossPageDependencies(field, fieldErrors, dataModel)
            
            errors.addAll(fieldErrors + crossPageErrors)
        }
        
        // Cache results per page
        val result = ValidationResult(fieldErrors = errors, timestamp = System.currentTimeMillis())
        validationResultsCache[currentpageId] = result
        
        return errors
    }
    
    /**
     * Validate if field dependencies are satisfied in context of journey progression
     */
    private fun validateCrossPageDependencies(
        field: ComponentConfig,
        baseErrors: List<ValidationError>,
        dataModel: JourneyDataModelStore
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        
        // Check if this field's prerequisites from other pages are met
        field.prerequisites?.forEach { prerequisite ->
            val prerequisiteMet = when {
                prerequisite.type == "data_exists" -> {
                    dataModel.resolveAtContextPath(prerequisite.path) != null
                }
                prerequisite.type == "previous_page_completed" -> {
                    JourneyManager.journeyHistory.contains(prerequisite.pageId)
                }
                prerequisite.type == "form_section_validated" -> {
                    // Check if referenced section validated successfully
                    dataModel.getAtPath("${prerequisite.sectionId}.isValid") as? Boolean == true
                }
                else -> true // Default to true for unrecognized prerequisites
            }
            
            if (!prerequisiteMet) {
                errors.add(ValidationError(
                    fieldId = field.id,
                    message = prerequisite.failureMessage ?: "Prerequisite not met",
                    ruleType = "crossPageDependency",
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        
        return errors
    }
    
    /**
     * Process validation results with page transition context
     */
    fun handleValidationResults(
        validationResult: ValidationResult,
        targetPageId: String = JourneyManager.currentPage.value,
        navigationIntent: NavigationIntent = NavigationIntent.Unknown
    ) {
        when (navigationIntent) {
            NavigationIntent.NavigateForward -> {
                // Ensure current page valid before allowing forward navigation
                if (validationResult.hasCriticalErrors) {
                    logValidationResultForPage(targetPageId, validationResult)
                    Analytics.trackValidationFailure(targetPageId, validationResult.errors)
                }
            }
            NavigationIntent.NavigateBack -> {
                // Store validation state for potential return
                validationResultsCache[targetPageId] = validationResult
            }
            NavigationIntent.Submit -> {
                // Validate entire journey context before submission
                val fulljourneyValidation = validateFulljourney()
                if (fulljourneyValidation.isValid) {
                    // Proceed with submission
                    Analytics.trackFormSubmissionSuccess(targetPageId)
                } else {
                    // Highlight relevant errors
                    val highlightedErrors = highlightRelevantErrors(fulljourneyValidation)
                    logValidationResultForPage(targetPageId, validationResult.withRelevantErrors(highlightedErrors))
                }
            }
        }
        
        // Always persist validation state for recovery
        persistValidationState(validationResult)
    }
}
```

### Journey-Based Data Binding Flow

#### Multi-Page Data Binding Architecture

The key insight is that while each page has its own isolated data context, the journey manager provides access to global journey data. The binding resolution follows a pattern:

1. First, look for the bound path in the current page's context
2. If not found, look in the journey-wide context
3. Validate the bound data in the appropriate context

```
User Action → JourneyManager → DataModel Context → Page Config → Validation → Binding Resolution → UI Update

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│ Component Event │───▶│ JourneyManager  │───▶│ DataModelStore  │
│ (onValueChange, ├───▶│ (Navigation/    ├───▶│ (Resolve path   │
│ onSubmit, etc)  │    │ State context)  │    │ in page/journey│
└─────────────────┘    └─────────────────┘    │ context)       │
                                    │         └─────────────────┘
                                    │                    │
                                    │                    ▼
                                    │         ┌─────────────────┐
                                    │◀────────┤ Validation     │
                                    │         │ Engine         │
                                    │         │ (Validate data │
                                    │         │ against rules) │
                                    └────────▶├─────────────────┤
                                              │ Binding         │
                                              │ Resolver        │
                                              │ (Update UI)     │
                                              └─────────────────┘
```

### Complete Implementation Example

Here's a practical example of the multi-page data validation binding flow:

#### Page 1: Personal Details

```json
{
  "pageId": "personal_details",
  "id": "step1_personal",
  "sections": [
    {
      "id": "personal_info_section",
      "type": "Column",
      "children": {
        "explicitList": [
          {
            "id": "first_name",
            "type": "TextField",
            "properties": {
              "label": {"literalString": "First Name"}
            },
            "validation": {
              "required": {
                "message": {"literalString": "First name is required"}  
              },
              "rules": [
                {
                  "type": "pattern", 
                  "pattern": "^[A-Za-z]{2,30}$",
                  "message": {"literalString": "Only letters, 2-30 characters"}
                }
              ]
            }
          },
          {
            "id": "last_name",
            "type": "TextField", 
            "properties": {
              "label": {"literalString": "Last Name"}
            },
            "validation": {
              "required": {
                "message": {"literalString": "Last name is required"}
              }
            }
          },
          {
            "id": "email_address",
            "type": "TextField",
            "properties": {
              "label": {"literalString": "Email Address"}
            },
            "validation": {
              "required": {
                "message": {"literalString": "Email address is required"}
              },
              "rules": [
                {
                  "type": "pattern",
                  "pattern": "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", 
                  "message": {"literalString": "Enter a valid email address"}
                }
              ],
              "customValidation": {
                "nativeFunction": "validateEmailDomain",
                "parameters": ["$.email.address"]
              }
            }
          },
          {
            "id": "continue_btn",
            "type": "Button",
            "properties": {
              "text": {"literalString": "Next: Account Setup"}
            },
            "action": {
              "event": "navigate_to_page",
              "context": {
                "targetPage": "account_setup",
                "validationRequired": true,
                "successRedirect": true
              }
            },
            "enabled": {
              "rule": "(validated($.first_name) && $.first_name.value.length >= 2) && (validated($.last_name) && $.last_name.value != '') && (validated($.email_address) && $.email_address.validated === true)"
            }
          }
        ]
      ]
    }
  ]
}
```

#### Page 2: Account Setup with Cross-Page Dependencies

```json
{
  "pageId": "account_setup", 
  "id": "step2_account",
  "sections": [
    {
      "id": "account_info_section",
      "type": "Column",
      "children": {
        "explicitList": [
          {
            "id": "welcome_message",
            "type": "Text",
            "properties": {
              "text": {
                "binding": "$.first_name.value", 
                "transform": {
                  "nativeFunction": "concatString",
                  "parameters": ["Welcome, ", "$.first_name.value"]
                }
              }
            }
          },
          {
            "id": "phone_number", 
            "type": "TextField",
            "properties": {
              "label": {"literalString": "Phone Number"}
            },
            "validation": {
              "required": {
                "message": {"literalString": "Phone number is required"}
              },
              "rules": [
                {
                  "type": "pattern", 
                  "pattern": "^[0-9+\\-\\s().]{10,15}$",
                  "message": {"literalString": "Enter a valid 10-15 digit phone number"}
                }
              ]
            }
          },
          {
            "id": "account_type",
            "type": "Dropdown",
            "properties": {
              "label": {"literalString": "Account Type"}
            },
            "validation": {
              "required": {
                "message": {"literalString": "Please select an account type"}
              }
            },
            "dependencies": {
              "options": {
                "rule": "getDynamicAccountOptionsByRegion($.user_region.value)",
                "source": "region_based_options"
              }
            }
          }
        ]
      ]
    }
  ]
}
```

#### Page-Specific Implementation

```kotlin
@Composable
fun JourneyPageRenderer(
    pageId: String,
    dataModel: JourneyDataModelStore,
    onNavigate: (String) -> Unit
) {
    // Load page configuration
    val pageConfig = ConfigManager.getPage("banking_journey", pageId)
    val navigationEnabled = remember { mutableStateOf(true) }
    
    // Validate entire page on load
    LaunchedEffect(pageId) {
        val validationResult = validatePage(pageConfig, dataModel)
        navigationEnabled.value = validationResult.isValid && 
                                  pageConfig.canProceedCheck(dataModel)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Render page sections
        pageConfig.sections.forEach { section ->
            when (section.type) {
                "Column" -> {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        renderSectionChildren(
                            section.children,
                            dataModel,
                            onNavigate,
                            pageId
                        )
                    }
                }
                "Row" -> {
                    Row(
                        modifier = Modifier
                    ) {
                        renderSectionChildren(
                            section.children,
                            dataModel,
                            onNavigate,
                            pageId
                        )
                    }
                }
            }
        }
    }
}

private fun validatePage(
    pageConfig: PageConfig,
    dataModel: JourneyDataModelStore
): ValidationResult {
    val validator = ValidationEngine()
    
    // Validate all fields in this page
    val pageFields = pageConfig.extractFields()
    val result = validator.validatePage(pageFields, dataModel)
    
    return result
}
```

---

### Data Flow Pattern for Multi-Page Journey Validations and Bindings

The following diagram illustrates the complete flow from data change through validation and binding resolution across multiple pages:

```
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ User enters data │───▶│ JourneyManager   │───▶│ Current Page     │
│ in TextField     │    │ Manages context  │    │ Specific Data    │
└──────────────────┘    └──────────────────┘    │ Binding          │
          │                       │              └─────────┬────────┘
          ▼                       ▼                        ▼
┌──────────────────┐    ┌──────────────────┐    ┌──────────────────┐
│ DataModelStore   │───▶│ Validation       │───▶│ Field Validation │
│ Updates State    │    │ Engine with      │    │ (Cross-Page      │
│ (Page/Journey)   │    │ Journey Context  │    │ Dependencies)    │
└──────────────────┘    └──────────────────┘    └─────────┬────────┘
          │                       │                        ▼
          ▼                       ▼               ┌─────────────────┐
┌──────────────────┐    ┌──────────────────┐     │ Binding Resolver│
│ StateFlow        │───▶│ Compose Rebuild  │────▶│ Processes       │
│ Emits Change     │    │ Based on Validation│   │ Data Binding    │
└──────────────────┘    │ Result & Data    │     │ (Page/Journey   │
                        │ Context State    │     │ Priority)       │
                        └──────────────────┘     └─────────────────┘
                              │                        
                              ▼                        
                    ┌──────────────────┐               
                    │ UI State Update  │               
                    │ with Validation  │               
                    │ Errors/Success   │               
                    └──────────────────┘               
```

---

### Page State Management

| Feature | Approach | Notes |
|---------|----------|-------|
| **Multiple Pages** | ✅ Implemented | homepage, wealth_page, transfer_page configured |
| **Navigation Routes** | ✅ Compose NavHost | Type-safe sealed Screen routes per journey config |
| **Page Loading** | ✅ Config-driven | Loads from JSON journey configuration |
| **Back Stack** | ✅ Compose Navigation | Automatic back stack management per journey |
| **State Preservation** | ✅ Full Journey Support | Includes both journey-wide and page-local state |
| **Cross-Page Validation** | ✅ Implemented | Prerequisites and inter-page dependencies |
| **Data Binding Across Pages** | ✅ Implemented | Journey-Global + Page-Local Context Resolution |
| **Navigation Validation** | ✅ Implemented | Required validation before page transitions |
| **Deep Linking** | ⏳ Planned | Would need intent filters and journey state restoration |
| **Page Transitions** | ✅ Custom Implemented | Slide animations configurable in journey config |
| **Form Validation Context** | ✅ Contextual Implementation | Validation errors persist across page transitions for journey continuity |
| **Multi-Page Data Flow** | ✅ Isolated + Shared Context | Current page isolated but with access to shared journey data |

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
