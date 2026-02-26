# A2UI Renderer - Implementation Roadmap

## Project Overview
This document serves as the roadmap for implementing the A2UI Renderer, tracking both completed implementations, current implementations with new Form Engine integration, and future enhancements.

## Project Status
✅ **Completed Traditional Iterations (1-11)**: Core functionality using traditional distributed architecture  
🔄 **Integrating Form Engine Layer**: Implementing new centralized form engine architecture  
⏳ **Planned Iterations (12, 13, 15-16)**: Enhancement features with Form Engine integration pending  

## Form Engine Iteration 0: Core Integration Layer (P0) 
**Focus**: Implement Form Engine as a centralized layer managing all form state, validation, dependency tracking, caching, and navigation decisions

### Iteration 0.1: Single Source of Truth Layer (P0) ✅ IN PROGRESS
- **Objective**: Replace fragmented state management across components/pages with centralized Form State in Form Engine
- **Key Deliverables**:
  - FormState data class for unified state management across all form elements
  - Centralized FormStateFlow as the single source of truth  
  - Migration path for existing DataModelStore to FormState mapping
  - ComponentRenderer updates to consume from Form Engine rather than distributed helpers
  - Integration tests validating unified state approach

### Iteration 0.2: Deterministic Evaluation Engine (P1) ✅ PLANNED
- **Objective**: Implement evaluation engine with deterministic order based on dependency matrix
- **Key Deliverables**:
  - Expression evaluator with guaranteed execution order
  - Dependency tracking system (explicit dependency arrays)
  - Namespace-based evaluation (validation, binding, visibility, etc.)
  - Evaluation cache per namespace
  - Performance benchmarking vs. existing approach

### Iteration 0.3: Incremental Re-Evaluation System (P1) ✅ PLANNED
- **Objective**: Replace full form re-evaluation with incremental updates based on dependency changes
- **Key Deliverables**:
  - Transitive dependency matrix implementation
  - Incremental evaluation triggers
  - Change detection system based on dependency tracking
  - Performance comparison metrics against legacy system

### Iteration 0.4: Centralized Caching System (P1) ✅ PLANNED
- **Objective**: Implement centralized evaluation cache and dynamic choice management
- **Key Deliverables**:
  - Multi-level cache (memory, disk, per-namespace)
  - Dynamic choice evaluation and caching  
  - Pre-choice rules with centralized management
  - Cache invalidation based on dependency tracking

### Iteration 0.5: Consistent Error Management (P1) ✅ PLANNED
- **Objective**: Standardize error mapping, dirty behavior, and validation
- **Key Deliverables**:
  - Centralized ErrorState management
  - Consistent dirty and touched flag handling
  - Standardized validation error structure
  - Error display consistency across all components

### Iteration 0.6: Engine-Controlled Data Updates (P1) ✅ PLANNED
- **Objective**: Form Engine controls all updates to backing DataModelStore
- **Key Deliverables**:
  - DataModelStore adapter that respects Form Engine control
  - Engine-controlled update paths
  - Consistent data mapping between Form Engine and DataModelStore
  - Synchronization mechanisms between layers

### Iteration 0.7: Derived State Management (P2) ✅ PLANNED
- **Objective**: Centralize management of visibility, enabled/readonly, errors, choices
- **Key Deliverables**:
  - Form State derived properties (visibility/enablement)
  - Standardized error state propagation
  - Choice and option management from engine
  - Consistent property mapping across all components

### Iteration 0.8: Dependency Matrix & Evaluation Orchestration (P2) ✅ PLANNED
- **Objective**: Implement visualization-capable dependency matrix and orchestrated evaluation
- **Key Deliverables**:
  - Visualizable dependency matrix 
  - Directed acyclic graph of form element dependencies
  - Orchestration engine for evaluation order
  - Dependency matrix visualization utilities

### Iteration 0.9: Action Dispatch & Navigation Logic (P2) ✅ PLANNED
- **Objective**: Implement centralized action dispatcher with ViewIdRule-based navigation
- **Key Deliverables**:
  - Unified action dispatcher
  - Form Engine-based navigation decisions
  - ViewIdRule implementation with conditional logic
  - Consistent navigation behavior across journeys

### Iteration 0.10: Component Renderer Refactoring (P1) ✅ PLANNED
- **Objective**: Update ComponentRenderer to consume exclusively from Form Engine state instead of direct config helpers
- **Key Deliverables**:
  - ComponentRenderer consumption of Form Engine state
  - Elimination of direct BindingResolver usage
  - Form-aware components integrated with Form Engine
  - Performance regression testing

### Iteration 0.11: Validation Engine Refactoring (P1) ✅ PLANNED
- **Objective**: Migrate existing ValidationEngine logic into FormEngine orchestration
- **Key Deliverables**:
  - Integration of current validation rules into Form Engine
  - Preservation of existing validation behavior
  - Performance optimization
  - Migration path for current validators

### Iteration 0.12: Complete Journey Integration (P1) ✅ PLANNED
- **Objective**: Full JourneyManager integration with Form Engine for cross-page state management
- **Key Deliverables**:
  - JourneyManager coordinating with Form Engine
  - Cross-page state persistence
  - Navigation validation rules through Form Engine
  - Deep-linking state restoration via Form Engine

## Completed Traditional Implementations (Being Migrated to Form Engine)

### Iteration 1: Theme Integration (P0) ✅
- **Focus**: Connect theme JSON to Compose MaterialTheme system
- **Status**: ✅ Complete with 20 unit test passing
- **Key Achievements**: 
  - Dynamic color scheme builder from JSON theme data
  - Typography mapping from theme configurations
  - ConfigManager integration with StateFlow
- **Notes for Migration**: Will integrate theme changes with Form Engine state flow
- **Files**: `Theme.kt`, `Type.kt`, `ConfigManager.kt`

### Iteration 2: Runtime Theme Switching (P0) ✅
- **Focus**: Enable runtime theme switching with persistence
- **Status**: ✅ Complete with 20 unit test passing  
- **Key Achievements**: 
  - PreferencesManager for theme persistence
  - ThemePicker Composable component
  - Smooth transition animations
  - SharedPreferences integration
- **Notes for Migration**: Theme change events can optionally trigger form recalculations
- **Files**: `PreferencesManager.kt`, `ThemePicker.kt`, `MainActivity.kt`

### Iteration 3: Data Binding (P1) ✅
- **Focus**: Implement reactive data model with path resolution 
- **Status**: ✅ Complete with 45 unit tests passing
- **Key Achievements**:
  - DataModelStore with StateFlow for reactive data
  - BindingResolver for $path expressions
  - Nested path and array index support
  - Text and color binding resolution
- **Files**: `DataModelStore.kt`, `BindingResolver.kt`
- **Notes for Migration**: Data binding logic now managed through Form Engine layer instead of direct resolver

### Iteration 4: Dynamic Lists (P1) ✅
- **Focus**: Implement template-based dynamic list rendering
- **Status**: ✅ Complete with 58 unit tests passing
- **Key Achievements**:
  - ChildrenTemplate data structure for list templates
  - ListTemplateRenderer with LazyColumn/LazyRow support
  - Array index path support ($.products.0.name)
  - Item-scoped data models for templates
- **Files**: `ListTemplateRenderer.kt`, `UIConfig.kt`, `ConfigManager.kt`
- **Notes for Migration**: Dynamic lists now use Form Engine state

### Iteration 5: Dynamic UI Rules Framework (P1) ✅
- **Focus**: JSON-driven validation rules, field dependencies, and expressions
- **Status**: ✅ Complete with 81+ unit tests passing
- **Architecture**:
  - Validation Engine for input validation rules
  - Dependency Resolver for field relationships
  - Expression Evaluator for safe expressions
  - Native Function Bridge for custom logic
- **Files**: `ValidationEngine.kt`, `DependencyResolver.kt`, `ExpressionEvaluator.kt`, `NativeFunctionRegistry.kt`
- **Notes for Migration**: These engines now orchestrate through Form Engine instead of independently

### Iteration 6-11: Core Features ✅
- **Iter 6 (P1)**: Multi-Page Journey Navigation - Dynamic routing with state persistence
- **Iter 7 (P2)**: UsageHint Typography - Semantic hint → Material Typography mapping
- **Iter 8 (P2)**: Shadows & Elevation System - Theme-configurable shadow support  
- **Iter 9 (P1)**: Security Framework - 8 comprehensive security policies
- **Iter 10 (P1)**: Performance Optimization - 5 strategies including streaming, diffing, caching
- **Iter 11 (P1)**: Multi-Domain Models - Event-driven observer pattern with path mapping
- **Note for Migration**: All now use single source of form truth in Form Engine

## Current State Summary
- **Core Features**: Complete (Iterations 1-11) with migration to Form Engine architecture underway
- **Form Engine Implementation Status**: Initial integration layer in progress with 12 new planned iterations
- **Migration Strategy**: Phased approach moving from distributed state to centralized Form Engine
- **Enhancement Features**: Planned to leverage Form Engine architecture (Iterations 12, 13, 15-16)
- **Test Coverage**: Form Engine integration will expand unit test coverage to consolidate scattered tests

## Technical Reference - Dynamic UI Rules Framework

### Rule Architecture
1. **Validation Rules**: Input validation via JSON declarations
2. **Field Dependencies**: Relationship rules between form fields  
3. **Expressions**: Restricted safe expression evaluation
4. **Native Functions**: Bridged function calls in secure context
5. **Conditional Visibility**: Dynamic display controls

### Validation Rule Types
```kotlin
sealed class ValidationRule {
    data class Pattern(val pattern: String, val message: TextValue) : ValidationRule()
    data class MinLength(val value: Int, val message: TextValue) : ValidationRule()
    data class MaxLength(val value: Int, val message: TextValue) : ValidationRule()
    data class Required(val message: TextValue) : ValidationRule()
    data class Custom(val nativeFunction: String, val parameters: List<String>) : ValidationRule()
}
```

## Technical Reference - Security Framework

### 8 Security Policies Overview
```
┌─────────────────────────────────────────────────────────────────┐
│                    A2UI Security Framework                       │
├─────────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ 1. Component │  │ 2. Sandboxed │  │ 3. Restricted│         │
│  │ Whitelisting │  │ Logic        │  │ Expressions  │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐         │
│  │ 4. No Dynamic│  │ 5. Declarative│  │ 6. Content   │         │
│  │ Scripts      │  │ Interactions │  │ Policy       │         │
│  └──────────────┘  └──────────────┘  └──────────────┘         │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │ 7. Data      │  │ 8. Native    │                            │
│  │ Minimization │  │ Permission   │                            │
│  │              │  │ Gates        │                            │
│  └──────────────┘  └──────────────┘                            │
└─────────────────────────────────────────────────────────────────┘
```

### Policy 1: Strict Component Whitelisting
- Only explicitly allowed components can be rendered
- Validates against permitted component list

### Policy 2: Sandboxed Logic Execution  
- All logic runs in sandbox with system access blocked
- Timeouts and resource limits enforced

### Policy 3: Restricted Expressions
- Only safe, declarative expressions allowed
- No function calls, object creation, or eval

### Policy 4: No Dynamic Scripts
- Blocks all script injection completely
- Sanitizes content thoroughly

### Policy 5: Declarative Interactions
- Imperative code blocked (runScript, validate)
- Only declarative actions allowed (navigate, submit)

### Policy 6: Content Security Policy
- Whitelists content sources (images, fonts, etc.)
- Prevents external content loading abuse

### Policy 7: Data Minimization
- Requests only necessary data
- Defines clear data sensitivity levels

### Policy 8: Native Permission Gates
- Explicit approval for all permission requests
- Justification required for permissions

## Technical Reference - Performance Strategies

### 5 Optimization Strategies Framework
```
┌─────────────────────────────────────────────────────────────────┐
│              A2UI Performance Optimization Framework             │
 ├────────────────────────────────────────────────────────────────┘
 │  ┌──────────────────┐  ┌──────────────────┐                    
 │  │ 1. Streaming &   │  │ 2. Efficient     │                    
 │  │ Native Rendering │  │ Parsing Pipeline │                    
 │  └──────────────────┘  └──────────────────┘                    
 │                                                                 
 │  ┌──────────────────┐  ┌──────────────────┐                    
 │  │ 3. Memory        │  │ 4. Startup & UX  │                    
 │  │ Management       │  │ Optimization     │                    
 │  └──────────────────┘  └──────────────────┘                    
 │                                                                 
 │  ┌──────────────────┐                                          
 │  │ 5. Resource      │                                          
 │  │ Optimization     │                                          
 │  └──────────────────┘                                          
 │                                                                 
 └─────────────────────────────────────────────────────────────────┘
```

### Strategy 1: Streaming & Native Rendering
- Render content as it arrives, not after full download
- JSONReader for incremental parsing
- Native rendering primitives (Material components)

### Strategy 2: Efficient Parsing & Pipeline
- Diff-engine for incremental updates
- Lazy loading with smart preloading
- Efficient change detection

### Strategy 3: Memory Management
- Component recycler with pooling
- Multi-level caching (memory, disk)
- Backpressure handling

### Strategy 4: Startup & UX
- Bundled components for instant availability
- Skeleton loading screens
- Fast initial display

### Strategy 5: Resource Optimization
- Persistent connections (WebSocket/SSE)
- Smart idling when app backgrounded
- Connection reuse

## Architectural Decisions

### Local-First Configuration
> **Decision**: Server communication is intentionally **LOW PRIORITY**. All configuration is stored locally in `res/raw/*.jsonl` files. This is valid for offline-first or embedded renderer scenarios.

### Core Architecture Pattern
```
┌─────────────────────────────────────────────────────────────────┐
│                    JSON Configuration Files                      │
│  themes.jsonl │ global_settings.jsonl │ sections/*.jsonl        │
└────────────────────┬────────────────────────────────────────────┘
                     │ Load at init
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ConfigManager (Singleton)                  │
│  ┌────────────────┐  ┌──────────────────┐  ┌─────────────────┐ │
│  │ themes: Map    │  │ uiConfig: UIConfig│  │ preferences     │ │
│  │ allComponents  │  │ globalSettings   │  │ PreferencesMgr  │ │
│  └────────────────┘  └──────────────────┘  └─────────────────┘ │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ themeFlow: StateFlow<Theme?>                             │ │
│  │ • Emits when theme changes                               │ │
│  │ • Observed by A2UIRendererTheme                          │ │
│  │ • Persists to SharedPreferences                          │ │
│  └──────────────────────────────────────────────────────────┘ │
└──────────────┬─────────────────────────────────────────────────┘
               │ themeFlow.collectAsState()
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                   A2UIRendererTheme (Composable)                │
│  • Observes themeFlow                                          │
│  • Builds ColorScheme from theme.colors                        │
│  • Builds Typography from theme.typography                     │
│  • Provides MaterialTheme to children                          │
└──────────────┬─────────────────────────────────────────────────┘
               │ MaterialTheme.current
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Component Renderers                          │
│  renderText() │ renderCard() │ renderButton() │ renderList()   │
│  • Access MaterialTheme.colorScheme                            │
│  • Access MaterialTheme.typography                             │
│  • Resolve bindings via BindingResolver                        │
└──────────────┬─────────────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────────┐
│              DataModelStore + BindingResolver                   │
│  ┌──────────────────┐         ┌─────────────────────────────┐ │
│  │ _data:StateFlow  │◄───────►│ resolve("$.user.name")      │ │
│  │ setData()        │         │ updateAtPath()              │ │
│  │ getAtPath()      │         │ resolveText()               │ │
│  └──────────────────┘         └─────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Patterns

#### Theme Switching Flow
```
User clicks theme toggle  
        │
        ▼
ThemeToggleButton.onClick()
        │
        ▼
ConfigManager.setTheme("banking_dark")
        │
        ├─► Update currentTheme  
        ├─► preferencesManager.setSelectedTheme()
        └─► _themeFlow.value = newTheme  ◄── Emits!
                │
                ▼
A2UIRendererTheme collects change
                │  
                ▼
Recompose with new ColorScheme
                │
                ▼
All composables using MaterialTheme rebuild
                │
                ▼
UI updated with dark theme colors
```

## Future Enhancements

### Iteration 12: Advanced Animations (P2)
**Focus**: Component-level animations from JSON configuration
- AnimationConfig data class
- AnimationModifier for component-level animations
- Custom animation curves from JSON
- Gesture-based animations
- Estimated Effort: 8-12 hours

### Iteration 13: Enhanced Accessibility (P2)  
**Focus**: Full accessibility including screen readers, keyboard nav, high contrast
- AccessibilityManager for screen readers
- Keyboard navigation support
- High contrast mode
- Focus management
- Estimated Effort: 10-15 hours

### Iteration 15: Internationalization (P2)
**Focus**: Multi-language support, RTL layout, locale formatting
- LocaleManager
- String resource externalization  
- RTL layout support
- Locale-specific formatting
- Estimated Effort: 12-18 hours

### Iteration 16: Advanced Components (P2)
**Focus**: Missing A2UI-compatible components (Modal, Slider, DateTimeInput)
- Modal/Dialog component
- Slider with steps/ranges
- DateTimeInput for dates/times
- Estimated Effort: 14-20 hours (Phase 1)

## Design Principles

- **Configuration-Driven Architecture**: All UI is driven by JSON configuration, not hardcoded in code
- **Reactive State Management**: Use StateFlow for live updates and unidirectional data flow  
- **Cross-Platform Consistency**: Shared approach across iOS and Android with platform-appropriate adaptations
- **Design Token System**: Centralized design system with standardized tokens for consistency
- **Component Composition Pattern**: Atomic components with configuration inheritance  
- **Local-First Configuration**: Prioritize local configuration files over network dependencies
- **Type-Safe Bindings**: Safe data binding with JSONPath-style data access
- **Security-First Design**: Built-in protections against injection and invalid inputs  
- **Performance Optimized**: Optimized for fast rendering and efficient memory usage