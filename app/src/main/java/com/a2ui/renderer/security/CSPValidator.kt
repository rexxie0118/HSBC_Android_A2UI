package com.a2ui.renderer.security

import android.webkit.URLUtil

/**
 * Content Security Policy validator
 * Validates URLs against whitelist
 */
object CSPValidator {
    
    private val ALLOWED_SCHEMES = setOf("https", "http", "file", "content")
    private val BLOCKED_HOSTS = setOf("evil.com", "malicious.com", "hack.com")
    
    /**
     * Validate URL
     */
    fun validateUrl(url: String): SecurityResult {
        // Check if URL is valid
        if (!URLUtil.isValidUrl(url)) {
            return SecurityResult.Failure("Invalid URL: $url")
        }
        
        // Check scheme
        val scheme = try {
            url.substringBefore("://")
        } catch (e: Exception) {
            return SecurityResult.Failure("Invalid URL scheme: $url")
        }
        
        if (scheme !in ALLOWED_SCHEMES) {
            return SecurityResult.Failure("URL scheme not allowed: $scheme")
        }
        
        // Check for javascript: scheme
        if (url.startsWith("javascript:", ignoreCase = true)) {
            return SecurityResult.Failure("JavaScript URLs not allowed: $url")
        }
        
        // Check for blocked hosts
        BLOCKED_HOSTS.forEach { blocked ->
            if (url.contains(blocked, ignoreCase = true)) {
                return SecurityResult.Failure("Blocked host: $blocked")
            }
        }
        
        return SecurityResult.Success
    }
}
