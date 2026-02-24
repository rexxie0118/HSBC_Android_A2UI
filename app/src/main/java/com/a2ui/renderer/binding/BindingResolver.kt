package com.a2ui.renderer.binding

import com.a2ui.renderer.config.ComponentProperties
import com.a2ui.renderer.config.TextValue

/**
 * Resolves data bindings in component properties
 * Supports path syntax: $.user.name, $.products.0.price
 */
object BindingResolver {
    
    private const val PATH_PREFIX = "$."
    
    /**
     * Resolve a binding path to actual value
     * @param path Binding path (e.g., "$.user.name")
     * @param dataModel Data model to resolve against
     * @return Resolved value or null if not found
     */
    fun resolve(path: String, dataModel: DataModelStore): Any? {
        if (!path.startsWith(PATH_PREFIX)) {
            return path
        }
        
        val cleanPath = path.removePrefix(PATH_PREFIX)
        return dataModel.getAtPath(cleanPath)
    }
    
    /**
     * Resolve text value with binding support
     * Handles both literal strings and bound values
     */
    fun resolveText(
        textValue: TextValue?,
        dataModel: DataModelStore
    ): String {
        if (textValue == null) return ""
        
        val literal = textValue.literalString
        if (!literal.startsWith(PATH_PREFIX)) {
            return literal
        }
        
        val value = resolve(literal, dataModel)
        return value?.toString() ?: ""
    }
    
    /**
     * Resolve color value with binding support
     */
    fun resolveColor(
        colorRef: String?,
        dataModel: DataModelStore
    ): String? {
        if (colorRef == null) return null
        if (!colorRef.startsWith(PATH_PREFIX)) {
            return colorRef
        }
        
        val value = resolve(colorRef, dataModel)
        return value?.toString()
    }
    
    /**
     * Resolve all properties in a component using data model
     */
    fun resolveProperties(
        properties: ComponentProperties?,
        dataModel: DataModelStore
    ): ComponentProperties? {
        if (properties == null) return null
        
        return properties.copy(
            text = properties.text?.let { 
                TextValue(resolveText(it, dataModel))
            },
            color = resolveColor(properties.color, dataModel),
            backgroundColor = resolveColor(properties.backgroundColor, dataModel),
            tintColor = resolveColor(properties.tintColor, dataModel)
        )
    }
    
    /**
     * Check if a string contains binding expressions
     */
    fun hasBinding(value: String?): Boolean {
        if (value == null) return false
        return value.startsWith(PATH_PREFIX)
    }
    
    /**
     * Resolve multiple paths at once
     */
    fun resolveAll(
        paths: List<String>,
        dataModel: DataModelStore
    ): Map<String, Any?> {
        return paths.associateWith { resolve(it, dataModel) }
    }
    
    /**
     * Update data model with literal value from binding
     * Used when component has both path and literal (two-way binding)
     */
    fun updateWithLiteral(
        path: String,
        literal: Any,
        dataModel: DataModelStore
    ) {
        if (!path.startsWith(PATH_PREFIX)) {
            return
        }
        
        val cleanPath = path.removePrefix(PATH_PREFIX)
        dataModel.updateAtPath(cleanPath, literal)
    }
}
