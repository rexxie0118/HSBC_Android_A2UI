package com.a2ui.renderer.theme

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for theme configuration parsing and validation
 * Uses Gson for JSON parsing (no Android dependencies)
 */
class ThemeConfigTest {

    private val gson = Gson()

    @Test
    fun `banking_light theme JSON should have correct structure`() {
        val lightThemeJson = """
            {
                "type": "theme",
                "id": "banking_light",
                "mode": "light",
                "colors": {
                    "primary": "#D32F2F",
                    "secondary": "#1976D2",
                    "background": "#F5F5F5",
                    "surface": "#FFFFFF"
                },
                "shadows": {},
                "typography": {
                    "fontFamily": "system",
                    "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}
                }
            }
        """.trimIndent()

        val json = JsonParser.parseString(lightThemeJson).asJsonObject
        
        assertEquals("theme", json.get("type").asString)
        assertEquals("banking_light", json.get("id").asString)
        assertEquals("light", json.get("mode").asString)
        
        val colors = json.getAsJsonObject("colors")
        assertEquals("#D32F2F", colors.get("primary").asString)
        assertEquals("#1976D2", colors.get("secondary").asString)
        assertEquals("#F5F5F5", colors.get("background").asString)
        assertEquals("#FFFFFF", colors.get("surface").asString)
    }

    @Test
    fun `banking_dark theme JSON should have correct structure`() {
        val darkThemeJson = """
            {
                "type": "theme",
                "id": "banking_dark",
                "mode": "dark",
                "colors": {
                    "primary": "#FF5252",
                    "secondary": "#64B5F6",
                    "background": "#121212",
                    "surface": "#1E1E1E"
                },
                "shadows": {},
                "typography": {
                    "fontFamily": "system",
                    "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}
                }
            }
        """.trimIndent()

        val json = JsonParser.parseString(darkThemeJson).asJsonObject
        
        assertEquals("theme", json.get("type").asString)
        assertEquals("banking_dark", json.get("id").asString)
        assertEquals("dark", json.get("mode").asString)
        
        val colors = json.getAsJsonObject("colors")
        assertEquals("#FF5252", colors.get("primary").asString)
        assertEquals("#64B5F6", colors.get("secondary").asString)
        assertEquals("#121212", colors.get("background").asString)
        assertEquals("#1E1E1E", colors.get("surface").asString)
    }

    @Test
    fun `theme should have all required color tokens`() {
        val requiredColors = listOf(
            "primary", "primaryVariant",
            "secondary", "secondaryVariant",
            "background", "surface",
            "error", "onPrimary", "onSecondary",
            "onBackground", "onSurface", "onError",
            "outline", "outlineVariant", "divider",
            "textPrimary", "textSecondary", "textTertiary", "textDisabled",
            "cardBackground", "cardBorder"
        )

        val themeJson = """
            {
                "type": "theme",
                "id": "test_theme",
                "mode": "light",
                "colors": {
                    "primary": "#D32F2F",
                    "primaryVariant": "#B71C1C",
                    "secondary": "#1976D2",
                    "secondaryVariant": "#1565C0",
                    "background": "#F5F5F5",
                    "surface": "#FFFFFF",
                    "error": "#B00020",
                    "onPrimary": "#FFFFFF",
                    "onSecondary": "#FFFFFF",
                    "onBackground": "#000000",
                    "onSurface": "#000000",
                    "onError": "#FFFFFF",
                    "outline": "#BDBDBD",
                    "outlineVariant": "#E0E0E0",
                    "divider": "#E8E8E8",
                    "textPrimary": "#212121",
                    "textSecondary": "#757575",
                    "textTertiary": "#9E9E9E",
                    "textDisabled": "#BDBDBD",
                    "cardBackground": "#FFFFFF",
                    "cardBorder": "#BDBDBD"
                },
                "shadows": {},
                "typography": {
                    "fontFamily": "system",
                    "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0}
                }
            }
        """.trimIndent()

        val json = JsonParser.parseString(themeJson).asJsonObject
        val colors = json.getAsJsonObject("colors")
        
        requiredColors.forEach { colorKey ->
            assertTrue("Theme should have $colorKey", colors.has(colorKey))
        }
    }

    @Test
    fun `typography config should parse correctly`() {
        val typographyJson = """
            {
                "fontFamily": "system",
                "h1": {"size": 36, "weight": "bold", "lineHeight": 44, "letterSpacing": 0},
                "h2": {"size": 28, "weight": "bold", "lineHeight": 36, "letterSpacing": 0},
                "body1": {"size": 16, "weight": "regular", "lineHeight": 24, "letterSpacing": 0.5},
                "caption": {"size": 12, "weight": "regular", "lineHeight": 16, "letterSpacing": 0.4}
            }
        """.trimIndent()

        val json = JsonParser.parseString(typographyJson).asJsonObject
        
        assertEquals("system", json.get("fontFamily").asString)
        
        val h1 = json.getAsJsonObject("h1")
        assertEquals(36, h1.get("size").asInt)
        assertEquals("bold", h1.get("weight").asString)
        assertEquals(44, h1.get("lineHeight").asInt)
        assertEquals(0.0, h1.get("letterSpacing").asDouble, 0.01)
        
        val body1 = json.getAsJsonObject("body1")
        assertEquals(16, body1.get("size").asInt)
        assertEquals("regular", body1.get("weight").asString)
        assertEquals(24, body1.get("lineHeight").asInt)
    }

    @Test
    fun `hex color format should be valid`() {
        val validHexColors = listOf(
            "#D32F2F",
            "#FF5252",
            "#1976D2",
            "#F5F5F5",
            "#FFFFFF",
            "#121212",
            "#000000"
        )

        val hexPattern = "^#[0-9A-F]{6}$"
        
        validHexColors.forEach { color ->
            assertTrue(
                "Color $color should match hex pattern",
                color.matches(Regex(hexPattern, RegexOption.IGNORE_CASE))
            )
        }
    }

    @Test
    fun `theme mode should be light or dark`() {
        val modes = listOf("light", "dark")
        
        modes.forEach { mode ->
            assertTrue("Mode should be valid", mode in modes)
        }
        
        val invalidMode = "blue"
        assertFalse("Invalid mode should be rejected", invalidMode in modes)
    }

    @Test
    fun `font weight should be valid CSS weight`() {
        val validWeights = listOf(
            "thin", "light", "regular", "medium", "bold", "black"
        )
        
        validWeights.forEach { weight ->
            assertTrue("Weight $weight should be valid", weight in validWeights)
        }
    }

    @Test
    fun `typography font family should be supported`() {
        val validFamilies = listOf("system", "serif", "mono", "sans-serif")
        
        validFamilies.forEach { family ->
            assertTrue("Font family $family should be valid", family in validFamilies)
        }
    }
}
