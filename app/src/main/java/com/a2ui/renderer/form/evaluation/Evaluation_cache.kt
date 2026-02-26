package com.a2ui.renderer.form.evaluation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap

data class EvaluationCacheKey(
    val expression: String,
    val contextElement: String,
    val namespace: EvaluationNamespace
)

data class CachedEvaluationResult(
    val result: Any?,
    val evaluatedAt: Long,
    val ttlMs: Long
) {
    fun isExpired(): Boolean = (System.currentTimeMillis() - evaluatedAt) > ttlMs
}

class EvaluationCache {
    private val cache = ConcurrentHashMap<EvaluationCacheKey, CachedEvaluationResult>()
    private val mutex = Mutex()  // For thread safety when accessing results of specific keys
    
    // TTL defaults in milliseconds per namespace
    private val defaultTtlMap = mapOf(
        EvaluationNamespace.VALIDATION to 500L,
        EvaluationNamespace.VISIBILITY to 100L,
        EvaluationNamespace.ENABLEMENT to 100L,
        EvaluationNamespace.VALUE_BOUNDINGS to 500L,
        EvaluationNamespace.CHOICE_EVALUATION to 1000L
    )
    
    suspend fun get(key: EvaluationCacheKey): Any? = mutex.withLock(key) {
        val cached = cache[key] ?: return null
        
        // Check TTL expiration and remove if expired
        if (cached.isExpired()) {
            cache.remove(key)
            return null  // Expired
        }
        
        return cached.result
    }
    
    fun put(key: EvaluationCacheKey, result: Any?, ttlMs: Long? = null) {
        val ttl = ttlMs ?: defaultTtlMap[key.namespace] ?: 1000L // Default to 1 sec
        cache[key] = CachedEvaluationResult(
            result = result,
            evaluatedAt = System.currentTimeMillis(),
            ttlMs = ttl
        )
    }
    
    suspend fun evict(key: EvaluationCacheKey) = mutex.withLock(key) {
        cache.remove(key)
    }
    
    // Clear cache for a specific namespace
    fun clear(namespace: EvaluationNamespace? = null) {
        if (namespace == null) {
            cache.clear()
        } else {
            cache.keys.removeAll { it.namespace == namespace }
        }
    }
    
    // Helper function to get the appropriate TTL for an expression and namespace
    fun getTtlForNamespace(namespace: EvaluationNamespace): Long {
        return defaultTtlMap[namespace] ?: 1000L
    }
    
    // Clean up expired entries across all caches (called periodically)
    fun cleanupExpired() {
        val now = System.currentTimeMillis()
        cache.entries.removeIf { (_, result) -> (now - result.evaluatedAt) > result.ttlMs }
    }
}