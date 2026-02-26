package com.a2ui.renderer.form.evaluation

/**
 * NEW: Namespaces for different types of expression evaluation to enable optimization and caching strategies.
 * Each namespace can have different caching policies and security contexts.
 */
enum class EvaluationNamespace {
    /**
     * NEW: Validation rule expressions - highest frequency evaluation, short TTL in cache.
     * Examples: required, pattern, length validation rules.
     */
    VALIDATION,
    
    /**
     * NEW: Visibility rule expressions - moderate frequency evaluation, medium TTL.
     * Examples: show/hide rules based on other field values.
     */  
     VISIBILITY,
     
     /**
     * NEW: Enablement rule expressions - moderate frequency evaluation, medium TTL.
     * Examples: enable/disable rules based on form state.
     */
     ENABLEMENT,
     
     /**
     * NEW: Data binding path resolution - lower frequency but may be accessed frequently by renderer.
     * Examples: "$.user.name", "$.items.0.amount" path lookups.
     */
     BINDING,
     
     /**
     * NEW: Dynamic choice/value generation - typically expensive to calculate, longer TTL.
     * Examples: dynamic options for dropdowns based on other selections.
     */
     CHOICE_EVALUATION
}