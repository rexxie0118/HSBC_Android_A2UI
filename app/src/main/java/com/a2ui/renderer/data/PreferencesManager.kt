package com.a2ui.renderer.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Manages user preferences including theme selection
 * Uses SharedPreferences for persistence
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "a2ui_preferences",
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_THEME = "selected_theme"
        private const val DEFAULT_THEME = "banking_light"
    }
    
    /**
     * Get current selected theme ID
     */
    fun getSelectedTheme(): String {
        return prefs.getString(KEY_THEME, DEFAULT_THEME) ?: DEFAULT_THEME
    }
    
    /**
     * Set selected theme and persist
     */
    fun setSelectedTheme(themeId: String) {
        prefs.edit {
            putString(KEY_THEME, themeId)
        }
    }
    
    /**
     * Flow of selected theme for reactive UI updates
     */
    fun themeFlow(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_THEME) {
                trySend(getSelectedTheme())
            }
        }
        
        prefs.registerOnSharedPreferenceChangeListener(listener)
        
        // Send initial value
        send(getSelectedTheme())
        
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()
    
    /**
     * Check if dark mode is currently selected
     */
    fun isDarkMode(): Boolean {
        val theme = getSelectedTheme()
        return theme.contains("dark", ignoreCase = true)
    }
    
    /**
     * Toggle between light and dark theme
     */
    fun toggleDarkMode() {
        val currentTheme = getSelectedTheme()
        val newTheme = if (currentTheme.contains("dark", ignoreCase = true)) {
            currentTheme.replace("dark", "light", ignoreCase = true)
        } else {
            currentTheme.replace("light", "dark", ignoreCase = true)
        }
        setSelectedTheme(newTheme)
    }
    
    /**
     * Clear all preferences (for testing)
     */
    fun clear() {
        prefs.edit { clear() }
    }
}
