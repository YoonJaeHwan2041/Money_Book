package com.jaehwan.moneybook.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = MbGreen,
    secondary = MbGreenDark,
    tertiary = MbRed,
    background = MbSurfaceAlt,
    surface = MbSurface,
    onPrimary = MbSurface,
    onSecondary = MbSurface,
    onTertiary = MbSurface,
    onBackground = MbTextPrimary,
    onSurface = MbTextPrimary,
    onSurfaceVariant = MbTextSecondary,
)

private val LightColorScheme = lightColorScheme(
    primary = MbGreen,
    secondary = MbGreenDark,
    tertiary = MbRed,
    background = MbSurfaceAlt,
    surface = MbSurface,
    surfaceVariant = MbSurfaceAlt,
    outline = MbBorder,
    onPrimary = MbSurface,
    onSecondary = MbSurface,
    onTertiary = MbSurface,
    onBackground = MbTextPrimary,
    onSurface = MbTextPrimary,
    onSurfaceVariant = MbTextSecondary,
)

@Composable
fun MoneyBookTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}