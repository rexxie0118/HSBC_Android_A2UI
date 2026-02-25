package com.a2ui.renderer.rules

import android.util.Patterns
import com.a2ui.renderer.config.TextValue
import com.a2ui.renderer.binding.DataModelStore
import com.a2ui.renderer.bridge.NativeFunctionRegistry
import com.a2ui.renderer.config.ComponentConfig

/**
 * Validation engine for input components
 * Evaluates validation rules defined in JSON configuration
 */
object ValidationEngine {

    /**
     * Validation result for a field
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<ValidationError>
    )

    /**
     * Validation error details
     */
    data class ValidationError(
        val fieldId: String,
        val message: String,
        val ruleType: String
    )

    /**
     * Validate all fields in a form
     */
    fun validateForm(
        fields: List<com.a2ui.renderer.config.ComponentConfig>,
        dataModel: DataModelStore
    ): ValidationResult {
        val errors = mutableListOf<ValidationError>()

        fields.forEach { field ->
            val fieldErrors = validateField(field, dataModel)
            errors.addAll(fieldErrors)
        }

        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Validate single field
     */
    fun validateField(
        field: com.a2ui.renderer.config.ComponentConfig,
        dataModel: DataModelStore
    ): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val validation = field.validation ?: return errors

        val value = dataModel.getAtPath(field.id)

        // Required check
        if (validation.required == true && (value == null || value.toString().isEmpty())) {
            errors.add(ValidationError(
                fieldId = field.id,
                message = "This field is required",
                ruleType = "required"
            ))
            return errors // No point checking other rules
        }

        // Skip further validation if value is empty and not required
        if (value == null || value.toString().isEmpty()) {
            return errors
        }

        // Pattern validation
        validation.rules?.forEach { rule ->
            when (rule) {
                is ValidationRule.Pattern -> {
                    if (!value.toString().matches(rule.pattern.toRegex())) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "pattern"
                        ))
                    }
                }
                is ValidationRule.MinLength -> {
                    if (value.toString().length < rule.value) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "minLength"
                        ))
                    }
                }
                is ValidationRule.MaxLength -> {
                    if (value.toString().length > rule.value) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "maxLength"
                        ))
                    }
                }
                is ValidationRule.MinValue -> {
                    val numValue = value.toString().toDoubleOrNull()
                    if (numValue == null || numValue < rule.value) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "minValue"
                        ))
                    }
                }
                is ValidationRule.MaxValue -> {
                    val numValue = value.toString().toDoubleOrNull()
                    if (numValue == null || numValue > rule.value) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "maxValue"
                        ))
                    }
                }
                is ValidationRule.Email -> {
                    if (!Patterns.EMAIL_ADDRESS.matcher(value.toString()).matches()) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "email"
                        ))
                    }
                }
                is ValidationRule.Phone -> {
                    if (!Patterns.PHONE.matcher(value.toString()).matches()) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "phone"
                        ))
                    }
                }
                is ValidationRule.CrossField -> {
                    val isValid = ExpressionEvaluator.evaluateBoolean(rule.expression, dataModel)
                    if (!isValid) {
                        errors.add(ValidationError(
                            fieldId = field.id,
                            message = rule.message.literalString,
                            ruleType = "crossField"
                        ))
                    }
                }
            }
        }

        // Custom native validation
        validation.customValidation?.let { custom ->
            try {
                val parameters = custom.parameters.map { param ->
                    dataModel.getAtPath(param)
                }

                val result = NativeFunctionRegistry.execute(
                    custom.nativeFunction,
                    parameters
                )

                if (result is ValidationResultData && !result.isValid) {
                    errors.add(ValidationError(
                        fieldId = field.id,
                        message = result.message,
                        ruleType = "custom"
                    ))
                }
            } catch (e: Exception) {
                // Log error but don't fail validation
                android.util.Log.e("ValidationEngine", "Custom validation error: ${e.message}")
            }
        }

        return errors
    }

    /**
     * Validate field in real-time (debounced)
     */
    suspend fun validateFieldAsync(
        field: com.a2ui.renderer.config.ComponentConfig,
        dataModel: DataModelStore
    ): ValidationResult {
        // Small delay to avoid excessive validation during typing
        kotlinx.coroutines.delay(300)

        val errors = validateField(field, dataModel)
        return ValidationResult(errors.isEmpty(), errors)
    }

    /**
     * Check if a field is valid
     */
    fun isFieldValid(
        field: com.a2ui.renderer.config.ComponentConfig,
        dataModel: DataModelStore
    ): Boolean {
        return validateField(field, dataModel).isEmpty()
    }

    /**
     * Get validation state for UI display
     */
    data class FieldValidationState(
        val isValid: Boolean,
        val showError: Boolean,
        val errorMessage: String?,
        val isDirty: Boolean
    )

    /**
     * Track validation state for form fields
     */
    object ValidationStateTracker {
        private val dirtyFields = mutableSetOf<String>()
        private val validationStates = mutableMapOf<String, FieldValidationState>()

        fun markDirty(fieldId: String) {
            dirtyFields.add(fieldId)
        }

        fun isDirty(fieldId: String): Boolean = fieldId in dirtyFields

        fun updateState(fieldId: String, state: FieldValidationState) {
            validationStates[fieldId] = state
        }

        fun getState(fieldId: String): FieldValidationState? = validationStates[fieldId]

        fun hasErrors(): Boolean = validationStates.any { it.value.showError }

        fun getErrorFields(): List<String> = validationStates
            .filter { it.value.showError }
            .map { it.key }

        fun clear() {
            dirtyFields.clear()
            validationStates.clear()
        }
    }
}

/**
 * Data class for custom validation result
 */
data class ValidationResultData(
    val isValid: Boolean,
    val message: String
)
