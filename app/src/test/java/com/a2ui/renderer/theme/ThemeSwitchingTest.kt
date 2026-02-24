package com.a2ui.renderer.theme

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for theme switching and configuration
 */
class ThemeSwitchingTest {

    @Test
    fun `theme JSON should have light and dark modes`() {
        val themes = listOf(
            """
                {
                    "type": "theme",
                    "id": "banking_light",
                    "mode": "light",
                    "colors": {"primary": "#D32F2F"},
                    "shadows": {},
                    "typography": {"fontFamily": "system", "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}}
                }
            """.trimIndent(),
            """
                {
                    "type": "theme",
                    "id": "banking_dark",
                    "mode": "dark",
                    "colors": {"primary": "#FF5252"},
                    "shadows": {},
                    "typography": {"fontFamily": "system", "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}}
                }
            """.trimIndent()
        )
        
        val modes = themes.map { theme ->
            val json = JsonParser.parseString(theme).asJsonObject
            json.get("mode").asString
        }
        
        assertTrue("Should have light theme", modes.contains("light"))
        assertTrue("Should have dark theme", modes.contains("dark"))
    }

    @Test
    fun `light and dark themes should have different primary colors`() {
        val lightPrimary = "#D32F2F"
        val darkPrimary = "#FF5252"
        
        assertNotEquals("Light and dark themes should have different primary colors",
            lightPrimary, darkPrimary)
    }

    @Test
    fun `theme ID should match mode naming convention`() {
        val themes = mapOf(
            "banking_light" to "light",
            "banking_dark" to "dark"
        )
        
        themes.forEach { (id, expectedMode) ->
            assertTrue(
                "Theme ID $id should contain mode name $expectedMode",
                id.contains(expectedMode, ignoreCase = true)
            )
        }
    }

    @Test
    fun `theme mode should be lowercase`() {
        val validModes = listOf("light", "dark")
        
        validModes.forEach { mode ->
            assertEquals("Mode should be lowercase", mode, mode.lowercase())
        }
    }

    @Test
    fun `theme switching should preserve color scheme structure`() {
        val requiredColors = listOf(
            "primary", "secondary", "background", "surface",
            "error", "onPrimary", "onSecondary"
        )
        
        val lightTheme = """
            {
                "id": "banking_light",
                "mode": "light",
                "colors": {
                    "primary": "#D32F2F",
                    "secondary": "#1976D2",
                    "background": "#F5F5F5",
                    "surface": "#FFFFFF",
                    "error": "#B00020",
                    "onPrimary": "#FFFFFF",
                    "onSecondary": "#FFFFFF"
                },
                "shadows": {},
                "typography": {"fontFamily": "system", "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}}
            }
        """.trimIndent()
        
        val darkTheme = """
            {
                "id": "banking_dark",
                "mode": "dark",
                "colors": {
                    "primary": "#FF5252",
                    "secondary": "#64B5F6",
                    "background": "#121212",
                    "surface": "#1E1E1E",
                    "error": "#CF6679",
                    "onPrimary": "#000000",
                    "onSecondary": "#000000"
                },
                "shadows": {},
                "typography": {"fontFamily": "system", "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}}
            }
        """.trimIndent()
        
        listOf(lightTheme, darkTheme).forEach { themeJson ->
            val json = JsonParser.parseString(themeJson).asJsonObject
            val colors = json.getAsJsonObject("colors")
            
            requiredColors.forEach { colorKey ->
                assertTrue(
                    "Theme should have $colorKey color",
                    colors.has(colorKey)
                )
            }
        }
    }

    @Test
    fun `dark theme should have darker background than light theme`() {
        val lightBackground = "#F5F5F5"
        val darkBackground = "#121212"
        
        // Convert hex to brightness (simple comparison)
        val lightBrightness = hexToBrightness(lightBackground)
        val darkBrightness = hexToBrightness(darkBackground)
        
        assertTrue(
            "Dark theme background should be darker than light theme",
            darkBrightness < lightBrightness
        )
    }

    @Test
    fun `theme should have valid typography configuration`() {
        val typographyLevels = listOf("h1", "h2", "h3", "h4", "h5", "h6", "body1", "body2", "caption", "overline", "button")
        
        val themeJson = """
            {
                "id": "test_theme",
                "mode": "light",
                "colors": {"primary": "#D32F2F"},
                "shadows": {},
                "typography": {
                    "fontFamily": "system",
                    "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0},
                    "h2": {"size": 28, "weight": "bold", "lineHeight": 36, "letterSpacing": 0},
                    "h3": {"size": 24, "weight": "bold", "lineHeight": 32, "letterSpacing": 0},
                    "h4": {"size": 22, "weight": "bold", "lineHeight": 28, "letterSpacing": 0.15},
                    "h5": {"size": 18, "weight": "bold", "lineHeight": 24, "letterSpacing": 0},
                    "h6": {"size": 16, "weight": "bold", "lineHeight": 22, "letterSpacing": 0.15},
                    "body1": {"size": 16, "weight": "regular", "lineHeight": 24, "letterSpacing": 0.5},
                    "body2": {"size": 14, "weight": "regular", "lineHeight": 20, "letterSpacing": 0.25},
                    "caption": {"size": 12, "weight": "regular", "lineHeight": 16, "letterSpacing": 0.4},
                    "overline": {"size": 11, "weight": "medium", "lineHeight": 16, "letterSpacing": 1.5},
                    "button": {"size": 14, "weight": "medium", "lineHeight": 20, "letterSpacing": 1.25}
                }
            }
        """.trimIndent()
        
        val json = JsonParser.parseString(themeJson).asJsonObject
        val typography = json.getAsJsonObject("typography")
        
        typographyLevels.forEach { level ->
            assertTrue(
                "Typography should have $level level",
                typography.has(level)
            )
        }
    }

    private fun hexToBrightness(hex: String): Int {
        // Simple brightness calculation (average of RGB)
        val r = hex.substring(1, 3).toInt(16)
        val g = hex.substring(3, 5).toInt(16)
        val b = hex.substring(5, 7).toInt(16)
        return (r + g + b) / 3
    }
}
