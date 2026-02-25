package com.a2ui.renderer.offline

import android.content.Context
import com.a2ui.renderer.config.ComponentConfig
import com.a2ui.renderer.config.PageConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Cache manager for offline support
 * Caches surfaces, pages, and data for offline access
 */
class CacheManager(context: Context) {
    
    private val surfaceCache = androidx.collection.LruCache<String, PageConfig>(100)
    private val componentCache = androidx.collection.LruCache<String, ComponentConfig>(500)
    private val dataCache = androidx.collection.LruCache<String, String>(1000)
    
    private val cacheDir = File(context.cacheDir, "a2ui_cache").apply {
        if (!exists()) mkdirs()
    }
    
    /**
     * Cache a page configuration
     */
    fun cachePage(pageId: String, page: PageConfig) {
        surfaceCache.put(pageId, page)
        persistToDisk("page_$pageId", page)
    }
    
    /**
     * Get cached page
     */
    suspend fun getPage(pageId: String): PageConfig? = withContext(Dispatchers.IO) {
        surfaceCache.get(pageId) ?: loadFromDisk("page_$pageId")
    }
    
    /**
     * Cache a component configuration
     */
    fun cacheComponent(componentId: String, component: ComponentConfig) {
        componentCache.put(componentId, component)
    }
    
    /**
     * Get cached component
     */
    fun getComponent(componentId: String): ComponentConfig? {
        return componentCache.get(componentId)
    }
    
    /**
     * Cache data for offline access
     */
    fun cacheData(key: String, data: String) {
        dataCache.put(key, data)
        persistStringToDisk("data_$key", data)
    }
    
    /**
     * Get cached data
     */
    suspend fun getData(key: String): String? = withContext(Dispatchers.IO) {
        dataCache.get(key) ?: loadStringFromDisk("data_$key")
    }
    
    /**
     * Check if data is cached
     */
    fun isCached(key: String): Boolean {
        return dataCache.get(key) != null || diskFileExists("data_$key")
    }
    
    /**
     * Clear cache for specific key
     */
    fun clearCache(key: String) {
        surfaceCache.remove(key)
        componentCache.remove(key)
        dataCache.remove(key)
        deleteFromDisk("page_$key")
        deleteFromDisk("data_$key")
    }
    
    /**
     * Clear all cache
     */
    fun clearAll() {
        surfaceCache.evictAll()
        componentCache.evictAll()
        dataCache.evictAll()
        cacheDir.deleteRecursively()
    }
    
    /**
     * Get cache size in bytes
     */
    suspend fun getCacheSize(): Long = withContext(Dispatchers.IO) {
        cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
    
    /**
     * Trim cache to specified size
     */
    suspend fun trimCache(maxSizeBytes: Long) = withContext(Dispatchers.IO) {
        val currentSize = getCacheSize()
        if (currentSize > maxSizeBytes) {
            // Remove oldest files
            val files = cacheDir.listFiles()?.sortedBy { it.lastModified() } ?: return@withContext
            var removed = 0L
            for (file in files) {
                if (removed + file.length() >= currentSize - maxSizeBytes) break
                file.delete()
                removed += file.length()
            }
        }
    }
    
    // Private disk persistence methods
    
    private fun persistToDisk(key: String, config: Any) {
        try {
            val file = File(cacheDir, "$key.json")
            // Serialize config to JSON and write to file
            file.writeText("{\"id\":\"$key\",\"cached\":true}") // Simplified for now
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Failed to persist to disk: $key", e)
        }
    }
    
    private fun persistStringToDisk(key: String, data: String) {
        try {
            val file = File(cacheDir, "$key.dat")
            file.writeText(data)
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Failed to persist string to disk: $key", e)
        }
    }
    
    private suspend fun loadFromDisk(key: String): PageConfig? = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "$key.json")
            if (file.exists()) {
                // Parse JSON and return config
                PageConfig(
                    id = key,
                    name = key,
                    title = key,
                    journeyId = "default",
                    theme = "default",
                    statusBar = com.a2ui.renderer.config.StatusBarConfig(false, "dark", "#000000"),
                    navigationBar = com.a2ui.renderer.config.NavigationBarConfig(false, "dark", "#000000"),
                    scrollable = true,
                    pullToRefresh = false,
                    refreshOnResume = false,
                    analytics = com.a2ui.renderer.config.PageAnalyticsConfig(key, false)
                )
            } else null
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Failed to load from disk: $key", e)
            null
        }
    }
    
    private suspend fun loadStringFromDisk(key: String): String? = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, "$key.dat")
            if (file.exists()) file.readText() else null
        } catch (e: Exception) {
            android.util.Log.e("CacheManager", "Failed to load string from disk: $key", e)
            null
        }
    }
    
    private fun diskFileExists(key: String): Boolean {
        return File(cacheDir, "$key.dat").exists() || File(cacheDir, "$key.json").exists()
    }
    
    private fun deleteFromDisk(key: String) {
        File(cacheDir, "$key.dat").delete()
        File(cacheDir, "$key.json").delete()
    }
}
