package com.a2ui.renderer.form.evaluation

// Enum describing what type of evaluation is happening
enum class EvaluationNamespace {
    VALIDATION,       // Validation rule expressions
    VISIBILITY,       // Visibility expressions (show/hide)
    ENABLEMENT,       // Enable/disable expressions
    VALUE_BOUNDINGS,  // Data binding value resolutions
    CHOICE_EVALUATION // Options choice calculations
}