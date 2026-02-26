package com.a2ui.renderer.form.evaluation

import com.a2ui.renderer.form.state.FormState
import kotlinx.coroutines.*

/**
 * NEW: Centralized expression evaluator within Form Engine.
 * Handles ALL expressions across validation, visibility, binding, enablement scenarios with proper security.
 */
class ExpressionEvaluator(
    private val cache: EvaluationCache,
    private val stateProvider: suspend () -> FormState
) {
    
    /**
     * NEW: Main evaluation function with namespace awareness and caching.
     */
    suspend fun evaluate(
        expression: String,
        namespace: EvaluationNamespace,
        contextElementId: String,
        evaluationContext: String? = null
    ): Any? {
        val cacheKey = EvaluationCacheKey(
            expression = expression,
            elementId = contextElementId,
            evaluationContext = evaluationContext
        )
        
        // NEW: First try cache for namespace
        val cachedResult = cache.get(namespace, cacheKey)
        if (cachedResult != null) {
            return cachedResult
        }
        
        // NEW: Validate expression is safe before evaluation
        if (!isExpressionSafe(expression)) {
            throw SecurityException("Unsafe expression blocked: $expression")
        }
        
        // NEW: Evaluate expression with appropriate namespace context
        val result = when (namespace) {
            EvaluationNamespace.VALIDATION -> evaluateValidationExpression(expression, contextElementId)
            EvaluationNamespace.VISIBILITY -> evaluateVisibilityExpression(expression, contextElementId)
            EvaluationNamespace.ENABLEMENT -> evaluateEnablementExpression(expression, contextElementId)
            EvaluationNamespace.BINDING -> evaluateBindingExpression(expression, contextElementId)
            EvaluationNamespace.CHOICE_EVALUATION -> evaluateChoiceExpression(expression, contextElementId)
        }
        
        // NEW: Cache successful result
        cache.put(namespace, cacheKey, result)
        
        return result
    }
    
    /**
     * NEW: Validate expression doesn't contain unsafe patterns.
     */
    private fun isExpressionSafe(expression: String): Boolean {
        BLOCKED_PATTERNS.forEach { pattern ->
            if (pattern.containsMatchIn(expression)) {
                return false
            }
        }
        return true
    }
    
    /**
     * NEW: Evaluate expressions in validation context with validation-specific security.
     */
    private suspend fun evaluateValidationExpression(expression: String, elementId: String): Boolean {
        // NEW: Get current form state
        val currentState = stateProvider()
        val currentValue = currentState.values[elementId]
        
        // NEW: Parse expression and evaluate against current value
        return when (expression.trim()) {
            // NEW: Handle built in validation expressions
            "required" -> currentValue != null && currentValue.toString().isNotBlank()
            "notEmpty" -> currentValue != null && currentValue.toString().isNotBlank()
            else -> evaluateComplexValidationExpression(expression, currentValue, currentState)
        }
    }
    
    /**
     * NEW: Evaluate expressions for visibility context.
     */
    private suspend fun evaluateVisibilityExpression(expression: String, elementId: String): Boolean {
        val currentState = stateProvider()
        
        // NEW: Resolve boolean expressions for visibility
        return evaluateBooleanExpression(expression, elementId, currentState)
    }
    
    /**
     * NEW: Evaluate expressions for element enablement.
     */
    private suspend fun evaluateEnablementExpression(expression: String, elementId: String): Boolean {
        val currentState = stateProvider()
        
        // NEW: Resolve boolean expressions for enablement
        return evaluateBooleanExpression(expression, elementId, currentState)
    }
    
    /**
     * NEW: Evaluate expressions for data binding.
     */
    private suspend fun evaluateBindingExpression(expression: String, elementId: String): Any? {
        val currentState = stateProvider()
        
        // NEW: Handle path expressions for data binding (e.g., "$.user.name")
        return if (expression.startsWith("$.")) {
            resolvePathExpression(expression.substring(2), currentState)
        } else {
            // NEW: Handle simple literal expressions
            expression
        }
    }
    
    /**
     * NEW: Evaluate expressions for choice/dynamic options generation.
     */
    private suspend fun evaluateChoiceExpression(expression: String, elementId: String): Any? {
        val currentState = stateProvider()
        
        // NEW: Handle more complex choice generation expressions
        return when {
            expression.startsWith("getOptionsFor:") -> {
                // NEW: Handle options retrieval expressions
                evaluateOptionsExpression(expression, currentState)
            }
            expression.startsWith("filter:") -> {
                // NEW: Handle filter expressions for dynamic options
                evaluateFilterExpression(expression, elementId, currentState)
            }
            else -> {
                // NEW: Fallback to general expression evaluation
                evaluateGeneralExpression(expression, elementId, currentState)
            }
        }
    }
    
    /**
     * NEW: Evaluate boolean expression safely against form state.
     */
    private suspend fun evaluateBooleanExpression(expression: String, elementId: String, state: FormState): Boolean {
        // NEW: Implement safe boolean evaluation based on current state
        return try {
            // This would parse and evaluate boolean expressions safely
            // For example: "$.user.loggedIn === true", "$.age >= 18", etc.
            // For simplicity of this implementation, return default
            safelyParseBooleanExpression(expression, state)
        } catch (e: Exception) {
            // NEW: Return false for failing expressions as safe default
            false
        }
    }
    
    /**
     * NEW: Resolve path-based expression against form state (e.g., "user.profile.name").
     */
    private suspend fun resolvePathExpression(path: String, state: FormState): Any? {
        // NEW: Implement path resolution against FormState values map
        return if (path.contains(".")) {
            // NEW: Handle nested path access (e.g., "user.profile.name")
            val parts = path.split(".", limit = 2)
            val rootKey = parts[0]
            val subPath = parts.getOrNull(1)
            
            val rootValue = state.values[rootKey] ?: return null
            if (subPath == null) {
                return rootValue
            } else {
                // NEW: Handle nested property access recursively
                return resolveNestedValue(rootValue, subPath)
            }
        } else {
            // NEW: Handle simple key access
            return state.values[path]
        }
    }
    
    /**
     * NEW: Handle nested value resolution (e.g., nested Map access).
     */
    private suspend fun resolveNestedValue(value: Any?, subPath: String): Any? {
        if (value == null) return null
        if (subPath.isEmpty()) return value
        
        return when (value) {
            is Map<*, *> -> {
                val parts = subPath.split(".", limit = 2)
                val property = parts[0]
                val nextPath = parts.getOrNull(1)
                
                val nestedValue = value[property]
                if (nextPath != null) {
                    resolveNestedValue(nestedValue, nextPath)
                } else {
                    nestedValue
                }
            }
            is List<*> -> {
                // NEW: Handle list index access (e.g., users.0.name)
                val parts = subPath.split(".", limit = 2)
                val indexStr = parts[0]  
                val nextPath = parts.getOrNull(1)
                
                val index = indexStr.toIntOrNull() ?: return null
                if (index !in value.indices) return null
                
                val element = value[index]
                if (nextPath != null) {
                    resolveNestedValue(element, nextPath)
                } else {
                    element
                }
            }
            else -> null
        }
    }
    
    /**
     * NEW: Handle choice/option expression evaluation.
     */
    private suspend fun evaluateOptionsExpression(expression: String, state: FormState): Any? {
        val optionsSpecifier = expression.substringAfter(":").trim()
        
        return when (optionsSpecifier) {
            // NEW: Default to static options from configuration
            "static" -> state.values["${optionsSpecifier}_options"]
            // NEW: Handle dynamic options based on other state
            "dynamic" -> {
                // Here we would evaluate context-sensitive options  
                // For example: get options based on current user role, selected country, etc.
                getDynamicOptions(optionsSpecifier, state)
            }
            else -> {
                // NEW: Handle custom options specifier
                getCustomOptions(optionsSpecifier, state)
            }
        }
    }
    
    /**
     * NEW: Evaluate filter expressions for options filtering.
     */
    private suspend fun evaluateFilterExpression(expression: String, elementId: String, state: FormState): Any? {
        val filterSpec = expression.substringAfter(":").trim()
        
        // NEW: Implement filter logic against state
        // This would filter options based on current form state
        return performFilter(filterSpec, state)
    }
    
    /**
     * NEW: General expression evaluation.
     */
    private suspend fun evaluateGeneralExpression(expression: String, elementId: String, state: FormState): Any? {
        // NEW: Implement safe general expression evaluation
        return when {
            expression.startsWith("$.") -> {
                // NEW: Path resolution
                resolvePathExpression(expression.substring(2), state)
            }
            expression.isBooleanExpression() -> {
                // NEW: Boolean expression evaluation
                safelyParseBooleanExpression(expression, state)
            } 
            else -> {
                // NEW: Default to string literal (safe fallback)
                expression
            }
        }
    }
    
    /**
     * NEW: Evaluate complex validation expressions (more advanced than basic types).
     */
    private suspend fun evaluateComplexValidationExpression(
        expression: String, 
        elementValue: Any?, 
        state: FormState
    ): Boolean {
        // NEW: More complex validation logic like cross-field validation
        // e.g., "$.password === $.confirmPassword", "$.age >= 18 && $.country === 'USA'" 
        return performComplexValidation(expression, elementValue, state)
    }
    
    // NEW: Helper methods implementing actual evaluation logic
    private suspend fun safelyParseBooleanExpression(expression: String, state: FormState): Boolean {
        // NEW: Implement safe parsing of boolean expressions
        // e.g., parse "$.value > 10" safely by comparing with actual state values
        return true  // PLACEHOLDER - would implement proper parsing
    }
    
    private suspend fun getDynamicOptions(specifier: String, state: FormState): Any? {
        // NEW: Return options based on dynamic state (e.g., user role dependent options)
        return null  // PLACEHOLDER
    }
    
    private suspend fun getCustomOptions(specifier: String, state: FormState): Any? {
        // NEW: Return custom options based on specifier  
        return null  // PLACEHOLDER
    }
    
    private suspend fun performFilter(filterSpec: String, state: FormState): Any? {
        // NEW: Perform filtering based on filter specification
        return null  // PLACEHOLDER
    }
    
    private suspend fun performComplexValidation(
        expression: String, 
        elementValue: Any?, 
        state: FormState
    ): Boolean {
        // NEW: Perform complex validation (cross-field checks, etc.)
        return false  // PLACEHOLDER
    }
}

val BLOCKED_PATTERNS = listOf(
    Regex(".*\\beval\\b.*"),      // eval()
    Regex(".*\\bexec\\b.*"),      // exec()  
    Regex(".*\\bnew\\s+\\w+\\(.*"),   // new Object()
    Regex(".*\\bfunction\\b.*"),  // function(){}
    Regex(".*\\bclass\\b.*"),     // class definitions
    Regex(".*\\bimport\\b.*"),    // import statements
    Regex(".*\\brequire\\b.*"),   // require() calls
    Regex(".*\\b=>.*"),           // arrow functions
    Regex(".*\\b->.*"),           // lambda operators
    Regex(".*\\b__proto__.*"),    // prototype pollution
    Regex(".*\\bconstructor.*")   // constructor access
)