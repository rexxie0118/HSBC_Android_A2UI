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

### Form Engine System (NEW)
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
- Template-based list generation patterns
- Recursive component tree construction
- Configuration schema validation
- Runtime behavior modification

### State Management Approaches  
- Reactive state propagation via Form Engine
- Derivation and caching patterns
- Change detection and update efficiency
- Cross-component state synchronization

### Security Implementation Practices
- Component whitelisting at configuration load time
- Expression isolation with sandboxed execution environments
- Input sanitization and validation
- Network request minimization strategies

### Performance Optimization Strategies  
- Component rendering optimization via Form Engine
- State update batching through centralized engine
- Caching strategies for heavy computations
- Efficient list rendering with proper recycling

## References
- [Design Tokens Guide](./design-tokens.md) - Design token system documentation
- [Validation & Dependency Guide](./validation-dependencies.md)  - Validation engine and dependency resolution 
- [Security Policies Guide](./security-policies.md) - Security implementation patterns
- [Performance Strategies Guide](./performance-strategies.md) - Rendering optimization techniques  
- [Form Engine Architecture Guide](./form-engine-architecture.md) - New centralized form engine approach

## Learning Resources
- Study of existing configuration-driven UI systems
- Understanding of reactive programming principles
- Knowledge of native platform component systems
- Form design pattern research and best practices