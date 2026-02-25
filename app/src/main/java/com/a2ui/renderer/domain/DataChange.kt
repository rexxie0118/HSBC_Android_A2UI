package com.a2ui.renderer.domain

/**
 * Type-safe data mutation events
 */
sealed class DataChange<out T> {
    data class PropertyAdded<out T>(
        val path: String,
        val property: String,
        val newValue: T,
        val timestamp: Long = System.currentTimeMillis(),
        val domain: String
    ) : DataChange<T>()
    
    data class PropertyUpdated<out T>(
        val path: String,
        val property: String,
        val oldValue: Any?,
        val newValue: T,
        val timestamp: Long = System.currentTimeMillis(),
        val domain: String
    ) : DataChange<T>()
    
    data class PropertyRemoved(
        val path: String,
        val property: String,
        val removedValue: Any?,
        val timestamp: Long = System.currentTimeMillis(),
        val domain: String
    ) : DataChange<Nothing>()
    
    data class DataInitialized<out T>(
        val path: String,
        val data: T,
        val timestamp: Long = System.currentTimeMillis(),
        val domain: String
    ) : DataChange<T>()
    
    data class DataReset(
        val path: String,
        val previousData: Any?,
        val timestamp: Long = System.currentTimeMillis(),
        val domain: String
    ) : DataChange<Nothing>()
}

/**
 * Domain observer interface
 */
interface DomainObserver<T> {
    val observingDomains: List<String>
    val observingPaths: List<String>
    suspend fun onDataChanged(changes: DataChange<T>)
    fun onError(error: Throwable, domain: String, path: String)
}
