package com.a2ui.renderer.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.a2ui.renderer.config.ConfigManager

@Composable
private fun getColorScheme(theme: com.a2ui.renderer.config.Theme, darkTheme: Boolean): ColorScheme {
    val colors = theme.colors
    return if (darkTheme) {
        darkColorScheme(
            primary = colors["primary"]?.toColor() ?: Color.Unspecified,
            onPrimary = colors["onPrimary"]?.toColor() ?: Color.Unspecified,
            primaryContainer = colors["primaryVariant"]?.toColor() ?: Color.Unspecified,
            secondary = colors["secondary"]?.toColor() ?: Color.Unspecified,
            onSecondary = colors["onSecondary"]?.toColor() ?: Color.Unspecified,
            secondaryContainer = colors["secondaryVariant"]?.toColor() ?: Color.Unspecified,
            tertiary = colors["secondary"]?.toColor() ?: Color.Unspecified,
            onTertiary = colors["onSecondary"]?.toColor() ?: Color.Unspecified,
            error = colors["error"]?.toColor() ?: Color.Unspecified,
            onError = colors["onError"]?.toColor() ?: Color.Unspecified,
            background = colors["background"]?.toColor() ?: Color.Unspecified,
            onBackground = colors["onBackground"]?.toColor() ?: Color.Unspecified,
            surface = colors["surface"]?.toColor() ?: Color.Unspecified,
            onSurface = colors["onSurface"]?.toColor() ?: Color.Unspecified,
            outline = colors["outline"]?.toColor() ?: Color.Unspecified,
            outlineVariant = colors["outlineVariant"]?.toColor() ?: Color.Unspecified
        )
    } else {
        lightColorScheme(
            primary = colors["primary"]?.toColor() ?: Color.Unspecified,
            onPrimary = colors["onPrimary"]?.toColor() ?: Color.Unspecified,
            primaryContainer = colors["primaryVariant"]?.toColor() ?: Color.Unspecified,
            secondary = colors["secondary"]?.toColor() ?: Color.Unspecified,
            onSecondary = colors["onSecondary"]?.toColor() ?: Color.Unspecified,
            secondaryContainer = colors["secondaryVariant"]?.toColor() ?: Color.Unspecified,
            tertiary = colors["secondary"]?.toColor() ?: Color.Unspecified,
            onTertiary = colors["onSecondary"]?.toColor() ?: Color.Unspecified,
            error = colors["error"]?.toColor() ?: Color.Unspecified,
            onError = colors["onError"]?.toColor() ?: Color.Unspecified,
            background = colors["background"]?.toColor() ?: Color.Unspecified,
            onBackground = colors["onBackground"]?.toColor() ?: Color.Unspecified,
            surface = colors["surface"]?.toColor() ?: Color.Unspecified,
            onSurface = colors["onSurface"]?.toColor() ?: Color.Unspecified,
            outline = colors["outline"]?.toColor() ?: Color.Unspecified,
            outlineVariant = colors["outlineVariant"]?.toColor() ?: Color.Unspecified
        )
    }
}

@Composable
private fun getTypography(theme: com.a2ui.renderer.config.Theme): Typography {
    val typography = theme.typography
    val fontFamily = when (typography.fontFamily) {
        "system" -> FontFamily.SansSerif
        "serif" -> FontFamily.Serif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }
    
    return Typography(
        displayLarge = createTextStyle(typography.h1, fontFamily),
        displayMedium = createTextStyle(typography.h2, fontFamily),
        displaySmall = createTextStyle(typography.h3, fontFamily),
        headlineLarge = createTextStyle(typography.h4, fontFamily),
        headlineMedium = createTextStyle(typography.h5, fontFamily),
        headlineSmall = createTextStyle(typography.h6, fontFamily),
        bodyLarge = createTextStyle(typography.body1, fontFamily),
        bodyMedium = createTextStyle(typography.body2, fontFamily),
        bodySmall = createTextStyle(typography.caption, fontFamily),
        labelLarge = createTextStyle(typography.button, fontFamily),
        labelMedium = createTextStyle(typography.overline, fontFamily),
        labelSmall = createTextStyle(typography.caption, fontFamily)
    )
}

@Composable
private fun createTextStyle(textStyle: com.a2ui.renderer.config.TextStyle, fontFamily: FontFamily): androidx.compose.ui.text.TextStyle {
    val fontWeight = when (textStyle.weight) {
        "bold" -> FontWeight.Bold
        "medium" -> FontWeight.Medium
        "light" -> FontWeight.Light
        "thin" -> FontWeight.Thin
        else -> FontWeight.Normal
    }
    
    return androidx.compose.ui.text.TextStyle(
        fontSize = textStyle.size.sp,
        fontWeight = fontWeight,
        lineHeight = textStyle.lineHeight.sp,
        letterSpacing = textStyle.letterSpacing.sp,
        fontFamily = fontFamily
    )
}

private fun String.toColor(): Color {
    return try {
        Color(AndroidColor.parseColor(this))
    } catch (e: Exception) {
        Color.Unspecified
    }
}

@Composable
fun A2UIRendererTheme(
    darkTheme: Boolean = ConfigManager.getGlobalSettings()?.theme?.currentMode == "dark",
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val theme by ConfigManager.themeFlow.collectAsState()
    if (theme == null) {
        A2UIRendererThemeFallback(darkTheme, content)
        return
    }
    
    val colorScheme = getColorScheme(theme!!, darkTheme)
    val typography = getTypography(theme!!)
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
private fun A2UIRendererThemeFallback(
    darkTheme: Boolean,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFFF5252),
            secondary = Color(0xFF64B5F6),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFFD32F2F),
            secondary = Color(0xFF1976D2),
            background = Color(0xFFF5F5F5),
            surface = Color(0xFFFFFFFF)
        )
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
