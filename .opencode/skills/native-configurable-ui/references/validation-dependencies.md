# Validation & Dependency System (Now With Form Engine Integration)

## Overview

This guide covers the dynamic validation rules, field dependencies, and safe expression evaluation available in the NEW A2UI renderer, highlighting how the centralized Form Engine transforms the validation and dependency system from the traditional approach.

## NEW Architecture with Form Engine Integration

### BEFORE (Traditional Distributed Approach):
```
Component (Individual validation) → ValidationEngine → Field-level evaluation
Component (Own dependencies) → DependencyResolver → Own context calculation
Component (Own binding) → BindingResolver → Own data resolution
```

### AFTER (Form Engine Centralized Approach) - NEW:
```
Form Engine (Central Authority) 
├─▶ Validation Engine for form-wide validation  
├─▶ Dependency Graph for all element relationships
├─▶ Expression Evaluator for all expressions
├─▶ Evaluation Cache for all expression results
├─▶ Action Dispatcher for navigation rules
└─▶ Component Renderers consume from unified Form State
```

## Core Concepts with NEW Form Engine

### Validation Rules

#### Traditional Approach (Distributed):
```json
{
  "id": "email_field",
  "type": "TextField",
  "properties": {
    "label": {"literalString": "Email Address"}
  },
  "validation": {
    "required": {
      "message": {"literalString": "Email is required"} 
    },
    "rules": [
      {
        "type": "pattern",
        "pattern": "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
        "message": {"literalString": "Enter a valid email address"}
      }
    ]
  }
}
```

#### NEW Form Engine Approach (Centralized):
- Form Engine maintains ALL validation rules for ALL form elements
- Components configure validation rules that get processed by Form Engine
- All validation occurs through centralized Validation Engine integrated with Form Engine
- Error states maintained in unified Form State

### NEW Form Engine Validation Architecture

The NEW Form Engine handles validation through a centralized system:

```kotlin
data class FormState(
    // Previous fields...
    val validationErrors: Map<String, List<ValidationError>> = emptyMap(), // NEW: Central validation state
    val validationCache: Map<String, ValidationResult> = emptyMap()        // NEW: Central validation cache
)

// OLD standalone validation engines now integrated with Form Engine
class ValidationEngine(
    private val formEngineState: StateFlow<FormState>  // NEW: Form Engine integration
) {
    suspend fun validateField(elementId: String): List<ValidationError> {
        val rules = getConfiguredValidationRules(elementId) // Get rules from Form Engine context
        val formData = formEngineState.value // NEW: Centralized data state
        val currentValue = formData.values[elementId]
        
        val errors = mutableListOf<ValidationError>()
        
        rules.forEach { rule ->
            if (!evaluateRule(rule, currentValue, formData)) {
                errors.add(ValidationError(
                    elementId = elementId,
                    message = rule.message,
                    ruleType = rule.type,
                    timestamp = System.currentTimeMillis()
                ))
            }
        }
        
        // NEW: Update central state with validation errors
        return errors
    }
    
    // NEW: Centralized cross-field validation in the Form Engine
    fun validateCrossFieldDependencies(): List<ValidationError> {
        val formData = formEngineState.value
        val errors = mutableListOf<ValidationError>()
        
        // Process dependencies defined in Form Engine configuration
        formData.dependencies.forEach { (dependentFieldId, dependencySpec) ->
            if (dependencySpec.type == "cross-field-validation") {
                val relatedFieldValue = formData.resolveAtContextPath(dependencySpec.sourcePath)
                if (!meetsCrossFieldCriteria(dependentFieldId, relatedFieldValue)) {
                    errors.add(ValidationError(
                        elementId = dependentFieldId,
                        message = dependencySpec.errorMessage ?: "Cross-field validation failed",
                        ruleType = "cross-field",
                        timestamp = System.currentTimeMillis()
                    ))
                }
            }
        }
        
        return errors
    }
}
```

## Validation Rule Types NEW with Form Engine

### 1. Required Validation (Form Engine Processed)
```json
{
  "id": "full_name",
  "type": "TextField", 
  "validation": {
    "required": {
      "message": {"literalString": "Please provide your full name"}
    }
  }
}
```
**NEW Form Engine Process**:
- Form Engine loads required validation rule definition  
- During each Form State update, Form Engine re-evaluates required status for this field
- Error state stored in centralized Form Engine state
- Component Renderer accesses error state from unified Form State flow

### 2. Pattern Validation (Form Engine Orchestrated)  
```json
{
  "id": "phone_number",
  "type": "TextField",
  "validation": {
    "required": {
      "message": {"literalString": "Phone number is required"}
    },
    "rules": [
      {
        "type": "pattern",
        "pattern": "^\\d{10,12}$", 
        "message": {"literalString": "Enter 10-12 digit phone number"}
      }
    ]
  }
}
```
**NEW Form Engine Process**:
- All pattern expressions evaluated in centralized Expression Evaluator within Form Engine
- Validation result cached in centralized validation cache managed by Form Engine
- Component receives validation status from central Form Engine state

### 3. Cross-Field Validation (Form Engine Enabled) - NEW
Cross-field validation is now NATIVELY supported through Form Engine's dependency system:

```json
{
  "id": "confirm_password",
  "type": "PasswordInput",
  "validation": {
    "required": {
      "message": {"literalString": "Please confirm password"}
    },
    "dependencies": {
      "equals": {
        "sourcePath": "$.password_field.value",  // NEW: Reference to other field via Form Engine state
        "message": {"literalString": "Passwords must match"}
      }
    }
  }
}
```
**NEW Form Engine Process**:
- Form Engine tracks dependency: confirm_password depends on password_field
- When password_field updates, Form Engine invalidates dependent validation (confirm_password)
- NEW: Cross-field validation evaluated based on full Form Engine state 
- Dependency relationships managed in centralized Dependency Graph

### 4. Custom Validation Functions (Form Engine Bridged)
```json
{
  "id": "ssn_number", 
  "type": "TextField",
  "validation": {
    "required": {
      "message": {"literalString": "Social Security Number required"}
    },
    "customValidation": {
      "nativeFunction": "validateSsnChecksum",  // Form Engine bridges this
      "parameters": ["$.ssn_number.input_value"]  // Form Engine provides data access
    }
  }  
}
```
**NEW Form Engine Process**:
- Custom validation functions are now registered with Form Engine
- Native functions execute within secure Form Engine context
- Results processed through Form Engine centralized validation pipeline

## Field Dependencies with Form Engine Enhancement

Form Engine provides CENTRAL dependency tracking and evaluation:

### 1. Visibility Dependencies (Form Engine Controlled)
```json
{
  "id": "confirm_email_field",
  "type": "TextField",
  "dependencies": {
    "visibility": {
      "rule": "$.email_opt_in.checked === true",  // NEW: Evaluated by Form Engine
      "action": "show"  // NEW: Managed by Form Engine state
    }
  }
}
```
**NEW Form Engine Process**:
- Form Engine knows that confirm_email_field visibility depends on email_opt_in.checked
- Central Dependency Graph maps this relationship
- When email_opt_in state changes, Form Engine re-evaluates visibility for dependent elements
- Visibility state propagated to unified Form State

### 2. Enablement Dependencies (Form Engine Coordinated) 
```json
{
  "id": "next_button",
  "type": "Button", 
  "dependencies": {
    "enabled": {
      "rule": "$.email_field.validated === true && $.phone_field.validated === true", // NEW: Form Engine evaluates
      "source": "form_completion_prerequisites" // NEW: Named dependency for debugging/visualization
    }
  }
}
```
**NEW Form Engine Process**:
- Form Engine knows next_button depends on both email_field and phone_field validation state
- When either field gets validated, Form Engine re-evaluates next_button's enablement
- All enablement rules now centralized with dependency tracking

### 3. Value Dependencies (Form Engine Processed)
```json
{
  "id": "shipping_address", 
  "type": "TextField",
  "dependencies": {
    "value": {
      "rule": "$.address_type.value === 'same_as_billing' ? $.billing_address.value : ''", // Form Engine handles
      "source": "address_auto_fill"
    }
  }
}
```

## Safe Expression System with NEW Form Engine Integration

### NEW Expression Namespace System (Form Engine Managed)
Form Engine now operates different evaluation namespaces:

1. **validation:** For validation expression execution  
2. **visibility:** For visibility logic with caching  
3. **binding:** For data binding resolution with dependency tracking
4. **enablement:** For enable/disable logic with caching 
5. **option-generation:** For dynamic choice lists with centralized caching

```kotlin
enum class EvaluationNamespace {
    VALIDATION,       // NEW: For validation rule evaluation  
    VISIBILITY,       // NEW: For visibility expressions with dedicated cache
    ENABLEMENT,       // NEW: For enable/disable expressions with dedicated cache  
    VALUE_BINDING,   // NEW: For data binding expressions with dependency tracking
    CHOICE_EVALUATION // NEW: For dynamic option evaluation with centralized caching
}

// NEW: Centralized evaluation with namespaced caching
interface ExpressionEvaluator {
    suspend fun evaluate(
        expression: String,
        namespace: EvaluationNamespace,     // NEW: Namespace controls evaluation context
        contextElementId: String = ""       // NEW: Element ID for context-specific evaluation
    ): Any?
}
```

### Supported Expression Types in NEW Form Engine Architecture

#### 1. Path Resolution (Form Engine Central)
```
$.user.profile.name     // NEW: Now resolved through Form Engine centralized state 
$.address.billing.city  
$.items.0.quantity      // NEW: Array index resolution in unified Form State
```

#### 2. Comparisons (NOW Central in Form Engine)
```
$.user.age >= 18           // NEW: Evaluated by central Form Engine  
$.form.isValid === true    // NEW: Boolean evaluation through unified state
$.account.type != "closed" // NEW: String comparison via Form Engine
```

#### 3. Logical Operations (Form Engine Processed)
```
($.user.age >= 21 && $.user.usa_resident === true) || $.user.id_verified === true  // NEW: All in Form Engine evaluation
$.opt_in_newsletter.value && ($.age_group === "adult" || $.age_group === "senior") // Form Engine handles all logic
```

#### 4. NEW Function Operations in Form Engine
Form Engine now recognizes these built-in functions:

- `length(value)` - NEW: String/array length through Form Engine
- `contains(string, substring)` - NEW: String inclusion check via Form Engine  
- `indexOf(arrayOrString, value)` - NEW: Position lookup through Form Engine
- `min/max(numbers...)` - NEW: Numeric comparisons through Form Engine
- `now()` - NEW: Current timestamp from Form Engine

```
$.comments.length() > 0                      // NEW: Length evaluation via Form Engine
contains($.email.value, "@gmail.com")        // NEW: String operation in Form Engine
indexOf($.selected_tags, "urgent") !== -1    // NEW: Lookup through Form Engine  
min($.amount.value, 1000) > $.minimum.value    // NEW: Math via Form Engine
```

### NEW Blocking Patterns with Form Engine Security
Form Engine blocks the following unsafe operations:

❌ `eval("some code")` - NO evaluation of dynamic code in NEW architecture
❌ `new Object()` - NO object instantiation through Form Engine
❌ `import module` - NO module loading through Form Engine  
❌ `function() {...}` - NO function creation through NEW system
❌ `=>` or `->` - NO lambda expressions through NEW system
❌ `.constructor` - NO constructor access through Form Engine
❌ JavaScript/Shell expressions - NO dynamic evaluation through Form Engine

## Dependency Matrix Visualization (Now Available via NEW Form Engine)

With the Form Engine's centralized dependency tracking, developers can now visualize form element relationships:

```
Dependency Graph Visualization: 
(Generated by NEW Form Engine's Graph Traversal System)
```
```
┌─────────────┐    enables    ┌─────────────┐
│ email_opt_in ├───┬──────────►│ next_button │
│ (Checkbox)  │   │           │ (Button)    │
└─────────────┘   │           └─────────────┘
                  │ validates
┌─────────────┐   │           ┌─────────────┐
│ phone_field │───┼──────────►│ confirm_btn │
│ (TextField) │   │           │ (Button)    │
└─────────────┘   │           └─────────────┘
                  │ influences
┌─────────────┐   │           ┌─────────────┐
│ age_slider  │──────────────►│ discount    │
│ (Slider)    │               │ (Text)      │
└─────────────┘               └─────────────┘
```
NEW capabilities of Form Engine Dependency Management:
- REAL-TIME dependency visualization (NEW capability through centralized graph)
- CYCLE DETECTION to prevent infinite loops (NEW through Form Engine)
- NAVIGATION LOGIC visualization based on form completion states (NEW)
- PERFORMANCE OPTIMIZATION through intelligent evaluation order (NEW in Form Engine)

## Native Function Bridge (Form Engine Enhanced)

For complex logic, use the NEW Form Engine-controlled native function bridge:

```json
{
  "id": "discount_amount", 
  "type": "Text",
  "properties": {
    "text": {
      "transform": {
        "nativeFunction": "calculateDiscount",     // NOW registered with Form Engine
        "parameters": ["$.customer.tier.value", "$.original_price.value", "$.quantity.value"] // Passed via Form Engine
      }
    }
  }
}
```

### NEW Built-In Functions (Available via Form Engine Bridge)
The NEW Form Engine registers these native functions:

#### NEW String Functions in Form Engine:
- `concatString(first, second, ...)` - NEW: Safe string concatenation through Form Engine
- `toLowerCase(string)` - NEW: Case conversion through Form Engine context
- `toUpperCase(string)` - NEW: Case conversion through Form Engine
- `trim(string)` - NEW: Whitespace removal via Form Engine
- `substring(string, start, end?)` - NEW: Character range through Form Engine
- `replace(string, old, new)` - NEW: Substitution in Form Engine context

#### NEW Math Functions in Form Engine:
- `add(a, b, ...)` - NEW: Multiple number addition via Form Engine
- `multiply(a, b, ...)` - NEW: Multiplication through Form Engine
- `divide(dividend, divisor)` - NEW: Division in controlled Form Engine
- `round(number, precision?)` - NEW: Rounding via Form Engine
- `max(numbers...)` - NEW: Maximum in Form Engine context
- `min(numbers...)` - NEW: Minimum through Form Engine  

#### NEW Date Functions in Form Engine:
- `formatDate(timestamp, format?)` - NEW: Date formatting via Form Engine
- `dateDiff(date1, date2, unit?)` - NEW: Time difference calculation through Form Engine
- `now()` - NEW: Current timestamp from Form Engine context

#### NEW Validation Functions in Form Engine:
- `validateEmail(email)` - NEW: Email validation through Form Engine
- `validatePhone(phone)` - NEW: Phone validation by Form Engine
- `validateSsn(ssn)` - NEW: SSN checksum through centralized Form Engine validation
- `validateAge(age, minAge?)` - NEW: Age validation via Form Engine

### NEW Registration for Native Functions in Form Engine:
```kotlin
class NativeFunctionRegistry(
    private val formEngine: FormEngine  // NEW: Form Engine controls function registry
) {
    init {
        register("concatString") { params -> params.joinToString("") } 
        register("calculateTax") { params -> 
            val amount = params[0].toFloatOrNull() ?: 0f
            val rate = params[1].toFloatOrNull() ?: 0f  
            amount * rate
        }
        // NEW: Form Engine manages all native functions securely
    }
    
    suspend fun execute(funName: String, params: List<Any?>): Any? {
        // NEW: All executions happen through Form Engine security context
        if (!isFormEngineControlled(funName)) {
            throw SecurityException("Function not approved by Form Engine: $funName")
        }
        
        val function = registeredFunctions[funName] ?: 
            throw IllegalArgumentException("Function not found: $funName")
            
        return function(params)   
    }
}
```

## NEW Form Engine Migration Guide

### Step 1: Update Configuration for NEW Form Engine Processing
```diff
{
  "id": "registration_form",
  "type": "Form", 
  "children": {
    "explicitList": [
      {
        "id": "email_field",
        "type": "TextField",
        "properties": {
          "label": {"literalString": "Email"}
        },
-       // OLD: Validation resolved individually
-       "validation": {
-         "required": true,
-         "pattern": "email_pattern"
-       }
+       // NEW in Form Engine: Validation processed globally
+       "validation": {
+         "required": {
+           "message": {"literalString": "Email required"}
+         },
+         "rules": [
+           {
+             "type": "pattern",
+             "pattern": "^.*@.*\\..*$",
+             "message": {"literalString": "Valid email required"} 
+           }
+         ]
+       }
      }
    ]
  }
}
```

### Step 2: Update Component Renderers for Form Engine Integration
```diff
@Composable
fun ValidatedTextField(
  component: ComponentConfig
- dataModel: DataModelStore,  // REMOVED: Form Engine supplies data 
- validationEngine: ValidationEngine  // REMOVED: Form Engine manages validation
) {
+ // NEW: Receive form state from Form Engine via StateFlow
+ val formState by formEngine.formState.collectAsState()
+ val value by formEngine.getValue(component.id)
+ val errors by formEngine.getErrors(component.id)
+ val isDirty by formEngine.getDirtyState(component.id) 
  
  var input by remember { mutableStateOf(value?.toString() ?: "") }
  var showError by remember { mutableStateOf(errors.isNotEmpty()) }
  
  TextField(
    value = input,
    onValueChange = { newValue ->
      input = newValue
-     // OLD: Individual validation processing
-     val validationResults = validationEngine.validateField(component, dataModel)
      
+     // NEW: Dispatch to Form Engine 
+     formEngine.updateValue(component.id, newValue, ChangeSource.USER_INPUT)
      
+     // NEW: Form Engine handles validation internally
      showError = errors.isNotEmpty() 
    },
    isError = showError,
    supportingText = if (showError) {
      { Text(errors.first().message) }
    } else null
  )
}
```

### Step 3: Leverage NEW Cross-Element Validation in Form Engine
```json
{
  "id": "form_with_cross_validation", 
  "type": "Form",
  "children": {
    "explicitList": [
      {
        "id": "start_date",
        "type": "DateTimeInput",
        "validation": {
          "required": {"message": {"literalString": "Required"}}
        }
      },
      {
        // NEW: This validation now works through Form Engine's centralized validation
        "id": "end_date",  
        "type": "DateTimeInput",
        "validation": {
          "required": {"message": {"literalString": "Required"}},
          "dependencies": {
            "greater_than": {
              "sourcePath": "$.start_date.value",  // NEW: Form Engine resolves cross-element
              "message": {"literalString": "Must be after start date"}
            }
          }
        }
      }
    ]
  }
}
```

## NEW Performance Considerations with Form Engine

### NEW Caching via Central Form Engine
- NEW: Expression results cached by Form Engine per namespace (validation, visibility, etc.)
- NEW: Dependency-based invalidation managed by Form Engine for optimal performance  
- NEW: Bulk evaluation of dependent elements coordinated by Form Engine
- NEW: Evaluation results cached in namespaced caches by Form Engine

### NEW Evaluation Order in Form Engine 
- NEW: Deterministic evaluation based on dependency graph calculated by Form Engine
- NEW: No more unpredictable evaluation order as in traditional approach
- NEW: Circular dependency detection prevents infinite loops in Form Engine
- NEW: Evaluation orchestration through Form Engine dependency matrix

## NEW Security Enhancements with Form Engine

### NEW Centralized Security Control
- NEW: Form Engine acts as single authority for all validation/dependency execution
- NEW: All expression evaluation happens within Form Engine security context  
- NEW: Native function execution secured through Form Engine gatekeeper
- NEW: Caching security - Form Engine validates cached results before retrieval

### NEW Sandboxing via Form Engine
- NEW: Form Engine executes expressions with zero external system access
- NEW: Form Engine enforces strict timeout for all evaluations
- NEW: Form Engine prevents all network/file system access from expressions
- NEW: Form Engine manages memory limits for expression evaluation  

## Troubleshooting NEW Form Engine Validations & Dependencies

### NEW Form Engine Validation Issues

Problem: `Validation not triggering after form engine integration`
Solution: NEW - Verify that your validation rules are properly configured for centralized processing by Form Engine
- Check if validation configuration conforms to Form Engine schema
- Ensure native functions are registered with Form Engine
- Confirm validation results visible in Form Engine state

Problem: `Cross-field validation not working in Form Engine`
Solution: NEW - Form Engine handles cross-field validation through dependency graph
- Verify that Form Engine registers dependencies correctly
- Debug Form Engine's dependency matrix to see tracked relationships
- Check that both fields participate in the same Form Engine journey context

Problem: `Performance slow with centralized Form Engine`
Solution: NEW - Optimize Form Engine evaluation
- Review Form Engine's dependency graph for circular references
- Check Form Engine evaluation cache misses for frequently accessed expressions
- Profile Form Engine evaluation order for optimization opportunities

### NEW Expression Evaluation Problems

Problem: `Expression fails in Form Engine context`  
Solution: NEW - Debug through Form Engine evaluation
- Verify expression uses supported syntax according to Form Engine validator
- Check if native functions called are registered with Form Engine 
- Confirm all referenced paths exist in current Form Engine state context

Problem: `Cross-element expressions undefined in Form Engine`
Solution: NEW - Debug Form Engine's dependency registration
- Verify target element exists in same Form Engine state context
- Check Form Engine dependency graph includes the relationship
- Confirm referenced path follows standard Form Engine path resolution

## NEW Key Differences in Form Engine Implementation

| Aspect | Traditional (Distributed) | NEW (Form Engine Centralized) |
|--------|--------------------------|-------------------------------|
| **Validation Authority** | Each component | Form Engine (single owner) |
| **Dependency Tracking** | Per-element resolution | Central dependency graph |
| **Expression Evaluation** | Individual evaluators | Central evaluation with namespace |
| **Caching System** | Scattered per-component | Form Engine-wide with namespaces |
| **Error State** | Scattered per-field | Form Engine state flow |
| **Navigation Logic** | Component level | Form Engine decision engine |
| **Security Context** | Element-local restrictions | Form Engine centralized security |  
| **Performance Optimization** | Per-render basis | Global dependency optimization |

The NEW Form Engine approach transforms validation and dependency handling from scattered component logic to a centralized, optimized system that ensures consistency, security, and performance.