package com.a2ui.renderer.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.a2ui.renderer.config.ConfigManager

/**
 * Theme picker component that allows users to switch between light and dark themes
 */
@Composable
fun ThemePicker(
    modifier: Modifier = Modifier,
    onThemeChanged: ((themeId: String) -> Unit)? = null
) {
    val currentTheme = ConfigManager.themeFlow.collectAsState(initial = null).value
    val isDarkTheme = currentTheme?.mode == "dark"
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = LinearEasing)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Light theme option
                ThemeOptionButton(
                    label = "Light",
                    isSelected = !isDarkTheme,
                    backgroundColor = Color(0xFFF5F5F5),
                    accentColor = Color(0xFFD32F2F),
                    onClick = {
                        ConfigManager.setTheme("banking_light")
                        onThemeChanged?.invoke("banking_light")
                    }
                )
                
                // Dark theme option
                ThemeOptionButton(
                    label = "Dark",
                    isSelected = isDarkTheme,
                    backgroundColor = Color(0xFF1E1E1E),
                    accentColor = Color(0xFFFF5252),
                    onClick = {
                        ConfigManager.setTheme("banking_dark")
                        onThemeChanged?.invoke("banking_dark")
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    isSelected: Boolean,
    backgroundColor: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) {
        accentColor
    } else {
        MaterialTheme.colorScheme.outline
    }
    
    val borderWidth = if (isSelected) 3.dp else 1.dp
    
    Column(
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isSelected) {
                    accentColor.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Theme preview circle
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .padding(2.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Simple theme toggle button for quick switching
 */
@Composable
fun ThemeToggleButton(
    modifier: Modifier = Modifier,
    onToggle: ((isDark: Boolean) -> Unit)? = null
) {
    val currentTheme = ConfigManager.themeFlow.collectAsState(initial = null).value
    val isDarkTheme = currentTheme?.mode == "dark"
    
    IconButton(
        modifier = modifier,
        onClick = {
            val newTheme = if (isDarkTheme) "banking_light" else "banking_dark"
            ConfigManager.setTheme(newTheme)
            onToggle?.invoke(!isDarkTheme)
        }
    ) {
        Text(
            text = if (isDarkTheme) "☀️" else "🌙",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
