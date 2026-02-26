package com.a2ui.renderer.form.evaluation

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * NEW: Centralized cache management system within Form Engine for ALL evaluation results.
 * Provides namespaced caching with different policies per evaluation type (validation, binding, visibility, etc.).
 */
class EvaluationCache {
    // Protect shared state access
    private val mutex = Mutex()

    // Central cache with different TTLs per namespace (validation, visibility, binding, etc.)
    private val caches = mutableMapOf<EvaluationNamespace, MutableMap<EvaluationCacheKey, CachedEvaluationResult>>()

    /**
     * NEW: Get result from cache for specific namespace and cache key.
     * Implements cache hit with automatic removal of expired entries.
     */
    suspend fun get(namespace: EvaluationNamespace, key: EvaluationCacheKey): Any? = mutex.withLock {
        val namespaceCache = caches.getOrPut(namespace) { mutableMapOf() }
        val cachedResult = namespaceCache[key] ?: return null

        // Check if result is expired  
        if (cachedResult.isExpired()) {
            namespaceCache.remove(key)  // Clean up expired entry
            return null  
        }

        return cachedResult.result
    }

    /**
     * NEW: Put result to cache with namespace-specific TTL based on evaluation type.
     */
    suspend fun put(namespace: EvaluationNamespace, key: EvaluationCacheKey, result: Any?) = mutex.withLock {
        val ttl = getTtlForNamespace(namespace)
        val namespaceCache = caches.getOrPut(namespace) { mutableMapOf() }
        
        namespaceCache[key] = CachedEvaluationResult(
            result = result,
            evaluatedAt = System.currentTimeMillis(),
            ttlMs = ttl
        )
    }

    /**
     * NEW: Get TTL appropriate for namespace type - validation cache expires faster than binding cache.
     */
    private fun getTtlForNamespace(namespace: EvaluationNamespace): Long {
        return when (namespace) {
            EvaluationNamespace.VALIDATION -> 1000L     // 1 second for validation (might change frequently)
            EvaluationNamespace.VISIBILITY -> 5000L      // 5 seconds for visibility (stable once calculated)  
            EvaluationNamespace.ENABLEMENT -> 5000L      // 5 seconds for enablement (relatively stable)
            EvaluationNamespace.BINDING -> 30_000L       // 30 seconds for bindings (usually stable)
            EvaluationNamespace.CHOICE_EVALUATION -> 60_000L // 1 minute for dynamic choices (expensive to recompute)
        }
    }

    /**
     * NEW: Clear entries in specific namespace (used for invalidated dependencies).
     */
    suspend fun clear(namespace: EvaluationNamespace) = mutex.withLock {
        caches[namespace]?.clear()
    }

    /**
     * NEW: Evict entries based on dependency changes.
     */
    suspend fun evictForDependency(elementId: String, namespace: EvaluationNamespace) = mutex.withLock {
        caches[namespace]?.entries?.removeIf { entry ->
            entry.key.elementId == elementId  // Remove all entries related to this element
        }
    }

    /**
     * NEW: Cleanup expired entries across all namespaces - should be called periodically.
     */
    suspend fun cleanupExpired() = mutex.withLock {
        val now = System.currentTimeMillis()
        
        caches.forEach { (namespace, cache) ->
            cache.entries.removeIf { (_, result) ->
                result.isExpiredAt(now)
            }
        }
    }
}

/**
 * NEW: Cache key uniquely identifies evaluation in namespace context.
 */
data class EvaluationCacheKey(
    val expression: String,
    val elementId: String,
    val evaluationContext: String? = null  // Additional context that might affect result (e.g., $index in list)
)

/**
 * NEW: Cached result with expiration tracking.
 */
data class CachedEvaluationResult(
    val result: Any?,
    val evaluatedAt: Long, 
    val ttlMs: Long
) {
    fun isExpired(): Boolean = isExpiredAt(System.currentTimeMillis())
    
    fun isExpiredAt(currentTime: Long): Boolean = (currentTime - evaluatedAt) > ttlMs
}