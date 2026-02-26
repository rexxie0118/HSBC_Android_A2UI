package com.a2ui.renderer.form.validation

import com.a2ui.renderer.form.evaluation.ExpressionEvaluator
import com.a2ui.renderer.form.state.FormState
import com.a2ui.renderer.form.state.ValidationError

/**
 * NEW: Centralized validation engine integrated with Form Engine for handling all form validation.
 * Processes both field-level and cross-field validations through centralized approach.
 */
class ValidationEngine(
    private val expressionEvaluator: ExpressionEvaluator,
    private val formEngineStateProvider: suspend () -> FormState
) {
    
    /**
     * NEW: Validate a component field using its configuration and current form state.
     */
    suspend fun validateField(
        componentConfig: ComponentConfig, 
        currentValue: Any?
    ): List<ValidationError> {
        val elementId = componentConfig.id
        val validationRules = componentConfig.validation
        
        val errors = mutableListOf<ValidationError>()
        
        if (validationRules == null) {
            // NEW: No validation rules, field is valid by default
            return emptyList()
        }
        
        // NEW: Check required validation first 
        if (validationRules.required != null) {
            if (currentValue == null || (currentValue is String && currentValue.isBlank())) {
                errors.add(ValidationError.RequiredError(
                    elementId = elementId,
                    message = validationRules.required.message.literalString ?: "Field is required"
                ))
            }
        }
        
        // NEW: Process validation rules if they exist
        validationRules.rules?.forEach { rule ->
            when (rule) {
                is ValidationRule.Pattern -> {
                    val pattern = rule.pattern.toRegex()
                    val stringValue = currentValue?.toString() ?: ""
                    if (!pattern.matches(stringValue)) {
                        errors.add(ValidationError.PatternError(
                            elementId = elementId,
                            message = rule.message.literalString ?: "Value does not match required pattern",
                            pattern = rule.pattern
                        ))
                    }
                }
                is ValidationRule.MinLength -> {
                    val stringValue = currentValue?.toString() ?: ""
                    if (stringValue.length < rule.value) {
                        errors.add(ValidationError.LengthError(
                            elementId = elementId,
                            message = rule.message.literalString ?: "Minimum length of ${rule.value} characters required",
                            minLength = rule.value
                        ))
                    }
                }
                is ValidationRule.MaxLength -> {
                    val stringValue = currentValue?.toString() ?: ""
                    if (stringValue.length > rule.value) {
                        errors.add(ValidationError.LengthError(
                            elementId = elementId,
                            message = rule.message.literalString ?: "Maximum length of ${rule.value} characters exceeded",
                            maxLength = rule.value
                        ))
                    }
                }
                is ValidationRule.MinValue -> {
                    val numericValue = currentValue?.toString()?.toDoubleOrNull()
                    if (numericValue != null && numericValue < rule.value) {
                        errors.add(ValidationError.RangeError(
                            elementId = elementId,
                            message = rule.message.literalString ?: "Minimum value of ${rule.value} required",
                            minValue = rule.value
                        ))
                    }
                }
                is ValidationRule.MaxValue -> {
                    val numericValue = currentValue?.toString()?.toDoubleOrNull()
                    if (numericValue != null && numericValue > rule.value) {
                        errors.add(ValidationError.RangeError(
                            elementId = elementId,
                            message = rule.message.literalString ?: "Maximum value of ${rule.value} exceeded",
                            maxValue = rule.value
                        ))
                    }
                }
                // NEW: More specific validation rules would be here
            }
        }
        
        // NEW: Handle custom validation through native function
        validationRules.customValidation?.let { customRule ->
            val customResult = evaluateCustomValidation(customRule, elementId)
            if (customResult is ValidationResult.Invalid) {
                errors.add(ValidationError.CustomValidationError(
                    elementId = elementId,
                    message = customResult.message,
                    function = customRule.nativeFunction
                ))
            }
        }
        
        // NEW: Cross-field validation based on dependencies and relationships
        validateCrossFieldDependencies(componentConfig, errors)
        
        return errors
    }
    
    /**
     * NEW: Evaluate cross-field validation based on dependency relationships.
     */
    private suspend fun validateCrossFieldDependencies(
        componentConfig: ComponentConfig,
        validationResult: MutableList<ValidationError>
    ) {
        val formState = formEngineStateProvider()
        val elementId = componentConfig.id
        
        // NEW: Check if this element has cross-field dependencies specified in its validation config
        componentConfig.validation?.dependencies?.forEach { (dependencyType, dependencyExpression) ->
            val dependencyValid = when (dependencyType) {
                "visibility" -> validateVisibilityDependency(dependencyExpression, formState)
                "enablement" -> validateEnablementDependency(dependencyExpression, formState)
                "required" -> validateRequiredDependency(dependencyExpression, formState) 
                "value" -> validateValueDependency(dependencyExpression, formState)
                "options" -> validateOptionsDependency(dependencyExpression, formState)
                else -> true // Unknown dependency type is treated as valid (no error from it)
            }
            
            if (!dependencyValid) {
                validationResult.add(ValidationError.CrossFieldError(
                    elementId = elementId,
                    message = "Cross-field validation failed for dependency type: $dependencyType",
                    dependencyType = dependencyType
                ))
            }
        }
    }
    
    private suspend fun validateVisibilityDependency(expression: String, formState: FormState): Boolean {
        return expressionEvaluator.evaluate(
            expression = expression,
            namespace = EvaluationNamespace.VISIBILITY,
            contextElementId = "" // Doesn't matter for this specific evaluation
        ) as? Boolean ?: true // Default to valid if evaluation fails
    }
    
    private suspend fun validateEnablementDependency(expression: String, formState: FormState): Boolean {
        return expressionEvaluator.evaluate(
            expression = expression,
            namespace = EvaluationNamespace.ENABLEMENT, 
            contextElementId = ""
        ) as? Boolean ?: true
    }

    private suspend fun validateRequiredDependency(expression: String, formState: FormState): Boolean {
        return expressionEvaluator.evaluate(
            expression = expression,
            namespace = EvaluationNamespace.VALIDATION,
            contextElementId = ""
        ) as? Boolean ?: true
    }

    private suspend fun validateValueDependency(expression: String, formState: FormState): Boolean {
        return expressionEvaluator.evaluate(
            expression = expression,
            namespace = EvaluationNamespace.BINDING,
            contextElementId = ""
        ) != null // Treat as valid if non-null result
    }

    private suspend fun validateOptionsDependency(expression: String, formState: FormState): Boolean {
        return expressionEvaluator.evaluate(
            expression = expression,
            namespace = EvaluationNamespace.CHOICE_EVALUATION,
            contextElementId = ""
        ) != null
    }
    
    /**
     * NEW: Evaluate custom validation function.
     */
    private suspend fun evaluateCustomValidation(
        customRule: CustomValidation,
        elementId: String
    ): ValidationResult {
        val formState = formEngineStateProvider()
        
        // NEW: Prepare parameters by resolving paths from form state
        val resolvedParams = mutableListOf<Any?>()
        customRule.parameters.forEach { param ->
            if (param.startsWith("$.")) {
                // NEW: It's a path reference - resolve from form state
                val resolvedFromState = resolvePathFromState(param.substring(2), formState)
                resolvedParams.add(resolvedFromState) 
            } else {
                // NEW: It's a literal value
                resolvedParams.add(param)
            }
        }
        
        return try {
            // NEW: Call native function bridge with resolved parameters
            callNativeFunction(customRule.nativeFunction, resolvedParams)
        } catch (e: Exception) {
            // NEW: Handle evaluation exceptions gracefully
            ValidationResult.Invalid(
                message = "Custom validation error: ${e.message}",
                error = e
            )
        }
    }
    
    /**
     * NEW: Resolve path from form state (simplified path resolution).
     */
    private suspend fun resolvePathFromState(path: String, formState: FormState): Any? {
        // NEW: Implement basic path resolution - support simple paths like "user.name" or "items.0.quantity"
        val parts = path.split('.')
        
        var current: Any? = formState.values
        for (part in parts) {
            if (current == null) break
            val numericIndex = part.toIntOrNull()
            
            when {
                // NEW: Array/list index access (e.g., "items.0")
                numericIndex != null -> {
                    when(current) {
                        is List<*> -> {
                            if (numericIndex in current.indices) {
                                current = current[numericIndex]
                            } else {
                                current = null
                            }
                        }
                        else -> current = null
                    }
                }
                // NEW: Map/object property access (e.g., "user.name")  
                else -> {
                    when(current) {
                        is Map<*, *> -> {
                            current = current[part]
                        }
                        else -> current = null 
                    }
                }
            }
        }
        
        return current
    }
    
    /**
     * NEW: Mock for calling native validation functions through centralized bridge.
     */
    private suspend fun callNativeFunction(functionName: String, parameters: List<Any?>): ValidationResult {
        // This would be a real integration with NativeFunctionRegistry or similar
        // For this example, return a mock result based on function name for demonstration
        return when (functionName) {
            "validateEmailDomain" -> {
                val email = parameters.getOrNull(0) as? String
                val valid = email?.contains("@") == true && email.endsWith(".com")  // Simplified example
                if (valid) ValidationResult.Valid else ValidationResult.Invalid("Invalid email domain", null)
            }
            "validateSsnPattern" -> {
                val ssn = parameters.getOrNull(0) as? String
                val valid = ssn?.matches(Regex("^\\d{3}-\\d{2}-\\d{4}$")) == true // Simple SSN format
                if (valid) ValidationResult.Valid else ValidationResult.Invalid("Invalid SSN format", null)
            }
            else -> ValidationResult.Valid  // Default to valid for unrecognized functions
        }
    }
    
    /**
     * NEW: Validate all fields with centralized approach.
     */
    suspend fun validateAll(formState: FormState): Map<String, List<ValidationError>> {
        val results = mutableMapOf<String, List<ValidationError>>()
        
        formState.values.keys.forEach { elementId ->
            val componentConfig = getComponentConfigById(elementId) ?: return@forEach
            val elementValue = formState.values[elementId]
            val errors = validateField(componentConfig, elementValue)
            results[elementId] = errors
        }
        
        return results
    }
    
    // NEW: Placeholder for retrieving component configuration
    private fun getComponentConfigById(elementId: String): ComponentConfig? {
        return null // In real implementation, would fetch from ConfigManager by ID
    }
}

/**
 * NEW: Wrapper around component validation configuration.
 */
class ValidationRuleConfig(
    val required: RequiredConfig? = null,
    val rules: List<ValidationRule>? = null,
    val customValidation: CustomValidation? = null,
    val dependencies: Map<String, String>? = null  // NEW: Cross-field dependency rules
)

/**
 * NEW: Abstract base validation rule type.
 */
sealed class ValidationRule {
    data class Pattern(
        val pattern: String,
        val message: TextValue
    ) : ValidationRule()
    
    data class MinLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()
    
    data class MaxLength(
        val value: Int,
        val message: TextValue
    ) : ValidationRule()
    
    data class MinValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()
    
    data class MaxValue(
        val value: Double,
        val message: TextValue
    ) : ValidationRule()
    
    data class Required(
        val message: TextValue
    ) : ValidationRule()
    
    data class Custom(
        val nativeFunction: String,
        val parameters: List<String>
    ) : ValidationRule()
}

/**
 * NEW: Result of a validation operation.
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String, val error: Throwable? = null) : ValidationResult()
}

/**
 * NEW: Custom validation rule configuration.
 */
data class CustomValidation(
    val nativeFunction: String,
    val parameters: List<String> 
)