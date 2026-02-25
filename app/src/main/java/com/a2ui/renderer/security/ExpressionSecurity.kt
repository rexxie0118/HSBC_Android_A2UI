package com.a2ui.renderer.security

/**
 * Expression security validator
 * Blocks dangerous patterns in expressions
 */
object ExpressionSecurity {
    
    private val BLOCKED_PATTERNS = listOf(
        Regex(".*\\beval\\b.*"),           // eval()
        Regex(".*\\bexec\\b.*"),           // exec()
        Regex(".*\\bnew\\b.*"),            // new Object()
        Regex(".*\\bfunction\\b.*"),       // function() {}
        Regex(".*\\bclass\\b.*"),          // class MyClass {}
        Regex(".*\\bimport\\b.*"),         // import java.*
        Regex(".*\\brequire\\b.*"),        // require('module')
        Regex(".*=>.*"),                   // Arrow functions
        Regex(".*->.*"),                   // Lambda syntax
        Regex(".*\\bconstructor\\b.*"),    // constructor access
        Regex(".*\\bprototype\\b.*"),      // prototype access
        Regex(".*\\b__proto__\\b.*")       // __proto__ access
    )
    
    private val ALLOWED_OPERATORS = setOf(
        "==", "!=", "<", ">", "<=", ">=",
        "&&", "||", "!",
        "+", "-", "*", "/"
    )
    
    private val ALLOWED_FUNCTIONS = setOf(
        "length", "toLowerCase", "toUpperCase", "trim",
        "min", "max", "abs", "round",
        "contains", "indexOf", "size",
        "exists", "isEmpty"
    )
    
    /**
     * Check if expression is safe
     */
    fun isSafe(expression: String): Boolean {
        // Check for blocked patterns
        BLOCKED_PATTERNS.forEach { pattern ->
            if (pattern.matches(expression)) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Validate expression and return result
     */
    fun validate(expression: String): SecurityResult {
        if (!isSafe(expression)) {
            return SecurityResult.Failure("Unsafe expression: $expression")
        }
        return SecurityResult.Success
    }
    
    /**
     * Sanitize expression by removing dangerous patterns
     */
    fun sanitize(expression: String): String {
        var sanitized = expression
        BLOCKED_PATTERNS.forEach { pattern ->
            sanitized = pattern.replace(sanitized, "")
        }
        return sanitized
    }
}
