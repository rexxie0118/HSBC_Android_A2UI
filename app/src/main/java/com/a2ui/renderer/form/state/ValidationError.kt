package com.a2ui.renderer.form.state

/**
 * NEW: Standardized error representation for the new Form Engine architecture.
 * Centralizes all error types in one unified structure.
 */
sealed class ValidationError {
    abstract val elementId: String
    abstract val message: String
    abstract val timestamp: Long
    
    /**
     * NEW: Validation rule specific errors (required, pattern, length, etc.)
     */
    data class ValidationRuleError(
        override val elementId: String,
        val ruleType: String, // "required", "pattern", "minLength", "maxLength", etc.
        override val message: String,
        val ruleValue: Any? = null, // Value that triggered the validation failure
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Required field validation errors.
     */
    data class RequiredError(
        override val elementId: String,
        override val message: String,
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Pattern validation errors.
     */
    data class PatternError(
        override val elementId: String,
        override val message: String,
        val pattern: String,  // The pattern that failed validation
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Length validation errors (minimum/maximum).
     */
    data class LengthError(
        override val elementId: String,
        override val message: String,
        val minLength: Int? = null,  // If null, no min was required
        val maxLength: Int? = null,  // If null, no max was required
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Range validation errors (minimum/maximum value).
     */
    data class RangeError(
        override val elementId: String,
        override val message: String,
        val minValue: Double? = null,  // If null, no min was required
        val maxValue: Double? = null,  // If null, no max was required
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Cross-field validation errors.
     */
    data class CrossFieldError(
        override val elementId: String,
        override val message: String,
        val relatedFieldId: String,           // ID of related field in cross-field validation
        val relationType: String,            // Type of relationship (eq, gt, lt, etc.)
        val relatedValue: Any? = null,       // Value of related field
        val validationExpression: String,    // Expression that failed validation
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Custom validation function errors.
     */
    data class CustomValidationError(
        override val elementId: String,
        override val message: String,
        val customFunction: String,          // Name of the function that failed
        val parameterValues: List<Any?> = emptyList(),  // Parameter values passed to function
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Data transformation/dependency error.
     */
    data class DependencyError(
        override val elementId: String,
        override val message: String,
        val dependencyExpression: String,    // The expression that caused the dependency issue
        val dependencyType: String = "",     // Type of dependency ("visibility", "enablement", "value", etc.)
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
    
    /**
     * NEW: Generic error with error type specifier.
     */
    data class GenericError(
        override val elementId: String,
        override val message: String,
        val errorType: String,               // Specific category of error
        val details: Map<String, Any> = emptyMap(), // Additional debugging information
        override val timestamp: Long = System.currentTimeMillis()
    ) : ValidationError()
}