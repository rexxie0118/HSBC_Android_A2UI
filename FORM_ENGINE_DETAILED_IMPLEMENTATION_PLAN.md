# A2UI Form Engine Layer - Detailed Implementation Plan

## Overview
This document outlines the detailed technical implementation plan for the Form Engine Layer - a new centralized component responsible for managing form state, validation, dependendencies, and coordination in the A2UI Renderer system.

## Architecture Overview

The Form Engine Layer will sit between the existing configuration loading layer (ConfigManager) and the UI rendering layer (ComponentRenderer), serving as:

1. A single source of truth for all form-related state
2. An evaluation engine with deterministic dependency order
3. A centralized caching system  
4. A unified error and dirty state manager
5. An action dispatcher and navigation coordinator

## Technical Components

### 1. FormState and State Management
```kotlin
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
}
```

### 2. Dependency Tracking System
```kotlin
package com.a2ui.renderer.form.dependency

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap

class DependencyGraph {
    // elementId -> set of other element IDs it depends ON
    private var dependentOnGraph: ImmutableMap<String, Set<String>> = persistentMapOf()
    
    // elementId -> set of other elements that depend on it
    private var affectsGraph: ImmutableMap<String, Set<String>> = persistentMapOf()
    
    fun trackDependency(dependentElement: String, parentElement: String) {
        // Update dependent graph (this element has as dependency on parent)
        val currentDependencies = dependentOnGraph[dependentElement] ?: emptySet()
        dependentOnGraph = dependentOnGraph.put(dependentElement, currentDependencies + parentElement)
        
        // Update affects graph (parent affects dependent element)
        val currentlyAffected = affectsGraph[parentElement] ?: emptySet()
        affectsGraph = affectsGraph.put(parentElement, currentlyAffected + dependentElement)
    }
    
    fun getDependedOnElements(elementId: String): Set<String> {
        return dependentOnGraph[elementId] ?: emptySet()
    }
    
    fun getAffectingElements(elementId: String): Set<String> {
        return affectsGraph[elementId] ?: emptySet()
    }
    
    // Get all elements that should re-evaluate when given element changes
    fun getAffectedElementsTransitively(elementId: String): Set<String> {
        val result = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        val visited = mutableSetOf<String>()
        
        // Start with immediate effects
        queue.addAll(getAffectingElements(elementId))
        visited.addAll(queue)
        
        // Traverse transitively
        while (queue.isNotEmpty()) {
            val currentElement = queue.removeFirst()
            result.add(currentElement)
            
            // Add more affected elements and queue them for further processing
            val directlyAffected = getAffectingElements(currentElement)
            val newElements = directlyAffected.filterNot(visited::contains)
            
            queue.addAll(newElements)
            visited.addAll(newElements)
        }
        
        return result
    }
}
```

### 3. Namespaced Evaluation Engine
```kotlin
package com.a2ui.renderer.form.evaluation

import kotlinx.coroutines.*
import java.util.concurrent.*

data class EvaluationCacheKey(
    val expression: String,
    val contextElement: String,
    val namespace: EvaluationNamespace
)

data class CachedEvaluationResult(
    val result: Any?,
    val evaluatedAt: Long,
    val ttlMs: Long
)

class EvaluationCache {
    private val cache = ConcurrentHashMap<EvaluationCacheKey, CachedEvaluationResult>()
    
    fun get(key: EvaluationCacheKey): Any? {
        val cached = cache[key] ?: return null
        
        // Check TTL expiration
        if ((System.currentTimeMillis() - cached.evaluatedAt) > cached.ttlMs) {
            cache.remove(key)
            return null  // Expired
        }
        
        return cached.result
    }
    
    fun put(key: EvaluationCacheKey, result: Any?, ttlMs: Long) {
        cache[key] = CachedEvaluationResult(
            result = result,
            evaluatedAt = System.currentTimeMillis(),
            ttlMs = ttlMs
        )
    }
    
    fun evict(key: EvaluationCacheKey) {
        cache.remove(key)
    }
    
    fun clear(namespace: EvaluationNamespace? = null) {
        if (namespace == null) {
            cache.clear()
        } else {
            cache.keys.removeAll { it.namespace == namespace }
        }
    }
}

// Enum describing what type of evaluation is happening
enum class EvaluationNamespace {
    VALIDATION,       // Validation rule expressions
    VISIBILITY,       // Visibility expressions (show/hide)
    ENABLEMENT,       // Enable/disable expressions
    VALUE_BOUNDINGS,  // Data binding value resolutions
    CHOICE_EVALUATION // Options choice calculations
}

class ExpressionEvaluator(
    private val cache: EvaluationCache,
    private val stateProvider: () -> FormState
) {
    // Main evaluation function with namespaced caching
    suspend fun evaluateExpression(
        expression: String,
        namespace: EvaluationNamespace,
        contextElementId: String
    ): Any? {
        val cacheKey = EvaluationCacheKey(expression, contextElementId, namespace)
        
        // Try cache first
        cache.get(cacheKey)?.let { cachedResult ->
            return cachedResult
        }
        
        // Evaluate expression using form engine context
        val result = runCatching {
            when (namespace) {
                EvaluationNamespace.VALIDATION -> evaluateValidationExpression(expression, contextElementId)
                EvaluationNamespace.VISIBILITY -> evaluateVisibilityExpression(expression, contextElementId)
                EvaluationNamespace.ENABLEMENT -> evaluateEnablementExpression(expression, contextElementId)
                EvaluationNamespace.VALUE_BOUNDINGS -> evaluateValueExpression(expression, contextElementId)
                EvaluationNamespace.CHOICE_EVALUATION -> evaluateChoiceExpression(expression, contextElementId)
            }
        }.getOrNull()
        
        // Determine TTL based on namespace
        val ttl = when (namespace) {
            EvaluationNamespace.VALIDATION -> 500 // ms
            EvaluationNamespace.VISIBILITY -> 100  // ms
            EvaluationNamespace.ENABLEMENT -> 100 // ms
            EvaluationNamespace.VALUE_BOUNDINGS -> 500 // ms
            EvaluationNamespace.CHOICE_EVALUATION -> 1000 // ms
        }
        
        // Cache result and return
        cache.put(cacheKey, result, ttl)
        return result
    }
    
    private fun evaluateValidationExpression(expression: String, contextElementId: String): Boolean {
        // Implement actual validation evaluation
        // For example: resolve expression against current form state
        val currentState = stateProvider()
        return resolveValidation(expression, currentState, contextElementId)
    }
    
    private fun evaluateVisibilityExpression(expression: String, contextElementId: String): Boolean {
        val currentState = stateProvider()
        return resolveVisibility(expression, currentState, contextElementId)
    }
    
    private fun evaluateEnablementExpression(expression: String, contextElementId: String): Boolean {
        val currentState = stateProvider()
        return resolveEnablement(expression, currentState, contextElementId)
    }
    
    private fun evaluateValueExpression(expression: String, contextElementId: String): Any? {
        val currentState = stateProvider()
        return resolveValue(expression, currentState, contextElementId)
    }
    
    private fun evaluateChoiceExpression(expression: String, contextElementId: String): Any? {
        val currentState = stateProvider()
        return resolveChoices(expression, currentState, contextElementId)
    }
    
    // These methods would implement the actual expression resolution
    private fun resolveValidation(expression: String, state: FormState, elementId: String): Boolean {
        // Placeholder implementation - would depend on the expression type and syntax
        return true // Simplified for initial implementation
    }
    
    private fun resolveVisibility(expression: String, state: FormState, elementId: String): Boolean {
        // Placeholder implementation
        return true   
    }
    
    private fun resolveEnablement(expression: String, state: FormState, elementId: String): Boolean {
        // Placeholder implementation
        return true  
    }
    
    private fun resolveValue(expression: String, state: FormState, elementId: String): Any? {
        // Placeholder - would resolve the $path.value notation from the state
        return state.values[elementId] ?: "missing_in_state_$elementId"
    }
    
    private fun resolveChoices(expression: String, state: FormState, elementId: String): Any? {
        // Placeholder implementation
        return null
    }
}
```

### 4. Form Engine Core Orchestrator
```kotlin
package com.a2ui.renderer.form.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class FormEngine {
    // State management
    private val stateProvider = FormStateFlowProvider()
    val formState: StateFlow<FormState> = stateProvider.formState
    
    // Dependency tracking
    private val dependencyGraph = DependencyGraph()
    
    // Evaluation system
    private val evaluationCache = EvaluationCache()
    private val expressionEvaluator = ExpressionEvaluator(
        evaluationCache,
        { formState.value }
    )
    
    // Public state access
    fun getCurrentValue(elementId: String): Any? = formState.value.values[elementId]
    fun getErrors(elementId: String): List<ValidationError> = formState.value.errors[elementId] ?: emptyList()
    fun hasErrors(): Boolean = formState.value.errors.isNotEmpty()
    fun isDirty(elementId: String): Boolean = formState.value.dirtyFlags[elementId] == true
    fun isTouched(elementId: String): Boolean = formState.value.touchedFlags[elementId] == true
    fun isVisible(elementId: String): Boolean = formState.value.visibility[elementId] ?: true
    fun isEnabled(elementId: String): Boolean = formState.value.enabled[elementId] != false
    
    // Main update function to modify form state with dependency tracking
    suspend fun updateValue(elementId: String, newValue: Any?, source: ChangeSource = ChangeSource.USER_INPUT) {
        // Update the main value
        stateProvider.updateState { withValue(elementId, newValue) }
        
        // Track that this element has been touched
        if (source == ChangeSource.USER_INPUT) {
            val currentState = formState.value
            stateProvider.updateState { 
                copy(touchedFlags = touchedFlags + (elementId to true)) 
            }
        }
        
        // Process dependent elements based on dependency graph
        val affectedElements = dependencyGraph.getAffectedElementsTransitively(elementId)
        processAffectedElements(elementId, affectedElements)
    }
    
    // Process elements affected by a value change (validation, visibility, etc.)
    private suspend fun processAffectedElements(triggeringElement: String, affectedElements: Set<String>) {
        withContext(Dispatchers.Default) {
            // Evaluate validation, visibility, enablement for affected elements
            affectedElements.forEach { elementId ->
                // Re-run evaluation for this element
                reevaluateElementState(elementId)
            }
        }
    }
    
    // Re-evaluate all state aspects for a given element
    private suspend fun reevaluateElementState(elementId: String) {
        // Re-check validations that might use this field
        rerunValidationsForElement(elementId)
        
        // Re-evaluate visibility based on current form state
        reevaluateVisibilityForElement(elementId)
        
        // Re-evaluate enablement based on current form state
        reevaluateEnablementForElement(elementId)
    }
    
    private suspend fun rerunValidationsForElement(elementId: String) {
        // Clear previous validation errors for this element
        stateProvider.updateState {
            withError(elementId, ValidationError.ValidationRuleError(
                elementId = elementId,
                ruleType = "placeholder_rule",
                message = "placeholder_message"
            )).also { 
                // TODO: Actually validate based on the element's declared validation rules
            }
        }
    }
    
    private suspend fun reevaluateVisibilityForElement(elementId: String) {
        // TODO: Evaluate visibility expression and update state
        stateProvider.updateState {
            copy(visibility = visibility + (elementId to true))
        }
    }
    
    private suspend fun reevaluateEnablementForElement(elementId: String) {
        // TODO: Evaluate enable expression and update state
        stateProvider.updateState {
            copy(enabled = enabled + (elementId to true))
        }
    }
    
    // Track dependencies for an element
    fun trackDependency(elementDepending: String, elementDependedOn: String) {
        dependencyGraph.trackDependency(elementDepending, elementDependedOn)
    }
    
    // Public method to validate a specific field or form
    suspend fun validateField(elementId: String): List<ValidationError> {
        // This would re-evaluate validation rules for a specific field
        val currentState = formState.value
        val errors = mutableListOf<ValidationError>()
        
        // TODO: Retrieve validation rules for this element from configuration
        // TODO: Run validation and collect errors
        
        // Update errors in state
        stateProvider.updateState {
            if (errors.isEmpty()) {
                // Clear errors for this field
                copy(errors = errors - elementId)
            } else {
                // Set new errors
                copy(errors = errors + (elementId to errors))
            }
        }
        
        return errors
    }
    
    suspend fun validateAll(): Map<String, List<ValidationError>> {
        // Validate all fields that have validation rules
        val allElementsWithErrors = mutableMapOf<String, List<ValidationError>>()
        
        // TODO: Validate each field with validation rules
        
        return allElementsWithErrors
    }
    
    // Navigation/Action methods
    suspend fun navigateToView(nextViewRule: NavigationRule): Boolean {
        // Evaluate navigation condition and decide whether to allow transition
        // This would use form state to determine navigation
        return true  // Placeholder implementation
    }
    
    // Initialize form data from a data snapshot
    suspend fun initializeWithData(initialData: Map<String, Any>) {
        stateProvider.updateState { currentState ->
            currentState.copy(
                values = currentState.values + initialData,
                dirtyFlags = initialState.dirtyFlags,
                timestamps = initialData.mapValues { System.currentTimeMillis() }
            )
        }
    }
    
    // Reset form state
    suspend fun resetAll() {
        stateProvider.updateState { FormState.initial() }
    }
}

/**
 * Represents a rule governing navigation decisions based on form state
 */
sealed class NavigationRule {
    // Direct navigation to a target view
    data class Direct(val targetViewId: String) : NavigationRule()
    
    // Conditional navigation based on form state
    data class Conditional(
        val conditionExpression: String,  // e.g. "user.age >= 18 AND account.status == 'active'"
        val trueTarget: String?,
        val falseTarget: String?
    ) : NavigationRule()
    
    // Navigation based on selected choices/options
    data class BasedOnSelection(
        val dependentElementId: String,
        val selectionMap: Map<String, String> // choice value -> destination view
    ) : NavigationRule()
}

enum class ChangeSource {
    USER_INPUT,    // Direct user interaction
    PROGRAMMATIC,  // Internal programmatic change
    CONFIG_UPDATE, // Changes due to config reloaded
    FORM_INIT      // Initial form state values
}
```

### 5. Data Adapter for Legacy DataModelStore Integration
```kotlin
package com.a2ui.renderer.form.adapter

import com.a2ui.renderer.binding.DataModelStore
import com.a2ui.renderer.form.engine.FormEngine
import kotlinx.coroutines.*

/**
 * Adapter to synchronize between DataModelStore (legacy) and Form Engine (new)
 *
 * Responsibilities:
 * 1. Listen to DataModelStore changes and propagate them to Form Engine
 * 2. Update DataModelStore when Form Engine state reflects user input decisions
 * 3. Maintain coherence between both sources
 */
class DataModelAdapter(
    private val formEngine: FormEngine,
    private val dataModelStore: DataModelStore
) {
    private var isSynchronizing = false  // Prevent cycles
    
    suspend fun startListening() {
        // Set up observers for changes to DataModelStore to propagate to FormEngine
        dataModelStore.data.collect { newData ->
            if (!isSynchronizing) {
                isSynchronizing = true
                try {
                    // Push updates from DataModelStore to FormEngine
                    handleDataModelChanges(newData)
                } finally {
                    isSynchronizing = false
                }
            }
        }
        
        // Set up observers for changes to FormEngine to push to DataModelStore if needed
        formEngine.formState.collect { newFormState ->
            if (!isSynchronizing) {
                isSynchronizing = true
                try {
                    // Push specific updates that should go back to DataModelStore
                    handleFormEngineChanges(newFormState)
                } finally {
                    isSynchronizing = false
                }
            }
        }
    }
    
    private suspend fun handleDataModelChanges(dataModelSnapshot: Map<String, Any>) {
        // Translate DataModelStore changes to FormEngine updates
        for ((key, value) in dataModelSnapshot) {
            // Determine if this update should go to form engine
            if (shouldPushToFormEngine(key, value)) {
                formEngine.updateValue(key, value, ChangeSource.CONFIG_UPDATE)
            }
        }
    }
    
    private suspend fun handleFormEngineChanges(formState: FormState) {
        // Translate FormEngine changes to DataModelStore when appropriate
        for ((elementId, value) in formState.values) {
            if (shouldPropagateToDataModel(elementId, value)) {
                // Push data model change - this would be conditional on business logic
                // dataModelStore.updateAtPath(elementId, value)  // Not implemented until needed
            }
        }
    }
    
    private fun shouldPushToFormEngine(key: String, value: Any): Boolean {
        // Determine if this DataModel change should be reflected in FormEngine
        return true // Initially, all changes are synced
    }
    
    private fun shouldPropagateToDataModel(elementId: String, value: Any?): Boolean {
        // Determine when changes from FormEngine should update DataModelStore
        return false // Initially, FormEngine controls but only pushes specific types
    }
}
```

### 6. Component Renderer Integration
```kotlin
package com.a2ui.renderer.form.integrations

import androidx.compose.runtime.*
import com.a2ui.renderer.components.*
import com.a2ui.renderer.form.engine.FormEngine

@Composable
fun FormAwareComponentRenderer(
    componentId: String,
    formEngine: FormEngine,
    onFormAction: (FormAction) -> Unit = {}
) {
    // Access form state through the Form Engine
    val currentFormState by formEngine.formState.collectAsState()
    val value by remember { derivedStateOf { currentFormState.values[componentId] } }
    val errors by remember { derivedStateOf { currentFormState.errors[componentId] ?: emptyList() } }
    val isVisible by remember { derivedStateOf { currentFormState.visibility[componentId] ?: true } }
    val isEnabled by remember { derivedStateOf { currentFormState.enabled[componentId] != false } }
    val isTouched by remember { derivedStateOf { currentFormState.touchedFlags[componentId] == true } }
    val isDirty by remember { derivedStateOf { currentFormState.dirtyFlags[componentId] == true } }
    
    if (!isVisible) {
        // Don't render invisible components
        return
    }
    
    FormAwareComponentImpl(
        componentId = componentId,
        value = value,
        errors = errors,
        isEnabled = isEnabled,
        isTouched = isTouched,
        isDirty = isDirty,
        onValueChanged = { newValue ->
            onFormAction(FormAction.UpdateValue(componentId, newValue))
        },
        onAction = { action ->
            onFormAction(action)
        }
    )
}

@Composable
private fun FormAwareComponentImpl(
    componentId: String,
    value: Any?,
    errors: List<ValidationError>,
    isEnabled: Boolean,
    isTouched: Boolean,
    isDirty: Boolean,
    onValueChanged: (Any?) -> Unit,
    onAction: (FormAction) -> Unit
) {
    // Render the appropriate component based on its type and state
    // This replaces old ComponentRenderer.renderComponent function
    when (componentId) {  // This would normally match on component.type instead of ID
        else -> {
            // Default component that can handle all the various form states
            DefaultFormAwareComponent(
                currentValue = value,
                errors = errors,
                isEnabled = isEnabled,
                isTouched = isTouched,
                isDirty = isDirty,
                onValueChanged = onValueChanged,
                onAction = onAction
            )
        }
    }
}

@Composable
private fun DefaultFormAwareComponent(
    currentValue: Any?,
    errors: List<ValidationError>,
    isEnabled: Boolean,
    isTouched: Boolean,
    isDirty: Boolean,
    onValueChanged: (Any?) -> Unit,
    onAction: (FormAction) -> Unit
) {
    if (currentState.value is String || currentState.value == null) {
        // Handle as a form input
        val textFieldValue = currentValue?.toString() ?: ""
        
        FormAwareTextField(
            value = textFieldValue,
            onValueChange = { newValue ->
                onValueChanged(newValue)
            },
            isEnabled = isEnabled,
            errorMessages = errors,
            isTouched = isTouched,
            isDirty = isDirty
        )
    } else {
        // Handle other types of components
        Text("Value: $currentValue (type unknown in demo implementation)")
    }
}

@Composable
private fun FormAwareTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isEnabled: Boolean,
    errorMessages: List<ValidationError>,
    isTouched: Boolean,
    isDirty: Boolean
) {
    Column {
        TextField(
            value = value,
            onValueChange = onValueChange,
            enabled = isEnabled,
            isError = errorMessages.isNotEmpty(),
            modifier = Modifier.alpha(if (isEnabled) 1f else 0.5f)
        )
        
        if (errors.isNotEmpty()) {
            errorMessages.forEach { error ->
                Text(
                    text = error.message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        
        if (isDirty && isTouched) {
            // Show visual indicator that field has been modified
            Text(
                text = "• Changed",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

// Define action types the form engine should handle
sealed class FormAction {
    data class UpdateValue(val elementId: String, val value: Any?) : FormAction()
    data class ValidateField(val elementId: String) : FormAction()
    data class NavigateTo(val navigationRule: NavigationRule) : FormAction()
    data class DispatchAction(val elementId: String, val actionDetails: Map<String, Any>) : FormAction()
    data class ResetField(val elementId: String) : FormAction()
    object ResetAll : FormAction()
}
```

## Migration Plan

### Phase 1: Form Engine Core Implementation
Timeline: 2-3 weeks
- Implement core FormState, FormEngine, and DependencyGraph
- Create basic evaluation infrastructure  
- Implement adapter pattern for FormEngine initialization
- Create unit tests for form state and engine functionality

### Phase 2: Form-Aware Components
Timeline: 1-2 weeks  
- Modify ComponentRenderer to consume from FormEngine instead of direct DataModelStore access
- Create FormAware components (TextField, etc.) that consume engine state
- Implement error, dirty, visibility, and enablement logic

### Phase 3: Evaluation and Caching Integration 
Timeline: 1-2 weeks
- Integrate expression evaluator with dependency tracking
- Implement caching per namespace
- Migrate validation and dependency rules to use Form Engine

### Phase 4: Integration and Migration
Timeline: 2-3 weeks
- Deploy Form Engine alongside existing engine
- Implement bidirectional adapter between legacy and new system
- Migrate journeys and components to use FormEngine progressively

## Testing Considerations

1. Unit tests for each component (FormState, DependencyGraph, ExpressionEvaluator, FormEngine)
2. Integration tests to ensure compatibility with existing components
3. Performance tests to verify that centralized approach is equal or better than existing implementations
4. End-to-end tests to ensure form flows work as expected

## Performance Concerns and Optimization

1. Caching of expensive expressions in EvaluationCache with per-namespace TTL
2. Dependency-based only-recomputation rather than full form re-evaluation
3. Memory management for the centralized state store as forms grow
4. Coroutines for efficient non-blocking evaluation and updates

## Risks and Mitigation

Risk: Performance degradation due to centralized evaluation
Mitigation: Thorough performance benchmarking, incremental deployment, fallback to current architecture

Risk: Complexity of migrating existing code
Mitigation: Adapter layer for backward compatibility, phased migration

Risk: Increased memory usage with centralized state
Mitigation: Memory profiling, lazy initialization, cleanup mechanisms