## File Structure

```
app/src/main/java/com/a2ui/renderer/
├── config/                             # Configuration management (ConfigManager)
│   ├── ConfigManager.kt               # MAIN: Orchestrates all configuration loading and provides central services (now includes NEW Form Engine integration)
│   ├── UIConfig.kt                    # CONFIG STRUCTURES: Component and theme data classes  
│   └── ValidationConfig.kt            # VALIDATION CLASSES: Validation rule data structures
├── form/                              # NEW FORM ENGINE CORE (Central orchestration and management)
│   ├── engine/                        # FORM ORCHESTRATION: Main Form Engine and dispatcher
│   │   ├── FormEngine.kt              # CORE ORCHESTRATOR: Central orchestrator managing all form aspects (single source of truth, dependency graph, validation, binding resolution, and navigation)
│   │   └── ActionDispatcher.kt        # ACTION DISPATCH: Navigation and action routing through centralized Form Engine
│   ├── state/                         # FORM STATE MANAGEMENT: Central state structures and validation errors  
│   │   ├── FormState.kt               # STATE STRUCTURE: Unified form state including values, errors, visibility, etc. (single source of truth)
│   │   ├── ValidationError.kt         # ERROR CLASSES: Standardized error representations across all forms  
│   │   └── FormStateFlowProvider.kt   # STATE FLOW: Reactive state flow wrapper with change tracking
│   ├── validation/                    # VALIDATION SYSTEM: Centralized validation engine and evaluation
│   │   ├── ValidationEngine.kt        # VALIDATION LOGIC: Central validation processing with cross-field support (managed by Form Engine)
│   │   └── ValidationRule.kt          # VALIDATION RULES: Rule structures and evaluation helpers 
│   ├── binding/                       # DATA BINDING SYSTEM: Binding resolution and processing
│   │   ├── DataModelStore.kt          # ADAPTER LAYER: Legacy DataModelStore adapting to Form Engine (controlled by Form Engine)
│   │   └── BindingResolver.kt         # BINDING RESOLVER: Centralized path binding resolution (managed by Form Engine)
│   ├── evaluation/                    # EXPRESSION EVALUATION: Central expression processing
│   │   ├── ExpressionEvaluator.kt     # EVALUATION ENGINE: Safe expression evaluation with namespace support (managed by Form Engine)  
│   │   ├── EvaluationNamespace.kt     # NAMESPACE ENUM: Evaluation type categories (validation, visibility, binding, etc.)
│   │   └── EvaluationCache.kt         # EVALUATION CACHE: Namespaced evaluation caching (managed by Form Engine)
│   ├── dependency/                    # DEPENDENCY TRACKING: Element relationship tracking  
│   │   └── DependencyGraph.kt         # DEPENDENCY GRAPH: Directed acyclic graph for element relationships (managed by Form Engine)
│   └── renderer/                      # FORM-AWARE RENDERERS: Components that consume from Form Engine
│       ├── FormAwareComponentRenderer.kt # FORM-AWARE RENDERER: Component rendering based on Form Engine state flow
│       └── JourneyPageRenderer.kt     # PAGE RENDERER: Form Engine-coordinated multi-page journey rendering
├── binding/                           # LEGACY: (Will be phased out in favor of Form Engine binding)  
│   ├── DataModelStore.kt              # STATE PROVIDER: Reactive data model (DEPRECATED - now adapter for Form Engine)
│   └── BindingResolver.kt             # PATH RESOLVER: $path expression resolution (DEPRECATED - centralized in Form Engine)
├── rules/                             # LEGACY: (Will be replaced by Form Engine validation/dependency) 
│   ├── ValidationEngine.kt            # VALIDATION LOGIC: Input validation rules (REPLACED - by Form Engine ValidationEngine)
│   ├── DependencyResolver.kt          # FIELD RELATIONSHIPS: Field dependencies (REPLACED - by Form Engine DependencyGraph)
│   ├── ExpressionEvaluator.kt         # SAFE EXPRESSIONS: Expression evaluation (REPLACED - by Form Engine ExpressionEvaluator)
│   └── NativeFunctionRegistry.kt      # NATIVE FUNCTIONS: Function bridges (INTEGRATED - into Form Engine)
├── renderer/                          # COMPONENT RENDERERS: Form-aware renderers
│   ├── ComponentRenderer.kt           # MAIN RENDERER: Now consumes from FormEngine instead of old helpers (Form-Aware)
│   └── ListTemplateRenderer.kt        # LIST RENDERER: Now uses FormEngine for rendering (Form-Aware)
├── ui/                                # UI PRESENTATION LAYER: Themes and theme UI
│   ├── theme/
│   │   ├── Theme.kt                   # COLOR/TYPE MAPPING: Dynamic theme building from JSON (accesses Form Engine for changes)
│   │   ├── Color.kt                   # COLOR DEFINITIONS: Material color scheme mapping
│   │   └── Type.kt                    # TYPOGRAPHY: Material typography mapping 
│   └── components/
│       └── ThemePicker.kt             # THEME UI: Theme selection UI component (communicates with Form Engine)
├── data/                              # DATA PERSISTENCE: Settings and persistence
│   └── PreferencesManager.kt          # SETTINGS: Theme and settings persistence (integration with Form Engine)
└── journey/                           # NEW JOURNEY LAYER: Multi-page navigation coordination
    ├── JourneyManager.kt              # ROUTE MANAGEMENT: NEW - Multi-page journey coordination now delegates to Form Engine (Form Engine centralizes journey state)
    └── PageLifecycle.kt               # LIFECYCLE: NEW - Page lifecycle coordination (Form Engine handles state persistence)
```

### Detailed Component Descriptions

#### Form Engine Core Components (NEW CENTRAL ARCHITECTURE)

**FormEngine.kt (CORE ORCHESTRATOR)**
Location: `app/src/main/java/com/a2ui/renderer/form/engine/FormEngine.kt`

NEW: Central orchestrator implementing ALL single source of truth, evaluation order, validation, dependency tracking, caching, and navigation decisions:
- **FormState Management**: Maintains single unified FormState as source of truth
- **Dependency Graph Integration**: Coordinates with DependencyGraph for evaluation order  
- **Central Validation Engine**: All validation processed through centralized system
- **Unified Expression Evaluation**: Namespaced evaluation with centralized caching
- **Action Dispatch Coordination**: Navigation and action decisions through ViewIdRule system
- **Form-Aware Component Integration**: All components rendered through Form Engine state
- **Engine-Controlled Updates**: All data model updates channeled through Form Engine
- **Consistency Orchestration**: Central visibility/enablement/error/choice management

**ActionDispatcher.kt (ACTION ORCHESTRATION)**  
Location: `app/src/main/java/com/a2ui/renderer/form/engine/ActionDispatcher.kt`

NEW: Coordinating all navigation and form actions through Form Engine:
- Central navigation decisions based on form state
- ViewIdRule-based routing systems
- Action processing through Form Engine state changes
- Consistent action handling across all journey elements

#### Form State Management (NEW UNIFIED STATE)

**FormState.kt (UNIFIED STATE STRUCTURE)**  
Location: `app/src/main/java/com/a2ui/renderer/form//state/FormState.kt`

NEW: Single data structure containing ALL form-related state for unified management:
- **values**: Complete map of element ID → form value  
- **dirtyFlags**: Element ID → dirty status (modified since initial load)
- **touchedFlags**: Element ID → touched status (user interacted with)
- **errors**: Element ID → List of ValidationError objects
- **visibility**: Element ID → visibility status (shown/hidden)
- **enabled**: Element ID → enablement status (enabled/disabled)
- **choices**: Element ID → dynamic choice options
- **timestamps**: Element ID → last modified timestamp

**FormStateFlowProvider.kt (REACTIVE PROVIDER)**
Location: `app/src/main/java/com/a2ui/renderer/form/state/FormStateFlowProvider.kt` 

NEW: Reactive StateFlow wrapper for centralized form state access:
- **formState**: StateFlow exposing unified FormState
- **updateState**: Thread-safe state update function via transformation
- **changeTracking**: Integrated with lifecycle for reactive component updates

#### Validation Engine (NEW CENTRAL VALIDATION)

**ValidationEngine.kt (CENTRAL VALIDATION PROCESSING)**  
Location: `app/src/main/java/com/a2ui/renderer/form/validation/ValidationEngine.kt`

NEW: All validation logic processed via centralized engine:
- **validateField**: Central validation for all fields
- **validateAll**: Comprehensive form validation  
- **crossFieldValidation**: Dependency-aware validation
- **customValidation**: Centralized function bridge support
- **validationStateManagement**: Consistent error behavior across all elements

#### Dependency Management (NEW CENTRAL GRAPH) 

**DependencyGraph.kt (RELATIONSHIP TRACKING)**  
Location: `app/src/main/java/com/a2ui/renderer/form/dependency/DependencyGraph.kt`

NEW: Centralized graph tracking element relationships:
- **elementDependsOn**: Forward dependency tracking  
- **elementsThatDependOn**: Reverse dependency tracking
- **getTransitivelyAffectedElements**: Transitive dependency evaluation
- **dependencyMatrixVisualization**: Visualizable dependency graph
- **evaluationOrchestration**: Proper sequencing for dependent elements

#### Expression Evaluation (NEW CENTRAL CACHING)  

**ExpressionEvaluator.kt (SAFE EVALUATION)**  
Location: `app/src/main/java/com/a2ui/renderer/form/evaluation/ExpressionEvaluator.kt`

NEW: Centralized expression evaluation with proper security and caching:
- **evaluateWithNamespace**: Namespaced expression evaluation 
- **validationExpression**: Safe validation rule evaluation
- **visibilityExpression**: Safe visibility condition evaluation
- **bindingResolution**: Safe path binding evaluation
- **evaluationSecurity**: Blocklist-based security validation

**EvaluationCache.kt (CENTRAL CACHING)**  
Location: `app/src/main/java/com/a2ui/renderer/form/evaluation/EvaluationCache.kt`

NEW: Namespaced caching for all expression evaluations:
- **namespaceCaching**: Different caching policies per evaluation type
- **expirationPolicy**: Time-based cache invalidation
- **dependencyInvalidation**: Cache invalidation based on dependencies
- **performanceOptimization**: Efficient cache retrieval across all evaluation types

#### Form-Aware Renderers (NEW CONSUMPTION MODEL)

**FormAwareComponentRenderer.kt (FORM-CENTERED RENDERING)**  
Location: `app/src/main/java/com/a2ui/renderer/form/renderer/FormAwareComponentRenderer.kt`

NEW: Components that consume from Form Engine state rather than distributed helpers:
- **consumeFromFormEngine**: All component state from unified FormState flow  
- **dispatchToUpdateEngine**: All interactions directed to Form Engine
- **reactiveRendering**: Proper reactivity to central state changes
- **unifiedBehavior**: Consistent component behavior across all implementations

**JourneyPageRenderer.kt (JOURNEY-INTEGRATED RENDERING)**  
Location: `app/src/main/java/com/a2ui/renderer/form/renderer/JourneyPageRenderer.kt`

NEW: Page rendering coordinated by Form Engine:
- **formEngineIntegration**: Page state coordinated through Form Engine
- **multiPageState**: Journey-wide versus page-local state distinction
- **navigationCoordination**: FormEngine-managed navigation decisions
- **validationConsistency**: Cross-page validation rules respected

#### Legacy Component Adaptation (PHASING OUT)

**DataModelStore.kt (ADAPTER LAYER)**  
Location: `app/src/main/java/com/a2ui/renderer/binding/DataModelStore.kt`

NEW: Legacy adapter still present for backwards compatibility:
- **formEngineAdapter**: Adapter layer that defers to Form Engine for updates
- **deprecatedPathMethods**: Maintained for transition but Form Engine preferred
- **migrationPath**: Clear indicators of what should migrate to Form Engine patterns

**JourneyManager.kt (JOURNEY ORCHESTRATION)**  
Location: `app/src/main/java/com/a2ui/renderer/journey/JourneyManager.kt`

NEW: Multi-page journey coordination now delegates to Form Engine:
- **formEngineCoordinated**: Now mainly coordinates Form Engine multi-page state
- **navigationDelegation**: Navigation decisions processed through Form Engine
- **statePreservation**: Form Engine now manages cross-page state persistence
- **legacyCompatibility**: Maintains interface but Form Engine does the heavy lifting