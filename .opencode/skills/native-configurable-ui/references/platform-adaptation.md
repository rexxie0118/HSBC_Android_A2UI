# Platform Adaptation Guide

Adapt configurable UI components to platform conventions.

## Detection and Abstraction

### Platform Detection

```swift
// iOS - Platform checks
if #available(iOS 17.0, *) {
    // Use new iOS 17 APIs
} else {
    // Fallback
}

#if targetEnvironment(macCatalyst)
    // macOS Catalyst specific
#endif
```

```kotlin
// Android - Platform checks
when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
        // Android 13+ specific
    }
    else -> {
        // Fallback
    }
}

when (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) {
    Configuration.UI_MODE_TYPE_NORMAL -> { /* Phone/Tablet */ }
    Configuration.UI_MODE_TYPE_TELEVISION -> { /* TV */ }
}
```

## Navigation Patterns

### iOS - Platform Navigation

```swift
// Use native navigation patterns
NavigationView {
    List {
        NavigationLink("Detail", destination: DetailView())
    }
}

// iOS 16+ NavigationStack
NavigationStack {
    List {
        NavigationLink("Detail", destination: DetailView())
    }
    .navigationTitle("Items")
    .toolbar {
        ToolbarItem(placement: .primaryAction) {
            Button("Add") { }
        }
    }
}

// Sheet presentation
.sheet(isPresented: $isPresented) {
    SheetView()
        .presentationDetents([.medium, .large])
        .presentationDragIndicator(.visible)
}
```

### Android - Platform Navigation

```kotlin
// Use Material navigation patterns
val navController = rememberNavController()

NavHost(navController = navController, startDestination = "list") {
    composable("list") {
        ListScreen(
            onNavigateToDetail = { id ->
                navController.navigate("detail/$id")
            }
        )
    }
    composable("detail/{id}") { backStackEntry ->
        val id = backStackEntry.arguments?.getString("id")
        DetailScreen(id = id)
    }
}

// Bottom sheet
val sheetState = rememberModalBottomSheetState()
ModalBottomSheet(
    onDismissRequest = { sheetState.hide() },
    sheetState = sheetState,
    dragHandle = { SheetDragHandle() }
) {
    SheetContent()
}
```

## Component Adaptations

### Picker/Spinner

```swift
// iOS - Native Picker
Picker("Select", selection: $selected) {
    ForEach(options) { option in
        Text(option.title).tag(option.id)
    }
}
.pickerStyle(.menu)  // or .wheel, .segmented
```

```kotlin
// Android - Native Dropdown
var expanded by remember { mutableStateOf(false) }
var selected by remember { mutableStateOf(options.first()) }

ExposedDropdownMenuBox(
    expanded = expanded,
    onExpandedChange = { expanded = !expanded }
) {
    OutlinedTextField(
        value = selected.title,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        modifier = Modifier.menuAnchor()
    )
    
    ExposedDropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option.title) },
                onClick = {
                    selected = option
                    expanded = false
                }
            )
        }
    }
}
```

### Date/Time Selection

```swift
// iOS - DatePicker
DatePicker(
    selection: $selectedDate,
    in: dateRange,
    displayedComponents: [.date]
)
.datePickerStyle(.graphical)  // or .wheel, .compact
```

```kotlin
// Android - DatePicker Dialog
var showDatePicker by remember { mutableStateOf(false) }

if (showDatePicker) {
    DatePickerDialog(
        onDismissRequest = { showDatePicker = false },
        confirmButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = { showDatePicker = false }) {
                Text("Cancel")
            }
        }
    ) {
        selectedDate = it
    }
}

Button(onClick = { showDatePicker = true }) {
    Text("Select Date")
}
```

### Search

```swift
// iOS - Native Search Bar
.searchable(text: $searchText, prompt: "Search items")
.autocorrectionDisabled()
```

```kotlin
// Android - Search Bar
var searchText by remember { mutableStateOf("") }

SearchBar(
    query = searchText,
    onQueryChange = { searchText = it },
    onSearch = { /* Handle search */ },
    active = isActive,
    onActiveChange = { isActive = it },
    placeholder = { Text("Search items") },
    leadingIcon = {
        if (isActive) {
            IconButton(onClick = { isActive = false }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        } else {
            Icon(Icons.Default.Search, contentDescription = "Search")
        }
    }
) {
    SearchResults(searchText)
}
```

## Visual Style Adaptations

### iOS - Follow Human Interface Guidelines

```swift
// Use system materials
Material(regularMaterial) {
    Content()
}

// Use system shadows
.shadow(color: .black.opacity(0.1), radius: 8, y: 4)

// Use SF Symbols
Image(systemName: "star.fill")
    .symbolRenderingMode(.hierarchical)
    .font(.system(size: 24, weight: .medium))
```

### Android - Follow Material Design

```kotlin
// Use Material surfaces
Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
) {
    Content()
}

// Use Material shadows
.shadow(
    elevation = 4.dp,
    shape = RoundedCornerShape(8.dp)
)

// Use Material Icons
Icon(
    Icons.Filled.Star,
    contentDescription = "Favorite",
    tint = MaterialTheme.colorScheme.primary
)
```

## Input Method Adaptations

### iOS - Keyboard Types

```swift
TextField("Email", text: $email)
    .keyboardType(.emailAddress)
    .autocapitalization(.none)
    .textInputAutocapitalization(.never)

TextField("Message", text: $message)
    .keyboardType(.default)
    .textInputAutocapitalization(.sentences)
```

### Android - Keyboard Types

```kotlin
OutlinedTextField(
    value = email,
    onValueChange = { email = it },
    label = { Text("Email") },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Email,
        capitalization = KeyboardCapitalization.None,
        autoCorrect = false
    )
)

OutlinedTextField(
    value = message,
    onValueChange = { message = it },
    label = { Text("Message") },
    keyboardOptions = KeyboardOptions(
        keyboardType = KeyboardType.Text,
        capitalization = KeyboardCapitalization.Sentences,
        autoCorrect = true
    )
)
```

## Haptic Feedback

### iOS

```swift
// Light impact for selections
UISelectionFeedbackGenerator().selectionChanged()

// Medium impact for buttons
UIFeedbackGenerator.impactOccurred(.medium)

// Success notification
UINotificationFeedbackGenerator().notificationOccurred(.success)
```

### Android

```kotlin
val haptic = LocalHapticFeedback.current

// Selection
haptic.performHapticFeedback(HapticFeedbackType.LongPress)

// Button click
haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

// Success
haptic.performHapticFeedback(HapticFeedbackType.Confirm)
```

## Responsive Adaptation

### iOS - Size Classes

```swift
@Environment(\.horizontalSizeClass) var horizontalSizeClass
@Environment(\.verticalSizeClass) var verticalSizeClass

var body: some View {
    if horizontalSizeClass == .regular {
        // iPad layout
        HStack { Sidebar(); Content() }
    } else {
        // iPhone layout
        NavigationStack { Content() }
    }
}
```

### Android - Window Size Classes

```kotlin
val windowSizeClass = calculateWindowSizeClass(activity)

when (windowSizeClass.widthSizeClass) {
    WindowWidthSizeClass.Compact -> {
        // Phone layout
        Column { /* Single pane */ }
    }
    WindowWidthSizeClass.Medium, WindowWidthSizeClass.Expanded -> {
        // Tablet layout
        Row { Sidebar(); Content() }
    }
}
```

## Platform-Specific Configuration

```swift
// iOS - Platform config
struct PlatformConfig {
    #if os(iOS)
    static let buttonHeight: CGFloat = 44
    static let cornerRadius: CGFloat = 10
    #elseif os(macOS)
    static let buttonHeight: CGFloat = 21
    static let cornerRadius: CGFloat = 6
    #endif
}
```

```kotlin
// Android - Platform config
object PlatformConfig {
    val buttonHeight: Dp
        @Composable get() = when {
            isTvDevice() -> 56.dp  // TV requires larger touch targets
            isTablet() -> 48.dp
            else -> 44.dp
        }
}
```
