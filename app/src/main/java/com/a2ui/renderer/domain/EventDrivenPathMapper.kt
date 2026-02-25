package com.a2ui.renderer.domain

import kotlinx.coroutines.*
import android.util.Log

/**
 * Event-driven path mapper for multi-domain routing
 */
class EventDrivenPathMapper(private val jsonRouteConfig: String) {
    
    private val eventRoutes = mutableMapOf<String, DomainDataProcessor<*, *>>()
    private val observers = mutableListOf<DomainObserver<*>>()
    private val pathObservers = mutableMapOf<String, MutableList<DomainObserver<*>>>()
    private val domainObservers = mutableMapOf<String, MutableList<DomainObserver<*>>>()
    
    init {
        loadRouteConfiguration()
    }
    
    /**
     * Load route configuration from JSON
     */
    private fun loadRouteConfiguration() {
        try {
            val config = org.json.JSONObject(jsonRouteConfig)
            // Load routes from config
            Log.d("EventDrivenPathMapper", "Route configuration loaded")
        } catch (e: Exception) {
            Log.e("EventDrivenPathMapper", "Failed to load route configuration", e)
        }
    }
    
    /**
     * Dispatch data to appropriate processor
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun dispatchData(domain: String, toPath: String, data: Any?, triggeringEvent: String? = null) {
        val processor = matchProcessorToPath(domain, toPath)
        if (processor != null && data != null) {
            try {
                val result = (processor as DomainDataProcessor<Any, Any>).process(data, domain, toPath)
                if (result != null) {
                    handleDispatchSuccess(domain, toPath, result, triggeringEvent)
                }
            } catch (e: Exception) {
                handleDispatchError(domain, toPath, e, triggeringEvent)
            }
        } else {
            Log.i("EventDrivenPathMapper", "No handler found for domain: $domain path: $toPath")
        }
    }
    
    /**
     * Match processor to path
     */
    private fun matchProcessorToPath(domain: String, dataPath: String): DomainDataProcessor<*, *>? {
        val domainSpecificPath = "/$domain$dataPath"
        return eventRoutes.entries.find { (path, _) ->
            pathMatcher(domainSpecificPath, path)
        }?.value
    }
    
    /**
     * Simple path matcher
     */
    private fun pathMatcher(actual: String, pattern: String): Boolean {
        if (pattern == actual) return true
        if (pattern.endsWith("/**")) {
            return actual.startsWith(pattern.removeSuffix("/**"))
        }
        if (pattern.contains("*")) {
            val regex = pattern.replace("*", ".*").toRegex()
            return regex.matches(actual)
        }
        return false
    }
    
    /**
     * Register observer
     */
    fun <T> registerObserver(observer: DomainObserver<T>) {
        observers.add(observer)
        
        observer.observingDomains.forEach { domain ->
            domainObservers.getOrPut(domain) { mutableListOf() }.add(observer as DomainObserver<*>)
        }
        
        observer.observingPaths.forEach { path ->
            pathObservers.getOrPut(path) { mutableListOf() }.add(observer as DomainObserver<*>)
        }
    }
    
    /**
     * Notify observers of data change
     */
    fun <T> notifyPathData(domain: String, path: String, change: DataChange<T>) {
        val directObservers = (pathObservers[path] ?: emptyList()) +
                            (domainObservers[domain] ?: emptyList())
        val allRelevantObservers = directObservers.distinct()
        
        if (allRelevantObservers.isEmpty()) {
            Log.v("EventDrivenPathMapper", "No interested observers for domain: $domain path: $path")
            return
        }
        
        CoroutineScope(Dispatchers.IO).launch {
            allRelevantObservers.forEach { observer ->
                try {
                    @Suppress("UNCHECKED_CAST")
                    (observer as DomainObserver<T>).onDataChanged(change)
                } catch (e: Exception) {
                    Log.e("EventDrivenPathMapper", "Observer notification failed", e)
                    observer.onError(e, domain, path)
                }
            }
        }
    }
    
    /**
     * Handle successful dispatch
     */
    private fun handleDispatchSuccess(domain: String, path: String, result: Any?, triggeringEvent: String?) {
        Log.d("EventDrivenPathMapper", "Dispatch successful: $domain/$path")
        notifyPathData(domain, path, DataChange.DataInitialized(path, result, domain = domain))
    }
    
    /**
     * Handle dispatch error
     */
    private fun handleDispatchError(domain: String, path: String, error: Throwable?, triggeringEvent: String?) {
        Log.e("EventDrivenPathMapper", "Dispatch failed: $domain/$path", error)
    }
    
    /**
     * Check if fallback processors exist
     */
    private fun hasFallbackProcessors(): Boolean {
        return eventRoutes.containsKey("/**")
    }
    
    /**
     * Fallback to generic processor
     */
    private fun fallbackToGenericProcessor(domain: String, path: String, data: Any?) {
        Log.d("EventDrivenPathMapper", "Using fallback processor for: $domain/$path")
    }
}

/**
 * Domain data processor interface
 */
interface DomainDataProcessor<I, O> {
    suspend fun process(input: I, domain: String, path: String): O?
    fun canHandle(domain: String, jsonPath: String, data: I): Boolean = true
}
