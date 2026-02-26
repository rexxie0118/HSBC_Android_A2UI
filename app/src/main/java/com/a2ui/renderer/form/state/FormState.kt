package com.a2ui.renderer.form.state

import kotlinx.coroutines.flow.*

// Unified state class storing form values, dirty flags, errors, and other state
data class FormState(
    // All form value mappings: element ID -> value
    val values: Map<String, Any?> = emptyMap(), 
    
    // Track which fields have been modified since initial state  
    val dirtyFlags: Map<String, Boolean> = emptyMap(),
    
    // Track which fields have been touched by user interaction
    val touchedFlags: Map<String, Boolean> = emptyMap(),
    
    // Timestamps to order when fields were last modified
    val timestamps: Map<String, Long> = emptyMap(),
    
    // Map of field to list of validation errors
    val errors: Map<String, List<ValidationError>> = emptyMap(),
    
    // Map of field to visibility state (derived from expressions)
    val visibility: Map<String, Boolean> = emptyMap(),
    
    // Map of field to enabled state (derived from expressions)
    val enabled: Map<String, Boolean> = emptyMap()
) {
    companion object {
        fun initial(): FormState = FormState()
    }
    
    fun withValue(elementId: String, value: Any?): FormState {
        return copy(
            values = values + (elementId to value),
            timestamps = timestamps + (elementId to System.currentTimeMillis()),
            dirtyFlags = dirtyFlags + (elementId to true)
        )
    }
    
    fun withError(elementId: String, error: ValidationError): FormState {
        val currentErrors = errors[elementId] ?: emptyList()
        return copy(
            errors = errors + (elementId to (currentErrors + error))
        )
    }
    
    fun clearError(elementId: String): FormState {
        return copy(errors = errors - elementId)
    }
}

// Flow wrapper for reactive state management
class FormStateFlowProvider {
    private val _formState = MutableStateFlow(FormState.initial())
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    fun updateState(transform: FormState.() -> FormState) {
        _formState.value = _formState.value.transform()
    }
    
    fun getLatestState(): FormState = _formState.value
}