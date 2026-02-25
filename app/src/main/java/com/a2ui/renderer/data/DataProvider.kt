package com.a2ui.renderer.data

import android.content.Context
import org.json.JSONObject
import org.json.JSONArray

object DataProvider {
    private val dataCache = mutableMapOf<String, Any>()
    
    fun loadData(context: Context, fileName: String): JSONObject? {
        return try {
            val cached = dataCache[fileName]
            if (cached is JSONObject) {
                return cached
            }
            
            val inputStream = context.assets.open(fileName)
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonString)
            
            dataCache[fileName] = jsonObject
            jsonObject
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getDataArray(context: Context, fileName: String, arrayKey: String): List<JSONObject> {
        val data = loadData(context, fileName) ?: return emptyList()
        
        val array = data.optJSONArray(arrayKey) ?: return emptyList()
        val result = mutableListOf<JSONObject>()
        
        for (i in 0 until array.length()) {
            result.add(array.getJSONObject(i))
        }
        
        return result
    }
    
    fun clearCache() {
        dataCache.clear()
    }
}
