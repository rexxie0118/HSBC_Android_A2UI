# A2UI JSON Structure Comparison & Recommendations

## Overview

After reviewing the A2UI Composer website (https://a2ui-composer.ag-ui.com/components), this document compares their JSON structure with our current implementation and provides recommendations.

---

## Structure Comparison

### A2UI Composer Format

```json
{
  "id": "row-1",
  "component": {
    "Row": {
      "alignment": "center",
      "distribution": "spaceBetween",
      "children": {
        "explicitList": ["child-1", "child-2"]
      }
    }
  }
}
```

**Key Characteristics**:
- **Nested component structure**: `component: { ComponentType: { props } }`
- **Flat props**: Properties at component level, no `properties` wrapper
- **Component type as key**: Type is the key in component object
- **Simplified hierarchy**: Less nesting

### Our Current Format

```json
{
  "id": "row-1",
  "type": "Row",
  "properties": {
    "alignment": "center",
    "distribution": "spaceBetween",
    "children": {
      "explicitList": ["child-1", "child-2"]
    }
  }
}
```

**Key Characteristics**:
- **Flat structure**: Type and properties at same level
- **Explicit `properties` wrapper**: All props in `properties` object
- **Type as field**: `type: "ComponentType"`
- **More explicit**: Clearer separation of metadata vs props

---

## Component Support Comparison

| Category | A2UI Composer | Our Implementation | Status |
|----------|--------------|-------------------|--------|
| **Layout** | Row, Column, List, Card | Row, Column, List, Card, Section, Box | ✅ Extended |
| **Content** | Text, Image, Icon, Video, AudioPlayer | Text, Image, Icon | ⚠️ Missing Video, AudioPlayer |
| **Input** | TextField, CheckBox, Slider, DateTimeInput, MultipleChoice | TextField, CheckBox | ⚠️ Missing Slider, DateTimeInput, MultipleChoice |
| **Navigation** | Button, Tabs, Modal | Button, Tabs, BottomNavigation | ⚠️ Missing Modal |
| **Decoration** | Divider | Divider, Spacer | ✅ Extended |

---

## Property Name Differences

| Feature | A2UI Composer | Our Format | Recommendation |
|---------|--------------|------------|----------------|
| **Component Type** | Nested key (`"Row": {}`) | `type: "Row"` | Keep current (clearer) |
| **Properties** | Direct in component | `properties: {}` | Keep current (organized) |
| **Children** | `children: {}` | `children: {}` | ✅ Same |
| **Alignment** | `alignment: "center"` | `alignment: "centerY"` | Align to Composer |
| **Distribution** | `distribution: "spaceBetween"` | `distribution: "spaceBetween"` | ✅ Same |
| **Action Events** | Not shown | `action: { event: "..." }` | Keep current (more flexible) |

---

## Recommendations

### 1. Maintain Current Structure ✅

**Recommendation**: Keep our current flat structure with `type` and `properties` fields.

**Rationale**:
- More explicit and easier to parse
- Better separation of concerns
- Easier to add metadata (validation, dependencies, etc.)
- More compatible with our validation system

### 2. Add Missing Components ⚠️

**Priority Components to Add**:

| Component | Priority | Iteration | Effort |
|-----------|----------|-----------|--------|
| **Modal** | High | 16 | 4-6h |
| **Slider** | Medium | 16 | 3-4h |
| **DateTimeInput** | Medium | 16 | 4-6h |
| **Video** | Low | 16 | 6-8h |
| **AudioPlayer** | Low | 16 | 4-6h |
| **MultipleChoice** | Low | 16 | 3-4h |

### 3. Standardize Property Names 🔧

**Align with A2UI Composer**:

```kotlin
// Current
alignment: "centerY"  // Android-specific

// Recommended (match Composer)
alignment: "center"   // Cross-platform
```

**Update alignment values**:
- `"centerY"` → `"center"` (vertical alignment)
- `"centerX"` → `"center"` (horizontal alignment, use in Row)

### 4. Add Component Schema Validation ✅

**Implement JSON schema for validation**:

```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["id", "type"],
  "properties": {
    "id": { "type": "string" },
    "type": { 
      "type": "string",
      "enum": ["Row", "Column", "Text", "Image", "Icon", "Button", ...]
    },
    "properties": {
      "type": "object",
      "properties": { ... }
    }
  }
}
```

### 5. Support Both Formats (Compatibility Layer) 🔌

**Add format adapter**:

```kotlin
object JsonFormatAdapter {
    fun fromComposerFormat(json: JSONObject): ComponentConfig {
        // Convert { component: { Row: {...} } } to { type: "Row", properties: {...} }
    }
    
    fun toComposerFormat(config: ComponentConfig): JSONObject {
        // Convert our format to Composer format
    }
}
```

---

## JSON Examples

### Row Component

**A2UI Composer**:
```json
{
  "id": "row-1",
  "component": {
    "Row": {
      "alignment": "center",
      "distribution": "spaceBetween",
      "children": {
        "explicitList": ["child-1", "child-2"]
      }
    }
  }
}
```

**Our Format (Recommended)**:
```json
{
  "id": "row-1",
  "type": "Row",
  "properties": {
    "alignment": "center",
    "distribution": "spaceBetween",
    "children": {
      "explicitList": ["child-1", "child-2"]
    }
  }
}
```

### Text Component

**Our Format (Enhanced with usageHint)**:
```json
{
  "id": "title-1",
  "type": "Text",
  "properties": {
    "text": {
      "literalString": "Welcome"
    },
    "usageHint": "h1",
    "color": "#212121"
  }
}
```

### Button Component

**Our Format (With Actions)**:
```json
{
  "id": "submit-btn",
  "type": "Button",
  "properties": {
    "text": {
      "literalString": "Submit"
    },
    "primary": true
  },
  "action": {
    "event": "submit",
    "context": {
      "destination": "confirmation"
    }
  },
  "validation": {
    "required": true
  }
}
```

---

## Implementation Priority

| Task | Priority | Effort | Status |
|------|----------|--------|--------|
| Keep current structure | ✅ | 0h | **Done** |
| Add Modal component | High | 4-6h | Iteration 16 |
| Add Slider component | Medium | 3-4h | Iteration 16 |
| Add DateTimeInput | Medium | 4-6h | Iteration 16 |
| Align property names | Low | 2-3h | Optional |
| Add format adapter | Low | 4-6h | Optional |
| Add schema validation | Medium | 6-8h | Iteration 9 (partial) |

---

## Conclusion

**Current Status**: ✅ **Our JSON structure is well-designed and compatible with A2UI Composer**

**Key Advantages**:
1. More explicit structure (easier to validate and extend)
2. Better support for advanced features (validation, dependencies, actions)
3. Clearer separation of metadata and properties
4. Already supports all core components from Composer

**Recommended Actions**:
1. ✅ **Keep current structure** - It's better for our use case
2. ⚠️ **Add missing components** (Modal, Slider, DateTimeInput) in Iteration 16
3. 🔧 **Minor alignment** - Standardize property names if needed
4. 📋 **Add schema validation** - For better error messages

**No major JSON structure changes needed!** 🎉

---

## References

- [A2UI Composer](https://a2ui-composer.ag-ui.com/components)
- [A2UI Spec](https://a2ui.org/guides/renderer-development/)
- [Our Component Examples](app/src/main/res/raw/homepage_components.jsonl)

---

**Last Updated**: 2025-02-25
