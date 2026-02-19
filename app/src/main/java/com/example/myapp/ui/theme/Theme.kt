package com.example.myapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ---------- Primary color by scheme name (from Wikiappsettings) ----------
private fun primaryColor(scheme: String): Color =
    when (scheme) {
        "green" -> Color(0xFF2E7D32)
        "purple" -> Color(0xFF6A1B9A)
        "red" -> Color(0xFFD32F2F)
        "orange" -> Color(0xFFF57C00)
        "teal" -> Color(0xFF00796B)
        "pink" -> Color(0xFFC2185B)
        "brown" -> Color(0xFF795548)
        "black" -> Color(0xFF000000)
        "cyan" -> Color(0xFF00BCD4)
        else -> Color(0xFF1565C0) // blue
    }

private fun lightColors(primary: Color) = lightColorScheme(
    primary = primary,
    secondary = primary,
    background = Color.White,
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private fun darkColors(primary: Color) = darkColorScheme(
    primary = primary,
    secondary = primary,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private fun blackColors(primary: Color) = darkColorScheme(
    primary = primary,
    secondary = primary,
    background = Color.Black,
    surface = Color.Black,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

/**
 * Theme that applies Settings: theme mode (system/light/dark), color scheme name,
 * black theme toggle, font family, and font scale.
 */
@Composable
fun WikidataMobileLiteTheme(
    themeMode: String = "system",
    colorSchemeName: String = "blue",
    useBlackTheme: Boolean = false,
    fontFamily: FontFamily = FontFamily.SansSerif,
    fontScale: Float = 16f,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }

    val primary = primaryColor(colorSchemeName)
    val colorScheme = when {
        isDark && useBlackTheme -> blackColors(primary)
        isDark -> darkColors(primary)
        else -> lightColors(primary)
    }

    val typography = Typography(
        bodyLarge = Typography().bodyLarge.copy(
            fontFamily = fontFamily,
            fontSize = fontScale.sp
        ),
        bodyMedium = Typography().bodyMedium.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale - 2).sp
        ),
        bodySmall = Typography().bodySmall.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale - 4).sp
        ),
        titleLarge = Typography().titleLarge.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale + 4).sp
        ),
        titleMedium = Typography().titleMedium.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale + 2).sp
        ),
        titleSmall = Typography().titleSmall.copy(
            fontFamily = fontFamily,
            fontSize = fontScale.sp
        ),
        labelLarge = Typography().labelLarge.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale - 1).sp
        ),
        labelMedium = Typography().labelMedium.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale - 2).sp
        ),
        labelSmall = Typography().labelSmall.copy(
            fontFamily = fontFamily,
            fontSize = (fontScale - 4).sp
        )
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
