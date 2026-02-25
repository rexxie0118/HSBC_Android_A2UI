package com.a2ui.renderer.security

/**
 * Script blocker - prevents dynamic script execution
 */
object ScriptBlocker {
    
    private val BLOCKED_SCRIPT_PATTERNS = listOf(
        Regex("<script.*>.*</script>", RegexOption.DOT_MATCHES_ALL),
        Regex("javascript:.*", RegexOption.IGNORE_CASE),
        Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE),  // onclick=, onerror=, etc.
        Regex("\\beval\\s*\\(", RegexOption.IGNORE_CASE),
        Regex("\\bFunction\\s*\\(", RegexOption.IGNORE_CASE),
        Regex("\\bsetTimeout\\s*\\(", RegexOption.IGNORE_CASE),
        Regex("\\bsetInterval\\s*\\(", RegexOption.IGNORE_CASE),
        Regex("\\bnew\\s+Function\\s*\\(", RegexOption.IGNORE_CASE)
    )
    
    /**
     * Check if content contains script
     */
    fun containsScript(content: String): Boolean {
        return BLOCKED_SCRIPT_PATTERNS.any { pattern ->
            pattern.containsMatchIn(content)
        }
    }
    
    /**
     * Sanitize content by removing scripts
     */
    fun sanitize(content: String): String {
        var sanitized = content
        BLOCKED_SCRIPT_PATTERNS.forEach { pattern ->
            sanitized = pattern.replace(sanitized, "")
        }
        return sanitized
    }
}

/**
 * XSS preventer
 */
object XSSPreventer {
    
    private val XSS_PATTERNS = listOf(
        Regex("<.*?script.*?>", RegexOption.IGNORE_CASE),
        Regex("javascript:", RegexOption.IGNORE_CASE),
        Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE),
        Regex("<.*?iframe.*?>", RegexOption.IGNORE_CASE),
        Regex("<.*?object.*?>", RegexOption.IGNORE_CASE),
        Regex("<.*?embed.*?>", RegexOption.IGNORE_CASE)
    )
    
    /**
     * Check if content contains XSS
     */
    fun containsXSS(content: String): Boolean {
        return XSS_PATTERNS.any { pattern ->
            pattern.containsMatchIn(content)
        }
    }
    
    /**
     * Sanitize content
     */
    fun sanitize(content: String): String {
        var sanitized = content
        XSS_PATTERNS.forEach { pattern ->
            sanitized = pattern.replace(sanitized, "")
        }
        // Escape HTML entities
        sanitized = sanitized
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;")
        return sanitized
    }
}
