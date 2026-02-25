package com.a2ui.renderer.binding

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Stores runtime data for a surface/page
 * Supports nested JSON-like data structures
 */
class DataModelStore {
    
    private val _data = MutableStateFlow<Map<String, Any>>(emptyMap())
    val data: StateFlow<Map<String, Any>> = _data.asStateFlow()
    
    /**
     * Get entire data model
     */
    fun getData(): Map<String, Any> = _data.value
    
    /**
     * Set entire data model
     */
    fun setData(newData: Map<String, Any>) {
        _data.value = deepToMutable(newData)
    }
    
    /**
     * Convert nested maps to mutable maps recursively
     */
    @Suppress("UNCHECKED_CAST")
    private fun deepToMutable(data: Map<String, Any>): MutableMap<String, Any> {
        val result = mutableMapOf<String, Any>()
        for ((key, value) in data) {
            result[key] = when (value) {
                is Map<*, *> -> deepToMutable(value as Map<String, Any>)
                is List<*> -> value.toMutableList()
                else -> value
            }
        }
        return result
    }
    
    /**
     * Update data at specified path
     * Path format: "user.name" or "products.0.price"
     */
    fun updateAtPath(path: String, value: Any?) {
        val keys = path.split(".")
        val currentData = _data.value.toMutableMap()
        
        if (keys.isEmpty()) {
            return
        }
        
        if (value == null) {
            // Delete at path
            deleteAtPath(currentData, keys, 0)
        } else {
            // Set at path
            setAtPath(currentData, keys, 0, value)
        }
        
        _data.value = currentData
    }
    
    /**
     * Get value at specified path
     * Returns null if path doesn't exist
     */
    fun getAtPath(path: String): Any? {
        if (path.isEmpty()) return _data.value
        
        val keys = path.split(".")
        return getAtPath(_data.value, keys, 0)
    }
    
    /**
     * Merge new data into existing model
     */
    fun mergeData(newData: Map<String, Any>) {
        val merged = _data.value.toMutableMap()
        mergeInto(merged, newData)
        _data.value = merged
    }
    
    /**
     * Clear all data
     */
    fun clear() {
        _data.value = emptyMap()
    }
    
    // Recursive helper to set value at path
    private fun setAtPath(
        current: MutableMap<String, Any>,
        keys: List<String>,
        index: Int,
        value: Any
    ) {
        if (index == keys.size - 1) {
            // Last key - set value
            current[keys[index]] = value
            return
        }
        
        val key = keys[index]
        val nextValue = current[key]
        
        if (nextValue is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            setAtPath(nextValue as MutableMap<String, Any>, keys, index + 1, value)
        } else if (nextValue is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            val mutableNext = nextValue.toMutableMap() as MutableMap<String, Any>
            current[key] = mutableNext
            setAtPath(mutableNext, keys, index + 1, value)
        } else {
            // Create new map
            val newMap = mutableMapOf<String, Any>()
            current[key] = newMap
            setAtPath(newMap, keys, index + 1, value)
        }
    }
    
    // Recursive helper to get value at path
    private fun getAtPath(
        current: Map<String, Any>,
        keys: List<String>,
        index: Int
    ): Any? {
        if (index >= keys.size) {
            return current
        }
        
        val key = keys[index]
        val value = current[key]
        
        if (index == keys.size - 1) {
            return value
        }
        
        return when (value) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                getAtPath(value as Map<String, Any>, keys, index + 1)
            }
            is List<*> -> {
                // Next key should be array index (e.g., "items.0.name" - after "items" comes "0")
                val nextKey = keys[index + 1]
                try {
                    val arrayIndex = nextKey.toInt()
                    val listItem = value.getOrNull(arrayIndex)
                    
                    // If this is the last key, return the item
                    if (index + 1 == keys.size - 1) {
                        listItem
                    } else if (listItem is Map<*, *>) {
                        // More keys to traverse
                        @Suppress("UNCHECKED_CAST")
                        getAtPath(listItem as Map<String, Any>, keys, index + 2)
                    } else {
                        listItem
                    }
                } catch (e: NumberFormatException) {
                    null
                }
            }
            else -> null
        }
    }
    
    // Recursive helper to delete at path
    private fun deleteAtPath(
        current: MutableMap<String, Any>,
        keys: List<String>,
        index: Int
    ) {
        if (index == keys.size - 1) {
            current.remove(keys[index])
            return
        }
        
        val key = keys[index]
        val nextValue = current[key]
        
        if (nextValue is MutableMap<*, *>) {
            @Suppress("UNCHECKED_CAST")
            deleteAtPath(nextValue as MutableMap<String, Any>, keys, index + 1)
        }
    }
    
    // Recursive merge
    private fun mergeInto(target: MutableMap<String, Any>, source: Map<String, Any>) {
        for ((key, value) in source) {
            if (key in target) {
                val existing = target[key]
                if (existing is MutableMap<*, *> && value is Map<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    val existingMutable = existing as MutableMap<String, Any>
                    val sourceMap = value as Map<String, Any>
                    mergeInto(existingMutable, sourceMap)
                } else {
                    target[key] = value
                }
            } else {
                target[key] = value
            }
        }
    }
}
