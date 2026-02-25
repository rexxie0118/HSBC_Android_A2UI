package com.a2ui.renderer.security

/**
 * Main security manager for A2UI Renderer
 * Enforces all 8 security policies
 */
object SecurityManager {
    
    /**
     * Validate component configuration
     */
    fun validate(config: Any): SecurityResult {
        // Validate based on type
        return when (config) {
            is com.a2ui.renderer.config.ComponentConfig -> validateComponent(config)
            is String -> validateContent(config)
            else -> SecurityResult.Success
        }
    }
    
    /**
     * Validate component
     */
    fun validateComponent(component: com.a2ui.renderer.config.ComponentConfig): SecurityResult {
        // 1. Component Whitelisting
        val whitelistResult = ComponentWhitelist.validate(component)
        if (whitelistResult.isFailure) {
            return whitelistResult
        }
        
        // 2. Expression Security (if component has expressions)
        component.properties?.let { props ->
            props.text?.literalString?.let { text ->
                if (text.startsWith("$.")) {
                    val exprResult = ExpressionSecurity.validate(text)
                    if (exprResult.isFailure) {
                        return exprResult
                    }
                }
            }
        }
        
        return SecurityResult.Success
    }
    
    /**
     * Validate content (text, URLs, etc.)
     */
    fun validateContent(content: String): SecurityResult {
        // Check for script injection
        if (ScriptBlocker.containsScript(content)) {
            return SecurityResult.Failure("Script injection detected")
        }
        
        // Check for XSS
        if (XSSPreventer.containsXSS(content)) {
            return SecurityResult.Failure("XSS detected")
        }
        
        // Check URL if it looks like one
        if (content.startsWith("http://") || content.startsWith("https://")) {
            val urlResult = CSPValidator.validateUrl(content)
            if (urlResult.isFailure) {
                return urlResult
            }
        }
        
        return SecurityResult.Success
    }
    
    /**
     * Log security event
     */
    fun logSecurityEvent(type: String, details: String) {
        android.util.Log.w("SecurityManager", "[$type] $details")
    }
}
