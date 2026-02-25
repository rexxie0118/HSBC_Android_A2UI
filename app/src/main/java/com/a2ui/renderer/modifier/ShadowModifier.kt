package com.a2ui.renderer.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shadow configuration from theme JSON
 */
data class ShadowConfig(
    val color: String = "#000000",
    val alpha: Float = 0.1f,
    val blur: Int = 8,
    val offsetX: Int = 0,
    val offsetY: Int = 2
)

/**
 * Shadow modifier for applying theme-defined shadows
 */
object ShadowModifier {
    
    /**
     * Get predefined shadow configs
     */
    fun getShadowForVariant(variant: String): ShadowConfig {
        return when (variant) {
            "card" -> ShadowConfig(alpha = 0.1f, blur = 8, offsetY = 2)
            "elevated" -> ShadowConfig(alpha = 0.15f, blur = 12, offsetY = 4)
            "floating" -> ShadowConfig(alpha = 0.2f, blur = 16, offsetY = 8)
            "pressed" -> ShadowConfig(alpha = 0.05f, blur = 4, offsetY = 1)
            "none" -> ShadowConfig(alpha = 0f, blur = 0)
            else -> ShadowConfig()
        }
    }
    
    /**
     * Get elevation in Dp for shadow level (0-4)
     */
    fun getElevationForLevel(level: Int): Dp {
        return when (level) {
            0 -> 0.dp
            1 -> 2.dp
            2 -> 4.dp
            3 -> 8.dp
            4 -> 16.dp
            else -> 2.dp
        }
    }
    
    /**
     * Get shadow config for elevation level
     */
    fun getShadowForLevel(level: Int): ShadowConfig {
        return when (level) {
            0 -> ShadowConfig(alpha = 0f, blur = 0)
            1 -> ShadowConfig(alpha = 0.1f, blur = 8, offsetY = 2)
            2 -> ShadowConfig(alpha = 0.15f, blur = 12, offsetY = 4)
            3 -> ShadowConfig(alpha = 0.2f, blur = 16, offsetY = 8)
            4 -> ShadowConfig(alpha = 0.05f, blur = 4, offsetY = 1)
            else -> ShadowConfig()
        }
    }
}

/**
 * Elevation levels for quick reference
 */
object ElevationLevels {
    val None = 0.dp
    val Small = 2.dp
    val Medium = 4.dp
    val Large = 8.dp
    val XL = 16.dp
    val XXL = 24.dp
}
