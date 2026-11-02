package com.calculator.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.calculator.app.data.preferences.AppThemeMode

data class CalculatorColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val numberKeyBg: Color,
    val numberKeyText: Color,
    val operatorKeyBg: Color,
    val operatorKeyText: Color,
    val functionKeyBg: Color,
    val functionKeyText: Color,
    val clearKeyBg: Color,
    val clearKeyText: Color,
    val equalsKeyBg: Color,
    val equalsKeyText: Color
)

val LocalCalculatorColors = staticCompositionLocalOf<CalculatorColors> {
    error("No CalculatorColors provided")
}

private val LightCalculatorColors = CalculatorColors(
    background = LightBackground,
    surface = LightSurface,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    numberKeyBg = LightNumberKey,
    numberKeyText = LightTextPrimary,
    operatorKeyBg = LightOperatorKey,
    operatorKeyText = LightOperatorText,
    functionKeyBg = LightFunctionKey,
    functionKeyText = LightFunctionText,
    clearKeyBg = LightClearKey,
    clearKeyText = LightClearText,
    equalsKeyBg = LightEqualsKey,
    equalsKeyText = LightEqualsText
)

// Dark mode uses the pure AMOLED Pitch Black theme
private val DarkCalculatorColors = CalculatorColors(
    background = DarkBackground,
    surface = DarkSurface,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    numberKeyBg = DarkNumberKey,
    numberKeyText = DarkNumberKeyText,
    operatorKeyBg = DarkOperatorKey,
    operatorKeyText = DarkOperatorText,
    functionKeyBg = DarkFunctionKey,
    functionKeyText = DarkFunctionText,
    clearKeyBg = DarkClearKey,
    clearKeyText = DarkClearText,
    equalsKeyBg = DarkEqualsKey,
    equalsKeyText = DarkEqualsText
)

private val LightMaterialColorScheme = lightColorScheme(
    primary = ElectricBlue,
    onPrimary = Color.White,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant
)

private val DarkMaterialColorScheme = darkColorScheme(
    primary = ElectricBlueDark,
    onPrimary = Color.Black,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant
)

@Composable
fun CalculatorTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()

    val (isDark, calcColors, colorScheme) = when (themeMode) {
        AppThemeMode.SYSTEM -> if (systemInDark) {
            Triple(true, DarkCalculatorColors, DarkMaterialColorScheme)
        } else {
            Triple(false, LightCalculatorColors, LightMaterialColorScheme)
        }
        AppThemeMode.LIGHT -> Triple(false, LightCalculatorColors, LightMaterialColorScheme)
        AppThemeMode.DARK -> Triple(true, DarkCalculatorColors, DarkMaterialColorScheme)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = calcColors.background.toArgb()
                window.navigationBarColor = calcColors.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !isDark
                insetsController.isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(LocalCalculatorColors provides calcColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
