# Change Name Journey - Implementation Plan

## Overview

This document defines the implementation plan for the "Change My ID or Name" journey based on the design specification in `Change_Name.jpg`.

---

## Entry Point

**Main Page → Menu Button → Digital Forms → Change Name Journey**

```
Homepage → Tap Menu Icon → Select "Digital Forms" → Change Name Journey
```

**Implementation**:
- Add "Digital Forms" option to existing menu/navigation
- Menu button already exists in top navigation (hamburger menu icon)
- On tap → Navigate to `digital_forms` page (Screen 1)

---

## Updated Journey Flow

```
Homepage → Menu → Screen 1 → Screen 2 → Screen 3 → Screen 4
             ↓         ↓         ↓          ↓         ↓
          Digital   Selection  Change    Acknowl-    Home/
          Forms     Confirmed  Name      edgement    Close
                                Form      Page
```

---

## Journey Flow

```
Screen 1: Entry Point → Screen 2: Selection → Screen 3: Form → Screen 4: Acknowledgement
     ↓              ↓              ↓                 ↓
  Digital       Dropdown       Change Name       Thank You
  Forms         Bottom         Form (4          / Acknowledge
  Menu          Sheet          fields)          Page
```

---

## Screen Breakdown

### Screen 1: Digital Forms (Entry Point)

**Purpose**: Main entry point for digital forms selection (accessed from main page menu)

**Navigation**: 
- From: Homepage menu button (hamburger icon in top nav)
- Action: `navigate_digital_forms`

**Components**:
- Back button (navigation) → Returns to homepage
- Title: "Digital forms"
- Subtitle: "New request"
- Description text
- Dropdown: "What do you want to do?"
- Options:
  - Update my signature
  - Change my ID or name ✓
  - Change address for a joint account
  - Update my FATCA form
- Continue button (red)

**Interaction**: 
- User taps dropdown → Bottom sheet appears
- User selects "Change my ID or name"
- Continue button becomes active
- User taps Continue → Navigate to Screen 2

---

### Screen 2: Selection Confirmed

**Purpose**: Show selected option before proceeding

**Components**: Same as Screen 1
- Dropdown shows selected value: "Change my ID or name"
- Continue button enabled (red)

**Interaction**:
- User taps Continue → Navigate to Screen 3

---

### Screen 3: Change Name Form

**Purpose**: Main form for changing ID or name

**Components**:
- Back button
- Title: "Change name"
- Instruction text: "Please make sure your new name matches that on your passport."
- Input Field 1: First name (text field, 0/100 chars)
- Input Field 2: Last name (text field, 0/100 chars)
- Divider text: "OR"
- Input Field 3: Identification type (dropdown, bottom sheet)
  - Options: NRIC ✓, Passport, Malaysian MyCard, FIN
- Input Field 4: ID Number (text field, 0/9 chars, info icon)
- Continue button (red)

**Interactions**:
- First name / Last name: Text input with character count
- ID type dropdown: Opens bottom sheet with options
- ID Number: Text input with format validation
- Continue: Validates form → Navigate to Screen 4

---

### Screen 4: Acknowledgement

**Purpose**: Confirmation page

**Components**:
- Back button
- Title: "Thank you" (or similar)
- Message text
- Close button

**Interaction**:
- Close → Return to previous screen or home

---

## JSON Structure

### Menu Integration (Homepage)

**Add to existing menu/navigation options**:

```json
{
  "id": "menu_digital_forms",
  "type": "Text",
  "properties": {
    "text": {"literalString": "Digital Forms"},
    "usageHint": "body"
  },
  "action": {
    "event": "navigate",
    "context": {
      "destination": "digital_forms"
    }
  }
}
```

### Journey Configuration

```json
{
  "type": "journey",
  "id": "change_name_journey",
  "name": "Change My ID or Name",
  "pages": [
    "digital_forms",
    "change_name_form",
    "acknowledgement"
  ],
  "defaultPage": "digital_forms"
}
```

### Page 1: Digital Forms

```json
{
  "type": "page",
  "id": "digital_forms",
  "journeyId": "change_name_journey",
  "components": [
    {
      "id": "header",
      "type": "Row",
      "properties": {
        "alignment": "center",
        "children": {
          "explicitList": ["back_button", "title"]
        }
      }
    },
    {
      "id": "back_button",
      "type": "Icon",
      "properties": {
        "icon": "arrow_left"
      },
      "action": {
        "event": "navigate_back"
      }
    },
    {
      "id": "title",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Digital forms"},
        "usageHint": "h5"
      }
    },
    {
      "id": "subtitle",
      "type": "Text",
      "properties": {
        "text": {"literalString": "New request"},
        "usageHint": "body"
      }
    },
    {
      "id": "description",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Select the reason for sharing your document so we can guide you to the correct requirements and process your documents quickly."},
        "usageHint": "body2",
        "color": "#666666"
      }
    },
    {
      "id": "dropdown_label",
      "type": "Text",
      "properties": {
        "text": {"literalString": "What do you want to do?"},
        "usageHint": "body"
      }
    },
    {
      "id": "dropdown",
      "type": "Dropdown",
      "properties": {
        "placeholder": {"literalString": "Please select"},
        "options": [
          {"label": "Update my signature", "value": "signature"},
          {"label": "Change my ID or name", "value": "id_name"},
          {"label": "Change address for a joint account", "value": "address"},
          {"label": "Update my FATCA form", "value": "fatca"}
        ],
        "displayMode": "bottomSheet"
      },
      "validation": {
        "required": true
      }
    },
    {
      "id": "continue_button",
      "type": "Button",
      "properties": {
        "text": {"literalString": "Continue"},
        "primary": true,
        "fullWidth": true
      },
      "action": {
        "event": "navigate",
        "context": {
          "destination": "change_name_form"
        }
      },
      "dependencies": {
        "enabled": {
          "rule": "$.dropdown.value != null"
        }
      }
    }
  ]
}
```

### Page 2: Change Name Form

```json
{
  "type": "page",
  "id": "change_name_form",
  "journeyId": "change_name_journey",
  "components": [
    {
      "id": "header",
      "type": "Row",
      "properties": {
        "alignment": "center",
        "children": {
          "explicitList": ["back_button", "title"]
        }
      }
    },
    {
      "id": "back_button",
      "type": "Icon",
      "properties": {
        "icon": "arrow_left"
      },
      "action": {
        "event": "navigate_back"
      }
    },
    {
      "id": "title",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Change name"},
        "usageHint": "h5",
        "color": "#0066CC"
      }
    },
    {
      "id": "instruction",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Please make sure your new name matches that on your passport."},
        "usageHint": "body2",
        "color": "#666666"
      }
    },
    {
      "id": "first_name_label",
      "type": "Text",
      "properties": {
        "text": {"literalString": "First name"},
        "usageHint": "body"
      }
    },
    {
      "id": "first_name",
      "type": "TextField",
      "properties": {
        "textFieldType": "shortText",
        "placeholder": {"literalString": ""},
        "maxLength": 100
      },
      "validation": {
        "required": true,
        "rules": [
          {"type": "maxLength", "value": 100, "message": {"literalString": "Max 100 characters"}}
        ]
      }
    },
    {
      "id": "last_name_label",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Last name"},
        "usageHint": "body"
      }
    },
    {
      "id": "last_name",
      "type": "TextField",
      "properties": {
        "textFieldType": "shortText",
        "maxLength": 100
      },
      "validation": {
        "required": true,
        "rules": [
          {"type": "maxLength", "value": 100, "message": {"literalString": "Max 100 characters"}}
        ]
      }
    },
    {
      "id": "or_divider",
      "type": "Row",
      "properties": {
        "alignment": "center",
        "children": {
          "explicitList": ["line1", "or_text", "line2"]
        }
      }
    },
    {
      "id": "or_text",
      "type": "Text",
      "properties": {
        "text": {"literalString": "OR"},
        "usageHint": "body",
        "color": "#999999",
        "padding": {"all": 8}
      }
    },
    {
      "id": "id_type_label",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Identification type"},
        "usageHint": "body"
      }
    },
    {
      "id": "id_type",
      "type": "Dropdown",
      "properties": {
        "placeholder": {"literalString": "Select"},
        "options": [
          {"label": "NRIC", "value": "nric"},
          {"label": "Passport", "value": "passport"},
          {"label": "Malaysian MyCard", "value": "mycard"},
          {"label": "FIN", "value": "fin"}
        ],
        "displayMode": "bottomSheet"
      },
      "validation": {
        "required": true
      }
    },
    {
      "id": "id_number_label",
      "type": "Row",
      "properties": {
        "alignment": "center",
        "children": {
          "explicitList": ["id_number_text", "info_icon"]
        }
      }
    },
    {
      "id": "id_number_text",
      "type": "Text",
      "properties": {
        "text": {"literalString": "ID Number"},
        "usageHint": "body"
      }
    },
    {
      "id": "info_icon",
      "type": "Icon",
      "properties": {
        "icon": "info",
        "tintColor": "#999999",
        "size": 16
      }
    },
    {
      "id": "id_number",
      "type": "TextField",
      "properties": {
        "textFieldType": "shortText",
        "maxLength": 9
      },
      "validation": {
        "required": true,
        "rules": [
          {"type": "pattern", "pattern": "^[A-Z0-9]{1,9}$", "message": {"literalString": "Invalid ID format"}},
          {"type": "maxLength", "value": 9, "message": {"literalString": "Max 9 characters"}}
        ]
      }
    },
    {
      "id": "continue_button",
      "type": "Button",
      "properties": {
        "text": {"literalString": "Continue"},
        "primary": true,
        "fullWidth": true
      },
      "action": {
        "event": "navigate",
        "context": {
          "destination": "acknowledgement"
        }
      },
      "dependencies": {
        "enabled": {
          "rule": "$.first_name.value != null && $.first_name.value.length > 0 && $.last_name.value != null && $.last_name.value.length > 0 && ($.id_type.value != null || $.id_number.value != null)"
        }
      }
    }
  ]
}
```

### Page 3: Acknowledgement

```json
{
  "type": "page",
  "id": "acknowledgement",
  "journeyId": "change_name_journey",
  "components": [
    {
      "id": "back_button",
      "type": "Icon",
      "properties": {
        "icon": "arrow_left"
      },
      "action": {
        "event": "navigate_back"
      }
    },
    {
      "id": "thank_you_icon",
      "type": "Icon",
      "properties": {
        "icon": "check_circle",
        "tintColor": "#00AA00",
        "size": 64
      }
    },
    {
      "id": "thank_you_title",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Thank you"},
        "usageHint": "h4"
      }
    },
    {
      "id": "message",
      "type": "Text",
      "properties": {
        "text": {"literalString": "Your request has been submitted successfully."},
        "usageHint": "body",
        "color": "#666666"
      }
    },
    {
      "id": "close_button",
      "type": "Button",
      "properties": {
        "text": {"literalString": "Close"},
        "primary": true,
        "fullWidth": true
      },
      "action": {
        "event": "navigate_home"
      }
    }
  ]
}
```

---

## Implementation Tasks

### Phase 0: Menu Integration (1-2 hours)

- [ ] Add "Digital Forms" option to existing menu
- [ ] Add navigation action from menu to digital forms page
- [ ] Test menu button interaction
- [ ] Verify navigation from homepage

### Phase 1: Core Components (4-6 hours)

- [ ] Create Dropdown component with bottom sheet support
- [ ] Add bottom sheet animation
- [ ] Implement TextField with character count
- [ ] Add form validation for all fields
- [ ] Implement Continue button enable/disable logic

### Phase 2: Journey Integration (2-3 hours)

- [ ] Create journey JSON files
- [ ] Add navigation between pages
- [ ] Implement form data persistence across pages
- [ ] Add back button handling

### Phase 3: Validation & Testing (2-3 hours)

- [ ] Add validation rules (required, pattern, maxLength)
- [ ] Test form submission flow
- [ ] Test bottom sheet interactions
- [ ] Test navigation flow
- [ ] Capture screenshots

---

## Files to Create

```
app/src/main/res/raw/
├── change_name_journey.jsonl        # Journey config
├── digital_forms_page.jsonl         # Page 1
├── change_name_form_page.jsonl      # Page 2
└── acknowledgement_page.jsonl       # Page 3

app/src/main/java/com/a2ui/renderer/components/
└── Dropdown.kt                       # NEW - Dropdown with bottom sheet
```

---

## Component Specifications

### Dropdown Component

```kotlin
@Composable
fun Dropdown(
    component: ComponentConfig,
    onAction: (String, Map<String, Any>?) -> Unit,
    onNavigate: (String) -> Unit
) {
    val props = component.properties ?: return
    val options = props.options ?: emptyList()
    val displayMode = props.displayMode ?: "inline" // or "bottomSheet"
    
    var expanded by remember { mutableStateOf(false) }
    var selectedValue by remember { mutableStateOf<String?>(null) }
    
    if (displayMode == "bottomSheet") {
        // Show selected value or placeholder
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(selectedValue ?: props.placeholder?.literalString ?: "")
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Open dropdown")
        }
        
        // Bottom sheet
        if (expanded) {
            BottomSheet(
                options = options,
                onOptionSelected = { value ->
                    selectedValue = value
                    expanded = false
                    // Update data model
                },
                onDismiss = { expanded = false }
            )
        }
    }
}
```

---

## Success Criteria

- [ ] Menu button on homepage navigates to Digital Forms
- [ ] All 4 screens implemented and match design
- [ ] Dropdown bottom sheet works smoothly
- [ ] Form validation prevents submission with empty fields
- [ ] Character count displays correctly (0/100, 0/9)
- [ ] Continue button enabled/disabled based on form state
- [ ] Navigation flow works (forward and back)
- [ ] Form data persists across screens
- [ ] Acknowledgement page displays on completion
- [ ] Back button from any screen returns to previous screen or homepage

---

## Estimated Effort

| Phase | Tasks | Hours |
|-------|-------|-------|
| **Phase 0** | Menu Integration | 1-2h |
| **Phase 1** | Core Components | 4-6h |
| **Phase 2** | Journey Integration | 2-3h |
| **Phase 3** | Validation & Testing | 2-3h |
| **Total** | | **9-14 hours** |

---

## References

- Design: [Change_Name.jpg](Change_Name.jpg)
- Spec: [Change_Name.md](Change_Name.md)
- A2UI Components: [JSON_COMPARISON_ANALYSIS.md](JSON_COMPARISON_ANALYSIS.md)
- Architecture: [architecture.md](architecture.md)
- Homepage: [app/src/main/res/raw/homepage_components.jsonl](app/src/main/res/raw/homepage_components.jsonl)
- Navigation: [app/src/main/java/com/a2ui/renderer/ui/NavigationHost.kt](app/src/main/java/com/a2ui/renderer/ui/NavigationHost.kt)

---

## Next Steps

1. **Review**: Confirm JSON structure matches design
2. **Phase 0**: Add "Digital Forms" to homepage menu
3. **Phase 1**: Implement Dropdown component with bottom sheet
4. **Phase 2**: Create journey JSON files and navigation
5. **Phase 3**: Test complete flow on emulator
6. **Deploy**: Capture screenshots

---

## Complete Flow Diagram

```
┌─────────────┐
│  Homepage   │
│             │
│  ☰ Menu     │───┐
│             │   │
└─────────────┘   │
                  │ Tap Menu
                  ▼
         ┌────────────────┐
         │  Menu Sheet    │
         │                │
         │ ► Digital Forms│───┐
         │   Other Options│   │
         └────────────────┘   │ Select
                              ▼
                     ┌─────────────────┐
                     │  Screen 1       │
                     │  Digital Forms  │
                     │                 │
                     │  [Dropdown ▼]   │───┐
                     │  [Continue]     │   │ Select
                     └─────────────────┘   ▼
                                ┌─────────────────┐
                                │  Screen 2       │
                                │  Selection      │
                                │  Confirmed      │
                                │                 │
                                │  [Continue]     │───┐
                                └─────────────────┘   │ Tap
                                                      ▼
                                             ┌─────────────────┐
                                             │  Screen 3       │
                                             │  Change Name    │
                                             │                 │
                                             │  First Name     │
                                             │  Last Name      │
                                             │  ─── OR ───     │
                                             │  ID Type [▼]    │
                                             │  ID Number      │
                                             │                 │
                                             │  [Continue]     │───┐
                                             └─────────────────┘   │ Submit
                                                                   ▼
                                                          ┌─────────────────┐
                                                          │  Screen 4       │
                                                          │  Acknowledge    │
                                                          │                 │
                                                          │  ✓ Thank You    │
                                                          │                 │
                                                          │  [Close]        │───┐
                                                          └─────────────────┘   │ Close
                                                                                ▼
                                                                       Homepage / Done
```

---

**Created**: 2025-02-25  
**Status**: Ready for implementation  
**Entry Point**: Homepage Menu Button → Digital Forms
