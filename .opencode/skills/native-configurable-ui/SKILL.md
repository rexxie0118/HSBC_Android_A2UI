---
name: native-configurable-ui
description: Configurable UI component system for native iOS (SwiftUI/UIKit) and Android (Jetpack Compose/Views). Use when: (1) Building reusable component libraries, (2) Implementing design tokens or theming systems, (3) Creating cross-platform UI with platform adaptation, (4) Developing configuration-driven components with JSON/runtime customization
license: MIT
---

# Native Configurable UI Development Guide

Build scalable, themeable UI component libraries for iOS and Android with configuration-driven architecture.

## Reference Files

Read these based on your task:

| File | When to Read |
|---|---|
| `references/design-tokens.md` | Need color, spacing, typography tokens |
| `references/theming.md` | Implementing theme switching or dark mode |
| `references/ios-swiftui.md` | Building iOS SwiftUI components |
| `references/android-compose.md` | Building Android Jetpack Compose components |
| `references/component-structure.md` | Designing config objects, builders, validation |
| `references/platform-adaptation.md` | Adapting UI to iOS vs Android conventions |

## Core Principles

### 1. Configuration-Driven Architecture
- All UI appearance and behavior is defined in configuration files, not hardcoded in code
- Runtime flexibility through JSON-based component definitions and theming
- Separation of presentation logic from business logic via declarative structures
- Support for dynamic updates without app redeployment

### 2. Reactive State Management
- Use of StateFlow or similar reactive patterns for live UI updates
- Unidirectional data flow from configuration → state → UI
- Centralized state management with observability via reactive streams
- Efficient change propagation without unnecessary recompositions

### 3. Cross-Platform Consistency
- Shared design tokens and component APIs across iOS and Android
- Unified configuration schemas regardless of target platform
- Platform-appropriate adaptations while maintaining consistent behavior
- Configuration-driven approach ensures visual consistency across platforms

### 4. Design Token System
- Centralized design system with standardized color, spacing, typography tokens
- Theme-agnostic components that adapt to current token values
- Maintained accessibility standards (contrast ratios, touch targets)
- Semantic naming to abstract underlying implementation details

### 5. Component Composition Pattern
- Atomic component design with configuration inheritance
- Flexible slot-based composition for extensibility
- Consistent configuration interfaces across all component types
- Reusable component configuration with validation patterns

### 6. Local-First Configuration
- Prioritize local configuration files over server-dependent features
- Support for offline-first scenarios without compromising functionality
- Efficient caching mechanisms for configuration assets
- Graceful degradation when network resources unavailable

### 7. Type-Safe Bindings
- Safe binding resolution with path-based data access
- Compile-time safety with runtime flexibility in data binding
- Hierarchical data access patterns for complex object structures
- Error isolation to prevent binding failures from affecting entire UI

### 8. Security-First Design
- Component whitelisting to prevent unauthorized element rendering
- Expression sandboxing with restricted function availability
- Validation and sanitization of all configuration inputs
- Content security policies limiting external resource access

### 9. Performance Optimized
- Lazy loading and view recycling for efficient memory usage
- Incremental updates and diffing to minimize UI recomposition
- Batching of frequent changes to prevent rendering jank
- Asynchronous operations to maintain responsive UI interactions
