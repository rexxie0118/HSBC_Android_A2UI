package com.a2ui.renderer.modifier

import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ShadowModifier
 */
class ShadowModifierTest {

    @Test
    fun `getShadowForVariant should return correct config for card`() {
        val shadow = ShadowModifier.getShadowForVariant("card")
        
        assertEquals(0.1f, shadow.alpha, 0.01f)
        assertEquals(8, shadow.blur)
        assertEquals(2, shadow.offsetY)
    }

    @Test
    fun `getShadowForVariant should return correct config for elevated`() {
        val shadow = ShadowModifier.getShadowForVariant("elevated")
        
        assertEquals(0.15f, shadow.alpha, 0.01f)
        assertEquals(12, shadow.blur)
        assertEquals(4, shadow.offsetY)
    }

    @Test
    fun `getShadowForVariant should return correct config for floating`() {
        val shadow = ShadowModifier.getShadowForVariant("floating")
        
        assertEquals(0.2f, shadow.alpha, 0.01f)
        assertEquals(16, shadow.blur)
        assertEquals(8, shadow.offsetY)
    }

    @Test
    fun `getShadowForVariant should return correct config for pressed`() {
        val shadow = ShadowModifier.getShadowForVariant("pressed")
        
        assertEquals(0.05f, shadow.alpha, 0.01f)
        assertEquals(4, shadow.blur)
        assertEquals(1, shadow.offsetY)
    }

    @Test
    fun `getShadowForVariant should return none shadow for none variant`() {
        val shadow = ShadowModifier.getShadowForVariant("none")
        
        assertEquals(0f, shadow.alpha, 0.01f)
        assertEquals(0, shadow.blur)
    }

    @Test
    fun `getShadowForVariant should return default for unknown variant`() {
        val shadow = ShadowModifier.getShadowForVariant("unknown")
        
        assertEquals(0.1f, shadow.alpha, 0.01f)
        assertEquals(8, shadow.blur)
        assertEquals(2, shadow.offsetY)
    }

    @Test
    fun `ShadowConfig should have correct default values`() {
        val shadow = ShadowConfig()
        
        assertEquals("#000000", shadow.color)
        assertEquals(0.1f, shadow.alpha, 0.01f)
        assertEquals(8, shadow.blur)
        assertEquals(0, shadow.offsetX)
        assertEquals(2, shadow.offsetY)
    }

    @Test
    fun `ShadowConfig should accept custom values`() {
        val shadow = ShadowConfig(
            color = "#FF0000",
            alpha = 0.5f,
            blur = 20,
            offsetX = 5,
            offsetY = 10
        )
        
        assertEquals("#FF0000", shadow.color)
        assertEquals(0.5f, shadow.alpha, 0.01f)
        assertEquals(20, shadow.blur)
        assertEquals(5, shadow.offsetX)
        assertEquals(10, shadow.offsetY)
    }

    @Test
    fun `ElevationLevels should have correct values`() {
        assertEquals(0.dp, ElevationLevels.None)
        assertEquals(2.dp, ElevationLevels.Small)
        assertEquals(4.dp, ElevationLevels.Medium)
        assertEquals(8.dp, ElevationLevels.Large)
        assertEquals(16.dp, ElevationLevels.XL)
        assertEquals(24.dp, ElevationLevels.XXL)
    }
}
