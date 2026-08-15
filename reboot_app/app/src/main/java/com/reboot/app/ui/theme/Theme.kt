package com.reboot.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val RebootColorScheme = darkColorScheme(
    primary = AccentViolet,
    onPrimary = TextPrimary,
    secondary = AccentCyan,
    onSecondary = BgDeep,
    tertiary = AccentPink,
    background = BgDeep,
    onBackground = TextPrimary,
    surface = CardDark,
    onSurface = TextPrimary,
    surfaceVariant = CardMid,
    error = AccentRed,
)

@Composable
fun RebootTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        val ctx = view.context
        val window = (ctx as? android.app.Activity)?.window
        window?.let {
            it.statusBarColor = BgDeep.toArgb()
            it.navigationBarColor = BgDeep.toArgb()
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = false
        }
    }
    MaterialTheme(
        colorScheme = RebootColorScheme,
        typography = RebootTypography,
        content = content
    )
}
