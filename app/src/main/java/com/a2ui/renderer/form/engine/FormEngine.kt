package com.a2ui.renderer.form.engine

import com.a2ui.renderer.form.dependency.DependencyGraph
import com.a2ui.renderer.form.evaluation.EvaluationCache
import com.a2ui.renderer.form.evaluation.ExpressionEvaluator
import com.a2ui.renderer.form.state.FormState
import com.a2ui.renderer.form.state.FormStateFlowProvider
import com.a2ui.renderer.form.state.ValidationError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

/**
 * The main orchestrator for the entire form engine functionality.
 * Acts as the single source of truth for all form-related state and evaluation.
 */
class FormEngine {
    
    // State management
    private val stateProvider = FormStateFlowProvider()
    val formState: StateFlow<FormState> = stateProvider.formState
    
    // Evaluation system
    private val evaluationCache = EvaluationCache()
    private val expressionEvaluator = ExpressionEvaluator(
        cache = evaluationCache,
        stateProvider = { stateProvider.formState.value }
    )
    
    // Public state access methods
    fun getCurrentValue(elementId: String): Any? = stateProvider.formState.value.values[elementId]
    fun getErrors(elementId: String): List<ValidationError> = stateProvider.formState.value.errors[elementId] ?: emptyList()
    fun hasErrors(): Boolean = stateProvider.formState.value.errors.isNotEmpty()
    fun isDirty(elementId: String): Boolean = stateProvider.formState.value.dirtyFlags[elementId] == true
    fun isTouched(elementId: String): Boolean = stateProvider.formState.value.touchedFlags[elementId] == true
    fun isVisible(elementId: String): Boolean = stateProvider.formState.value.visibility[elementId] ?: true
    fun isEnabled(elementId: String): Boolean = stateProvider.formState.value.enabled[elementId] != false
    
    /**
     * Main update function to modify form state with dependency tracking.
     * Processes changes and triggers re-evaluation of dependent elements.
     */
    suspend fun updateValue(
        elementId: String, 
        newValue: Any?, 
        source: ChangeSource = ChangeSource.USER_INPUT,
        elementDependencies: List<String> = emptyList() // Provide element dependencies if known
    ) {
        // Update the main value using the FormState.withValue method from FormState class
        val updatedState = stateProvider.formState.value.withValue(elementId, newValue)
        stateProvider.updateState { updatedState }
        
        // Mark this element as touched if updated via user intervention
        if (source == ChangeSource.USER_INPUT) {
            val currentState = stateProvider.getLatestState()
            val updatedTouched = currentState.touchedFlags + (elementId to true)
            val touchedUpdateState = currentState.copy(touchedFlags = updatedTouched)
            stateProvider.updateState { touchedUpdateState }
        }
        
        // Process dependent elements based on dependency graph - evaluate affected components
        withContext(Dispatchers.Default) {
            reevaluateElementState(elementId)
        }
    }
    
    private suspend fun reevaluateElementState(elementId: String) {
        // Re-validate the element
        rerunValidationsForElement(elementId)
        
        // Re-evaluate visibility based on current form state
        reevaluateVisibilityForElement(elementId)
        
        // Re-evaluate enablement based on current form state
        reevaluateEnablementForElement(elementId)
    }
    
    private suspend fun rerunValidationsForElement(elementId: String) {
        // Clear any existing errors for this element (we re-evaluate)
        var currentState = stateProvider.formState.value
        currentState = currentState.clearError(elementId)
        stateProvider.updateState { currentState }
        
        // Evaluate validation rules if configured for this element
        // This would retrieve validation rules from configuration and run them
        // For now we'll just set a placeholder to show how it would work
        evaluateValidationsForElement(elementId)
    }
    
    private suspend fun reevaluateVisibilityForElement(elementId: String) {
        // In a real implementation, this would evaluate visibility expressions
        // For now, assume everything is visible
        var currentState = stateProvider.formState.value
        val updatedVisibility = currentState.visibility + (elementId to true)
        currentState = currentState.copy(visibility = updatedVisibility)
        stateProvider.updateState { currentState }
    }
    
    private suspend fun reevaluateEnablementForElement(elementId: String) {
        // In a real implementation, this would evaluate enablement expressions
        // For now, assume everything is enabled
        var currentState = stateProvider.formState.value
        val updatedEnablement = currentState.enabled + (elementId to true)
        currentState = currentState.copy(enabled = updatedEnablement)
        stateProvider.updateState { currentState }
    }
    
    private suspend fun evaluateValidationsForElement(elementId: String) {
        // This would look up validation expressions from config for the element
        // This is a placeholder implementation
        val currentState = stateProvider.formState.value
        val elementValue = currentState.values[elementId]
        
        // Example: if field is required but empty, add error
        // The real implementation would fetch rules from configuration and evaluate them
        if (isRequired(elementId) && isValueMissing(elementValue)) {
            var updatedState = currentState
            updatedState = updatedState.withError(
                elementId,
                ValidationError.ValidationRuleError(
                    elementId = elementId,
                    ruleType = "required",
                    message = "$elementId is required"
                )
            )
            stateProvider.updateState { updatedState }
        }
    }
    
    // Placeholder methods - in real implementation these would come from configuration
    private fun isRequired(elementId: String): Boolean {
        // In a real implementation, check if element has required validation rule from config
        return false  // Default to not required
    }
    
    private fun isValueMissing(value: Any?): Boolean {
        return value == null || (value is String && value.isBlank())
    }
    
    // Validate a specific field directly
    suspend fun validateField(elementId: String): List<ValidationError> {
        evaluateValidationsForElement(elementId)
        
        val errors = getErrors(elementId)
        return errors
    }
    
    // Validate the entire form state
    suspend fun validateAll(): Map<String, List<ValidationError>> {
        val currentState = stateProvider.formState.value
        val allErrors = mutableMapOf<String, List<ValidationError>>()
        
        currentState.values.forEach { (elementId, _) ->
            val errors = validateField(elementId)
            if (errors.isNotEmpty()) {
                allErrors[elementId] = errors
            }
        }
        
        return allErrors
    }
    
    // Navigation methods would go here
    suspend fun canNavigateTo(destinationViewId: String): Boolean {
        // Check if form state allows navigation to destination
        val allErrors = validateAll()
        // Return true if state is valid for navigation (could be conditional based on configuration)
        return allErrors.isEmpty() // Basic default implementation
    }
    
    // Initialize form with initial data
    suspend fun initializeWithData(initialData: Map<String, Any>) {
        var currentState = stateProvider.formState.value
        currentState = currentState.copy(
            values = currentState.values + initialData,
            // Initialize other states appropriately
            dirtyFlags = currentState.dirtyFlags + initialData.mapValues { false },
            touchedFlags = currentState.touchedFlags + initialData.mapValues { false },
            timestamps = currentState.timestamps + initialData.mapValues { System.currentTimeMillis() }
        )
        stateProvider.updateState { currentState }
    }
    
    // Reset form to initial state
    suspend fun resetForm() {
        stateProvider.updateState { FormState.initial() }
    }
    
    // Clean-up method (periodic maintenance)  
    suspend fun cleanup() {
        // Run cleanup tasks
        withContext(Dispatchers.IO) {
            evaluationCache.cleanupExpired()
        }
    }
}

/**
 * Represents the source of a change to form state.
 */
enum class ChangeSource {
    USER_INPUT,      // Direct user interaction
    PROGRAMMATIC,    // Internal programmatic change 
    CONFIG_UPDATE,   // Changes due to config reload
    FORM_INIT        // Initial form state values
}