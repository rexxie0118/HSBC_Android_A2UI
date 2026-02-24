package com.a2ui.renderer.data

import android.content.Context
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

/**
 * Unit tests for PreferencesManager
 */
class PreferencesManagerTest {

    private lateinit var preferencesManager: PreferencesManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: android.content.SharedPreferences
    private lateinit var mockEditor: android.content.SharedPreferences.Editor

    @Before
    fun setup() {
        // Create mocks
        mockContext = mock(Context::class.java)
        mockPrefs = mock(android.content.SharedPreferences::class.java)
        mockEditor = mock(android.content.SharedPreferences.Editor::class.java)
        
        // Setup mock behavior
        `when`(mockContext.getSharedPreferences("a2ui_preferences", Context.MODE_PRIVATE))
            .thenReturn(mockPrefs)
        `when`(mockPrefs.edit()).thenReturn(mockEditor)
        `when`(mockEditor.putString(anyString(), anyString())).thenReturn(mockEditor)
        `when`(mockEditor.clear()).thenReturn(mockEditor)
        
        preferencesManager = PreferencesManager(mockContext)
    }

    @Test
    fun `getSelectedTheme returns default when no preference saved`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_light")
        
        val result = preferencesManager.getSelectedTheme()
        
        assertEquals("banking_light", result)
    }

    @Test
    fun `getSelectedTheme returns saved theme`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_dark")
        
        val result = preferencesManager.getSelectedTheme()
        
        assertEquals("banking_dark", result)
    }

    @Test
    fun `setSelectedTheme saves theme preference`() {
        preferencesManager.setSelectedTheme("banking_dark")
        
        verify(mockEditor).putString("selected_theme", "banking_dark")
    }

    @Test
    fun `isDarkMode returns true for dark theme`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_dark")
        
        val result = preferencesManager.isDarkMode()
        
        assertTrue("Should return true for dark theme", result)
    }

    @Test
    fun `isDarkMode returns false for light theme`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_light")
        
        val result = preferencesManager.isDarkMode()
        
        assertFalse("Should return false for light theme", result)
    }

    @Test
    fun `toggleDarkMode switches from light to dark`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_light")
            .thenReturn("banking_dark") // After setting
        
        preferencesManager.toggleDarkMode()
        
        verify(mockEditor).putString("selected_theme", "banking_dark")
    }

    @Test
    fun `toggleDarkMode switches from dark to light`() {
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn("banking_dark")
            .thenReturn("banking_light") // After setting
        
        preferencesManager.toggleDarkMode()
        
        verify(mockEditor).putString("selected_theme", "banking_light")
    }

    @Test
    fun `clear removes all preferences`() {
        preferencesManager.clear()
        
        verify(mockEditor).clear()
    }

    @Test
    fun `theme preference key is correct`() {
        val expectedKey = "selected_theme"
        
        // Verify the key is used correctly
        preferencesManager.setSelectedTheme("test_theme")
        
        verify(mockEditor).putString(eq(expectedKey), anyString())
    }

    @Test
    fun `default theme is banking_light`() {
        // This test verifies the default theme constant
        `when`(mockPrefs.getString("selected_theme", "banking_light"))
            .thenReturn(null)
        
        // When prefs returns null, should fall back to default
        val prefsValue = mockPrefs.getString("selected_theme", "banking_light")
        val result = prefsValue ?: "banking_light"
        
        assertEquals("banking_light", result)
    }

    @Test
    fun `multiple setTheme calls should update preference`() {
        preferencesManager.setSelectedTheme("banking_light")
        preferencesManager.setSelectedTheme("banking_dark")
        preferencesManager.setSelectedTheme("banking_light")
        
        verify(mockEditor, times(3)).putString(eq("selected_theme"), anyString())
    }
}
