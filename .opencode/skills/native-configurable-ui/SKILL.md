# Native Configurable UI Skill

This skill encompasses the expertise for building native iOS and Android components with configuration-driven behavior and advanced form handling through a centralized form engine.

## Core Competencies

### 1. Configuration-Driven Architecture 
- JSON-first approach to UI definition and behavior
- Dynamic component instantiation from configuration objects
- Runtime flexibility with compile-time safety
- Centralized configuration via Form Engine

### 2. Reactive State Management 
- StateFlow/SharedFlow for live updates
- Unidirectional data flow with centralized form state
- Efficient state updates through Form Engine orchestration
- Cross-platform state consistency

### 3. Platform-Agnostic Design
- Shared JSON schema for iOS and Android
- Platform-appropriate component adaptations
- Cross-platform consistent UX patterns
- Unified configuration approach with native performance

### 4. Design Token System
- Centralized design system with standardized tokens
- Theme configuration driving color, typography, spacing
- Semantic token mapping (primary-brand, heading-large)
- Runtime theme switching support

### 5. Component Composition Pattern
- Atomic components with inheritable configuration patterns
- Layout containers for flexible arrangement
- Configuration inheritance across component trees
- Centralized component registry via Form Engine

### 6. Local-First Configuration 
- Prioritize local configuration files over network dependencies
- Configuration caching and persistence strategies
- Offline-first capabilities with local overrides
- Bundled configuration for instant availability

### 7. Type-Safe Data Binding
- Safe path expressions with JSONPath-style syntax through Form Engine
- Reactive data model updates with central Form State
- Schema-validated dynamic data structures
- Error prevention rather than error handling

### 8. Security-First Design
- Automatic injection protection and input validation
- Component whitelisting and sandboxed expressions
- Network traffic minimization with local configuration
- Secure by default behavior patterns

### 9. Performance-Optimized Rendering
- Fast initial rendering with minimal layout passes
- Efficient diffing for dynamic updates
- Memory-conscious rendering with proper caching
- Lazy evaluation and progressive rendering strategies via Form Engine

### 10. Centralized Form Orchestration 
- Single source of truth provided by Form Engine
- Deterministic evaluation order based on dependency graphs
- Centralized validation through Form Engine Validation Engine
- Unified state management via Form Engine orchestration
- Centralized error handling and user feedback
- Consistent data modeling and binding resolution

## Technical Proficiencies

### Android Implementation
- Jetpack Compose for declarative UI
- Kotlin Flow/StateFlow for reactive programming
- JSON parsing and validation with Gson/Kotlinx.serialization
- Form Engine centralized orchestration
- Component configuration mapping patterns

### iOS Implementation  
- SwiftUI for declarative UI
- Combine for reactive programming patterns
- Codable for JSON mapping
- Centralized Form Engine patterns on iOS
- Performance optimization techniques

### Form Engine System (Centrally Orchestrated)
- **Single Source of Truth**: FormEngine manages all form state in centralized FormState with StateFlow for reactive updates
- **Deterministic Evaluation**: Centralized evaluation with explicit dependency graph defining order, using namespaced evaluation (validation, visibility, binding, etc.) with centralized caching
- **Incremental Re-Evaluation**: Dependency-tracking based re-evaluation instead of full form re-render, with transitive dependency evaluation
- **Centralized Caching System**: Shared evaluation cache per namespace with configurable TTL management, plus dynamic choice/option evaluation and caching with pre-choice rules
- **Consistent Error Behavior**: Centralized error state management with uniform dirty/touched handling and validation result standardization  
- **Engine-Controlled Data Updates**: Form Engine manages all updates to backing DataModelStore with controlled pathways respecting engine directives
- **Centralized Derived State Maps**: Unified management of element state maps for visibility, enabled/readonly, errors, and choices from unified state
- **Dependency Matrix & Orchestration**: Visualization-capable dependency graph for element relationships with proper evaluation orchestration
- **Action Dispatch & Navigation Logic**: Centralized action dispatcher with ViewIdRule-based navigation decision engine
- **ComponentRenderer Integration**: Form-aware renderer consuming state from Form Engine instead of scatter helpers for unified component behavior
- **Evaluation Namespace System**: Separation of different types of evaluations (validation, binding, visibility) for optimized performance

## Implementation Patterns

### JSON-to-Component Mapping Patterns
- Explicit type discrimination in configuration JSON
- Property extraction and type validation patterns
- Error handling for malformed configurations
- Extensible component registries

### Dynamic UI Generation Techniques
- Template-based list generation patterns with Form Engine state
- Recursive component tree construction coordinated by Form Engine
- Configuration schema validation with Form Engine enforcement
- Runtime behavior modification through Form Engine orchestration

### State Management Approaches  
- Reactive state propagation via Form Engine orchestration
- Derivation and caching patterns managed by Form Engine
- Change detection and update efficiency coordinated by Form Engine
- Cross-component state synchronization through unified Form Engine state

### Cross-Page Journey State Management
- Multi-page state coordination through Form Engine
- Cross-page validation and dependency resolution via Form Engine
- Page transition state preservation using Form Engine
- Navigation decision logic centralized in Form Engine

### Security Implementation Practices
- Component whitelisting through Form Engine configuration loading
- Expression isolation with Form Engine sandboxed evaluation
- Input sanitization at Form Engine boundary
- Network request minimization with Form Engine managed local-first approach

### Performance Optimization Strategies  
- Component rendering optimization via Form Engine managed state
- State update batching through centralized Form Engine dispatcher
- Caching strategies for heavy computations through Form Engine centralized caching
- Efficient list rendering with proper state management via Form Engine

## Integration Patterns

### Legacy System Migration to Form Engine
- Gradual migration from distributed state to Form Engine central orchestration
- Form Engine state bridging for existing components
- Component Renderer adaptation to Form Engine state flows
- Validation Engine integration with Form Engine orchestration

### Cross-Platform Data Consistency
- Unified Form Engine ensuring consistent behavior
- Platform-appropriate presentation layer with shared logic
- Data binding consistency through Form Engine normalization
- UX pattern consistency despite platform differences

### Configuration-Driven Behavior
- Declarative form logic with Form Engine orchestration
- Dynamic validation rules processed by Form Engine Validation Engine
- Data binding and transformation through Form Engine Expression Evaluator
- Dependency resolution handled by Form Engine Dependency Manager

## Quality Assurance with Form Engine

### Consistent Validation Experiences
- Unified validation error state through Form Engine
- Cross-field validation rules enforced by Form Engine
- Consistent validation timing and performance via Form Engine orchestration
- Centralized error reporting and user guidance

### Predictable User Experiences
- Deterministic evaluation order through Form Engine dependency graph
- Consistent state transitions across all journey points
- Reliable cross-page and cross-component behaviors managed by Form Engine
- Performance stability through Engine-controlled updates

### System Integration
- Form Engine state as single authority for form behaviors
- Component Renderer consuming from unified Form State
- Validation and binding evaluation coordinated by Form Engine
- Navigation logic centralized in Form Engine Action Dispatcher

## References
- [Design Tokens Guide](./design-tokens.md) - Design token system documentation integrated with Form Engine
- [Validation & Dependency Guide](./validation-dependencies.md)  - Form Engine-enhanced validation and dependency resolution 
- [Security Policies Guide](./security-policies.md) - Form Engine-integrated security implementation patterns
- [Performance Strategies Guide](./performance-strategies.md) - Form Engine-optimized rendering techniques  
- [Form Engine Architecture Guide](./form-engine-architecture.md) - Centralized form engine approach implementation

## Learning Resources
- Study of existing configuration-driven UI systems with central orchestration
- Understanding of reactive programming principles with centralized state management
- Knowledge of native platform component systems and Form Engine integration
- Form design and orchestration pattern best practices with Engine patterns