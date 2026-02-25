package com.a2ui.renderer.rules

import com.a2ui.renderer.config.TextValue

/**
 * Validation rule types for input components
 */
sealed class ValidationRule {
    /**
     * Pattern validation using regex
     */
    data class Pattern(
        val pattern: String,
        val message: TextValue
    ) : ValidationRule()

    /**
     * Minimum length validation
     */
    data class MinLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()

    /**
     * Maximum length validation
     */
    data class MaxLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()

    /**
     * Minimum value validation (for numbers)
     */
    data class MinValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()

    /**
     * Maximum value validation (for numbers)
     */
    data class MaxValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()

    /**
     * Email format validation
     */
    data class Email(
        val message: TextValue
    ) : ValidationRule()

    /**
     * Phone number validation
     */
    data class Phone(
        val message: TextValue
    ) : ValidationRule()

    /**
     * Cross-field validation using expressions
     */
    data class CrossField(
        val expression: String,
        val message: TextValue
    ) : ValidationRule()
}

/**
 * Validation configuration for a component
 */
data class ValidationConfig(
    val required: Boolean = false,
    val rules: List<ValidationRule>? = null,
    val customValidation: CustomValidationConfig? = null
)

/**
 * Custom validation that calls native function
 */
data class CustomValidationConfig(
    val nativeFunction: String,
    val parameters: List<String>
)
