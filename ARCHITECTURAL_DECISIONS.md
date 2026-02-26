# Architectural Decisions

## Technology Architecture

### Multi-Domain Model Architecture
The system implements an event-driven architecture with multiple domain models managing unified data, caching, updates and API interactions efficiently using:

```
┌─────────────────────────────────────────────────────────────────┐
│        Event-Driven Multi-Domain Architecture                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────┐      ┌──────────────────────────────┐       │
│  │ DOMAIN MODEL │─────▶│ EVENT DRIVEN PATH MAPPER     │       │
│  │   - Account  │      │                              │       │
│  │   - Products │      │ • Route-based event dispatch │       │
│  │   - User     │      │ • JSON-driven configuration  │       │
│  │              │      │ • Processor orchestration    │       │
│  └──────────────┘      └─────────────┬────────────────┘       │
│                                      │                        │
│                       ┌──────────────▼───────────────┐       │
│                       │  PROCESSORS W/ JSONPATH     │       │
│                       │ • AccountDataProcessor       │       │
│                       │ • UserDataProcessor          │       │
│                       │ • ProductDataProcessor       │       │
│                       │ • Advanced data transformers │       │
│                       └──────────────┬───────────────┘       │
│                                      │                        │
│                      ┌───────────────▼──────────┐           │
│                 ┌────► PATH-BASED OBSERVATION    │           │
│                 │    │ SYSTEM                     │           │
│                 │    │ • Subscription paths       │           │
│                 │    │ • Change notifications     │           │
│                 │    │ • Type-safe callbacks      │           │
│        ┌────────┼────┴─────────────┬─────────────┴────────┐ │
│        │    ┌──▼────────┐   ┌─────▼────────┐   ┌────────▼─┴─┐
│        │    │OBSERVER 1  │   │OBSERVER 2    │   │OBSERVER N  │
│        │    │ Account    │   │ User         │   │ Products   │
│        └────┴────────────┘   └──────────────┘   └────────────┘
│                                                                 │
│         JSON CONFIGURATION (dynamic runtime modification)        │
│         {                                                        │
│           "/api/v2/accounts/*": { "class": "AccountProcessor" },│
│           "/api/v3/users/*": { "class": "UserProcessor" },      │
│           "...new_paths": { "class": "NewProcessor" }           │
│         }                                                        │
└─────────────────────────────────────────────────────────────────┘
```

### Data Change Events
Each domain model uses type-safe data mutation events with comprehensive change tracking:

- `PropertyAdded`: When a new property is added
- `PropertyUpdated`: When a property value changes
- `PropertyRemoved`: When a property is removed
- `DataInitialized`: When data is initialized
- `DataReset`: When data is reset

### Path Matching Strategies
The system supports multiple path matching algorithms:
- Generic path matcher supporting wildcards
- JSONPath-style matcher for nested property matching
- Wildcard matcher for common API path patterns

## Data Flow Architecture

### StateFlow Diagram
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
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │ themeFlow: StateFlow<Theme?>                             │  │
│  │ • Emits when theme changes                               │  │
│  │ • Observed by A2UIRendererTheme                          │  │
│  │ • Persists to SharedPreferences                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────┬────────────────────────────────────────────┘
                     │ themeFlow.collectAsState()
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                   A2UIRendererTheme (Composable)                │
│  • Observes themeFlow                                          │
│  • Builds ColorScheme from theme.colors                        │
│  • Builds Typography from theme.typography                     │
│  • Provides MaterialTheme to children                          │
└────────────────────┬────────────────────────────────────────────┘
                     │ MaterialTheme.current
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│                    Component Renderers                          │
│  renderText() │ renderCard() │ renderButton() │ renderList()  │
│  • Access MaterialTheme.colorScheme                            │
│  • Access MaterialTheme.typography                             │
│  • Resolve bindings via BindingResolver                        │
└────────────────────┬────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────────┐
│              DataModelStore + BindingResolver                   │
│  ┌──────────────────┐         ┌─────────────────────────────┐  │
│  │ _data:StateFlow  │◄───────►│ resolve("$.user.name")      │  │
│  │ setData()        │         │ updateAtPath()              │  │
│  │ getAtPath()      │         │ resolveText()               │  │
│  └──────────────────┘         └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

## Security Architecture

### Defense-in-Depth Strategy
The A2UI Renderer follows a defense-in-depth security approach with 8 core security policies:

```
┌─────────────────────────────────────────────────────────────────┐
│                    A2UI Security Framework                      │
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
- Only explicitly whitelisted components can be rendered
- Enforced at: ConfigManager.parseComponentConfig(), ComponentRenderer.renderComponent(), ListTemplateRenderer.RenderList()
- Violation Response: SecurityException with detailed logging

### Policy 2: Sandboxed Logic Execution
- All logic execution must be sandboxed with no access to external systems
- Blocking operations: Network calls, File system, Process spawning
- Limits: Memory (50MB), CPU (1000ms timeout), Iterations (10,000)

### Policy 3: Restricted Expressions
- Only safe, declarative expressions allowed, no code execution
- Blocking patterns: Function calls, object creation, lambda functions
- Allowed expressions: Property access, comparisons, logical operators

### Policy 4: No Dynamic Scripts
- Absolutely no dynamic script execution from configuration
- Blocking: <script> tags, JavaScript URIs, event handlers

### Policy 5: Declarative Interactions
- Complex logic must be declarative, not imperative
- Examples: Instead of onClick="runScript()", use action: { event: "navigate", destination: "page2" }

### Policy 6: Content Security Policy
- All content sources must be explicitly whitelisted
- Blocked: Scripts, inline styles, data: URLs

## Performance Architecture

### Multi-Level Optimization Strategy
```
┌─────────────────────────────────────────────────────────────────┐
│              A2UI Performance Optimization Framework             │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │ 1. Streaming &   │  │ 2. Efficient     │                   │
│  │ Native Rendering │  │ Parsing Pipeline │                   │
│  └──────────────────┘  └──────────────────┘                   │
│                                                                 │
│  ┌──────────────────┐  ┌──────────────────┐                   │
│  │ 3. Memory        │  │ 4. Startup & UX  │                   │
│  │ Management       │  │ Optimization     │                   │
│  └──────────────────┘  └──────────────────┘                   │
│                                                                 │
│  ┌──────────────────┐                                          │
│  │ 5. Resource      │                                          │
│  │ Optimization     │                                          │
│  └──────────────────┘                                          │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Strategy 1: Streaming & Native Rendering
- Minimize latency by rendering content as it arrives
- Using Android's JsonReader for incremental parsing
- Native rendering pipeline with platform-native primitives

### Strategy 2: Efficient Parsing & Rendering Pipeline
- Diffing & incremental updates to minimize re-renders
- Lazy loading integration with efficient component diffing
- Memory-efficient parsing techniques

### Strategy 3: Memory Management
- View recycling with component pooling
- Level 3 caching: Memory cache (LruCache), Disk cache (DiskLruCache), Network
- Async resource loading to prevent UI thread blocking
- Backpressure handling to prevent UI overload

### Strategy 4: Startup & User Experience
- Bundled common components for instant availability
- Skeletron screen loaders to eliminate white screens during loading

### Strategy 5: Resource Optimization
- Connection reuse with persistent connections (WebSockets/SSE)
- Smart idling when app is backgrounded or idle

## Component Architecture

### Theme System Architecture
The theme system enables full JSON-driven theming with runtime switching:

- Theme JSON connects to Compose MaterialTheme
- Dynamic ColorScheme builder from Theme data class
- Global theme persistence via SharedPreferences
- Reactive StateFlow updates

### Data Binding Architecture
Supports both path-only and path with literal binding modes:

- DataModelStore holds runtime data per surface/page
- BindingResolver implements path resolution
- Two-way binding via updateWithLiteral()
- Nested path traversal with $.user.profile.name syntax

### Dynamic List Architecture
Implements A2UI-compliant list templates:

- ChildrenTemplate data class for template configuration
- DataBinding property specifies array to iterate over
- ComponentId identifies template component
- ItemVar creates scoped data context per item
- LazyColumn/LazyRow for efficient scrolling

## State Management Architecture

### Multi-Domain Data Management
```
┌─────────────────────────────────────────────────────────────────┐
│                Multi-Domain Model Manager                       │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ AccountModel    │  │ UserModel       │  │ ProductModel    │ │
│  │ Manager         │  │ Manager         │  │ Manager         │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│           │                     │                      │        │
│           ▼                     ▼                      ▼        │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ Event Mapper    │  │ Path Resolution │  │ Domain Store    │ │
│  │ & Routing       │  │ & Matching      │  │ Interface       │ │
│  └─────────────────┘  └─────────────────┘  └─────────────────┘ │
│                           │                           │        │
│                           ▼                           ▼        │
│                     ┌─────────────────┐      ┌─────────────────┐│
│                     │ Domain          │◄─────┤ Reactive        ││
│                     │ Processors      │      │ State Stores    ││
│                     └─────────────────┘      └─────────────────┘│
└─────────────────────────────────────────────────────────────────┘
```

Each domain manages its own: model structure, validation rules, business logic, and update patterns separately but integrated through the central path mapper that routes data and events appropriately.

## Design Patterns Used

### Observer Pattern (Event-Driven)
Used throughout for:
- Theme changes (StateFlow/collectAsState)
- Data model updates
- Cross-component dependencies
- Multi-domain communication

### Factory Pattern
Used for:
- Component creation and registration
- Theme configuration builders
- Network request managers

### Repository Pattern
Used for:
- Data model stores
- Network layer abstraction
- Caching strategies