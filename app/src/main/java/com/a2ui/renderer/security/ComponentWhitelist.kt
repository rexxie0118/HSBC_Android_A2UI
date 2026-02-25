package com.a2ui.renderer.security

import com.a2ui.renderer.config.ComponentConfig

/**
 * Component whitelist for security
 * Only explicitly allowed components can be rendered
 */
object ComponentWhitelist {
    
    private val ALLOWED_COMPONENTS = setOf(
        // Content
        "Text", "Image", "Icon",
        // Layout
        "Row", "Column", "List", "Card", "Tabs",
        // Interactive
        "Button", "CheckBox", "TextField",
        // Utility
        "Divider", "Spacer", "Box"
    )
    
    /**
     * Check if component type is allowed
     */
    fun isAllowed(componentType: String): Boolean {
        return componentType in ALLOWED_COMPONENTS
    }
    
    /**
     * Validate list of components
     */
    fun validate(components: List<ComponentConfig>): SecurityResult {
        val invalid = components.filter { !isAllowed(it.type) }
        return if (invalid.isEmpty()) {
            SecurityResult.Success
        } else {
            SecurityResult.Failure(
                "Unauthorized components: ${invalid.map { it.type }}"
            )
        }
    }
    
    /**
     * Validate single component
     */
    fun validate(component: ComponentConfig): SecurityResult {
        return if (isAllowed(component.type)) {
            SecurityResult.Success
        } else {
            SecurityResult.Failure(
                "Unauthorized component type: ${component.type}"
            )
        }
    }
}

/**
 * Security validation result
 */
sealed class SecurityResult {
    object Success : SecurityResult()
    data class Failure(val message: String) : SecurityResult()
    
    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure
}
