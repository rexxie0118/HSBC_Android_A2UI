package com.a2ui.renderer.form.state

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * NEW: Central unified form state that serves as the single source of truth.
 * Contains all form values and their derived states (errors, dirty, validity, visibility, etc.)
 */
data class FormState(
    // NEW: All form value mappings with element ID as key 
    val values: Map<String, Any?> = emptyMap(),
    
    // NEW: Track which elements have been modified since initial state (for dirty state display)
    val dirtyFlags: Map<String, Boolean> = emptyMap(), 
    
    // NEW: Track which elements have been interacted with by user (for focused error highlighting)
    val touchedFlags: Map<String, Boolean> = emptyMap(),
    
    // NEW: Track when each element was last modified for ordering purposes
    val timestamps: Map<String, Long> = emptyMap(),
    
    // NEW: Map of element ID to the list of validation errors for that element
    val errors: Map<String, List<ValidationError>> = emptyMap(), 
    
    // NEW: Map of element ID to visibility state (derived from expressions/dependencies)  
    val visibility: Map<String, Boolean> = emptyMap(),
    
    // NEW: Map of element ID to enablement state (derived from expressions/dependencies)
    val enabled: Map<String, Boolean> = emptyMap(),
    
    // NEW: Map of element ID to list of possible choices (for dropdowns, etc.)
    val choices: Map<String, List<ChoiceOption>> = emptyMap()
) {
    
    companion object {
        fun initial(): FormState = FormState()
    }
    
    /**
     * NEW: Update form state with a new value for an element, marking as dirty.
     */
    fun withValue(elementId: String, newValue: Any?): FormState {
        return copy(
            values = values + (elementId to newValue),
            timestamps = timestamps + (elementId to System.currentTimeMillis()),
            dirtyFlags = dirtyFlags + (elementId to true)
        )
    }
    
    /**
     * NEW: Add an error to the state for an element.
     */
    fun withError(elementId: String, error: ValidationError): FormState {
        val currentErrors = errors[elementId] ?: emptyList()
        return copy(
            errors = errors + (elementId to (currentErrors + error))
        )
    }
    
    /**
     * NEW: Clear errors for a specific element.
     */
    fun clearErrorsFor(elementId: String): FormState {
        return copy(errors = errors - elementId)
    }
    
    /**
     * NEW: Mark an element as touched (user has interacted with it).
     */
    fun markTouched(elementId: String): FormState {
        return copy(touchedFlags = touchedFlags + (elementId to true))
    }
    
    /**
     * NEW: Update visibility of an element.
     */
    fun withVisibilityState(elementId: String, visible: Boolean): FormState {
        return copy(visibility = visibility + (elementId to visible))
    }
    
    /**
     * NEW: Update enablement of an element.  
     */
    fun withEnablementState(elementId: String, enabled: Boolean): FormState {
        return copy(enabled = enabled + (elementId to enabled))
    }
    
    /**
     * NEW: Update choice options for an element.
     */
    fun withChoiceOptions(elementId: String, options: List<ChoiceOption>): FormState {
        return copy(choices = choices + (elementId to options))
    }
    
    /**
     * NEW: Get whether a field has any validation errors.
     */
    fun hasErrors(elementId: String): Boolean {
        return errors[elementId]?.isNotEmpty() ?: false
    }
    
    /**
     * NEW: Get whether the whole form state has any validation errors.
     */
    fun hasAnyErrors(): Boolean {
        return errors.any { (_, errorList) -> errorList.isNotEmpty() }
    }
    
    /**
     * NEW: Mark an element to indicate it's been validated.
     */
    fun markValidated(elementId: String): FormState {
        // NEW: This updates a new field that may be maintained separately for validation state tracking
        return this // Placeholder for when validation state is separate field
    }
    
    /**
     * NEW: Get a copy of the state that only contains data for specified element IDs.
     */
    fun filterForElements(elementIds: Set<String>): FormState {
        return copy(
            values = values.filterKeys { it in elementIds },
            dirtyFlags = dirtyFlags.filterKeys { it in elementIds },
            touchedFlags = touchedFlags.filterKeys { it in elementIds },
            timestamps = timestamps.filterKeys { it in elementIds },
            errors = errors.filterKeys { it in elementIds },
            visibility = visibility.filterKeys { it in elementIds },
            enabled = enabled.filterKeys { it in elementIds },
            choices = choices.filterKeys { it in elementIds }
        )
    }
    
    /**
     * NEW: Merge another form state into this one, updating values where present in other state.
     */
    fun merge(other: FormState): FormState {
        return copy(
            values = values + other.values,
            dirtyFlags = dirtyFlags + other.dirtyFlags,
            touchedFlags = touchedFlags + other.touchedFlags,
            timestamps = timestamps + other.timestamps,
            errors = errors + other.errors,
            visibility = visibility + other.visibility,
            enabled = enabled + other.enabled,
            choices = choices + other.choices
        )
    }
}

/**
 * NEW: Flow provider wrapper for reactive state access with change tracking enabled.
 */
class FormStateFlowProvider {
    private val _formState = MutableStateFlow(FormState.initial())
    val formState: StateFlow<FormState> = _formState.asStateFlow()
    
    private val lock = Mutex()  // NEW: Thread safety for state updates
    
    /**
     * NEW: Get current state snapshot.
     */
    fun getState(): FormState = _formState.value
    
    /**
     * NEW: Update state with transformation function (thread-safe).
     */
    suspend fun updateState(transform: FormState.() -> FormState) {
        lock.withLock {
            _formState.value = _formState.value.transform()
        }
    }
    
    /**
     * NEW: Direct update for simple state replacement.
     */
    suspend fun setState(newState: FormState) {
        lock.withLock {
            _formState.value = newState
        }
    }
    
    /**
     * NEW: Update state and get result in a thread-safe manner.
     */
    suspend fun <T> updateAndGet(transform: FormState.() -> Pair<FormState, T>): T {
        lock.withLock {
            val (newState, result) = _formState.value.transform()
            _formState.value = newState
            return result
        }
    }
    
    /**
     * NEW: Get value by element ID (safe access).
     */
    fun getValue(elementId: String): Any? = _formState.value.values[elementId]
    
    /**
     * NEW: Get error list by element ID (safe access).
     */
    fun getError(elementId: String): List<ValidationError> = _formState.value.errors[elementId] ?: emptyList()
    
    /**
     * NEW: Get whether element is visible (safe access).
     */
    fun getVisibility(elementId: String): Boolean = _formState.value.visibility[elementId] ?: true
    
    /**
     * NEW: Get whether element is enabled (safe access).
     */
    fun getEnablement(elementId: String): Boolean = _formState.value.enabled[elementId] != false
}