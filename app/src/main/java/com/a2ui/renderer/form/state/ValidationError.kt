package com.a2ui.renderer.form.state

// Base validation error class
sealed class ValidationError {
    abstract val elementId: String
    abstract val message: String
    abstract val severity: Severity

    // Validation rule error (required, pattern, etc.)
    data class ValidationRuleError(
        override val elementId: String,
        val ruleType: String, // e.g. "required", "pattern", "minLength"
        override val message: String,
        val ruleValue: Any? = null, // The value that triggered the error
        override val severity: Severity = Severity.ERROR
    ) : ValidationError()

    // Cross-field dependency error (depends on other fields)
    data class CrossFieldError(
        override val elementId: String,
        override val message: String,
        val relatedFieldId: String,
        val validationExpression: String,
        override val severity: Severity = Severity.WARNING
    ) : ValidationError()
    
    // Custom function error (calls to native functions)
    data class CustomFunctionError(
        override val elementId: String,
        override val message: String,
        val functionName: String,
        val parameters: List<String>,
        override val severity: Severity = Severity.ERROR
    ) : ValidationError()

    // Generic error for cases that don't fit other types
    data class GenericError(
        override val elementId: String,
        override val message: String,
        val errorType: String,
        override val severity: Severity = Severity.INFO
    ) : ValidationError()
}

enum class Severity {
    INFO, WARNING, ERROR, CRITICAL
}