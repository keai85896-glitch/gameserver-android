package com.tools.gameserver.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Glassmorphism + Neumorphism 混合主题系统
 *
 * - 自动根据 ThemeMode（LIGHT/DARK/SYSTEM）切换色板
 * - 通过 CompositionLocalProvider 注入自定义色板
 */
object AppTheme {

    @Composable
    fun GameServerTheme(
        themeModeService: ThemeModeService? = null,
        content: @Composable () -> Unit
    ) {
        val savedMode by themeModeService?.themeModeFlow?.collectAsState(initial = ThemeMode.SYSTEM)
            ?: androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(ThemeMode.SYSTEM) }

        val systemDark = isSystemInDarkTheme()
        val useDark = when (savedMode) {
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
            ThemeMode.SYSTEM -> systemDark
        }

        val palette = if (useDark) darkPalette else lightPalette
        val scheme = if (useDark) darkColorScheme(
            primary = palette.systemBlue,
            onPrimary = palette.textOnPrimary,
            primaryContainer = palette.systemBlueLight,
            onPrimaryContainer = palette.systemBluePressed,
            secondary = palette.systemGreen,
            onSecondary = Color.Black,
            tertiary = palette.systemPurple,
            onTertiary = Color.Black,
            error = palette.systemRed,
            onError = Color.Black,
            background = palette.backgroundPrimary,
            onBackground = palette.textPrimary,
            surface = palette.backgroundCard,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.backgroundSecondary,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderMain,
            outlineVariant = palette.separatorLight,
        ) else lightColorScheme(
            primary = palette.systemBlue,
            onPrimary = palette.textOnPrimary,
            primaryContainer = palette.systemBlueLight,
            onPrimaryContainer = palette.systemBluePressed,
            secondary = palette.systemGreen,
            onSecondary = Color.White,
            tertiary = palette.systemPurple,
            onTertiary = Color.White,
            error = palette.systemRed,
            onError = Color.White,
            background = palette.backgroundPrimary,
            onBackground = palette.textPrimary,
            surface = palette.backgroundCard,
            onSurface = palette.textPrimary,
            surfaceVariant = palette.backgroundSecondary,
            onSurfaceVariant = palette.textSecondary,
            outline = palette.borderMain,
            outlineVariant = palette.separatorLight,
        )

        androidx.compose.runtime.CompositionLocalProvider(LocalCurrentColors provides palette) {
            MaterialTheme(
                colorScheme = scheme,
                typography = AppTypography.Typography,
                content = {
                    val view = LocalView.current
                    if (!view.isInEditMode) {
                        SideEffect {
                            val context = view.context
                            if (context is Activity) {
                                val window = context.window
                                @Suppress("DEPRECATION")
                                window.navigationBarColor = Color.Transparent.toArgb()
                                WindowCompat.getInsetsController(window, view)
                                    .isAppearanceLightStatusBars = !useDark
                            }
                        }
                    }
                    content()
                }
            )
        }
    }
}