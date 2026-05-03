package com.tools.gameserver.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Glassmorphism + Neumorphism 混合色彩系统
 *
 * 设计理念：
 * - Light: 暖灰白底 + 柔和双阴影 + 半透明毛玻璃层
 * - Dark:  深蓝灰底 + 霓虹微光 + 暗色玻璃层
 *
 * 用法：AppColors.SystemBlue → 根据当前主题自动返回 Light/Dark 色值
 */
object AppColors {
    // ==================== 系统色 ====================
    val SystemBlue: Color @Composable @ReadOnlyComposable get() = currentColors.systemBlue
    val SystemBluePressed: Color @Composable @ReadOnlyComposable get() = currentColors.systemBluePressed
    val SystemBlueLight: Color @Composable @ReadOnlyComposable get() = currentColors.systemBlueLight
    val SystemGreen: Color @Composable @ReadOnlyComposable get() = currentColors.systemGreen
    val SystemRed: Color @Composable @ReadOnlyComposable get() = currentColors.systemRed
    val SystemOrange: Color @Composable @ReadOnlyComposable get() = currentColors.systemOrange
    val SystemYellow: Color @Composable @ReadOnlyComposable get() = currentColors.systemYellow
    val SystemPurple: Color @Composable @ReadOnlyComposable get() = currentColors.systemPurple
    val SystemTeal: Color @Composable @ReadOnlyComposable get() = currentColors.systemTeal
    val SystemIndigo: Color @Composable @ReadOnlyComposable get() = currentColors.systemIndigo

    // ==================== 背景 ====================
    val BackgroundPrimary: Color @Composable @ReadOnlyComposable get() = currentColors.backgroundPrimary
    val BackgroundCard: Color @Composable @ReadOnlyComposable get() = currentColors.backgroundCard
    val BackgroundSecondary: Color @Composable @ReadOnlyComposable get() = currentColors.backgroundSecondary
    val BackgroundOverlay: Color @Composable @ReadOnlyComposable get() = currentColors.backgroundOverlay

    // ==================== 文本 ====================
    val TextPrimary: Color @Composable @ReadOnlyComposable get() = currentColors.textPrimary
    val TextSecondary: Color @Composable @ReadOnlyComposable get() = currentColors.textSecondary
    val TextHint: Color @Composable @ReadOnlyComposable get() = currentColors.textHint
    val TextDisabled: Color @Composable @ReadOnlyComposable get() = currentColors.textDisabled
    val TextOnPrimary: Color @Composable @ReadOnlyComposable get() = currentColors.textOnPrimary

    // ==================== 边框/分割线 ====================
    val BorderMain: Color @Composable @ReadOnlyComposable get() = currentColors.borderMain
    val BorderFocus: Color @Composable @ReadOnlyComposable get() = currentColors.borderFocus
    val Divider: Color @Composable @ReadOnlyComposable get() = currentColors.divider
    val SeparatorLight: Color @Composable @ReadOnlyComposable get() = currentColors.separatorLight

    // ==================== HTTP 状态色 ====================
    val StatusSuccess: Color @Composable @ReadOnlyComposable get() = currentColors.statusSuccess
    val StatusRedirect: Color @Composable @ReadOnlyComposable get() = currentColors.statusRedirect
    val StatusClientError: Color @Composable @ReadOnlyComposable get() = currentColors.statusClientError
    val StatusServerError: Color @Composable @ReadOnlyComposable get() = currentColors.statusServerError

    // ==================== HTTP 方法色 ====================
    val MethodGet: Color @Composable @ReadOnlyComposable get() = currentColors.methodGet
    val MethodPost: Color @Composable @ReadOnlyComposable get() = currentColors.methodPost
    val MethodPut: Color @Composable @ReadOnlyComposable get() = currentColors.methodPut
    val MethodDelete: Color @Composable @ReadOnlyComposable get() = currentColors.methodDelete
    val MethodPatch: Color @Composable @ReadOnlyComposable get() = currentColors.methodPatch

    // ==================== 毛玻璃 ====================
    val GlassFill: Color @Composable @ReadOnlyComposable get() = currentColors.glassFill
    val GlassBorder: Color @Composable @ReadOnlyComposable get() = currentColors.glassBorder
    val GlassHighlight: Color @Composable @ReadOnlyComposable get() = currentColors.glassHighlight

    // ==================== 渐变 ====================
    val GradientStart: Color @Composable @ReadOnlyComposable get() = currentColors.gradientStart
    val GradientMid: Color @Composable @ReadOnlyComposable get() = currentColors.gradientMid
    val GradientEnd: Color @Composable @ReadOnlyComposable get() = currentColors.gradientEnd

    // ==================== 强调色 ====================
    val AccentCyan: Color @Composable @ReadOnlyComposable get() = currentColors.accentCyan
    val AccentViolet: Color @Composable @ReadOnlyComposable get() = currentColors.accentViolet
    val AccentGlow: Color @Composable @ReadOnlyComposable get() = currentColors.accentGlow

    // ==================== 新拟态阴影 ====================
    val ShadowLight: Color @Composable @ReadOnlyComposable get() = currentColors.shadowLight
    val ShadowDark: Color @Composable @ReadOnlyComposable get() = currentColors.shadowDark
    val NeumorphismRaised: Color @Composable @ReadOnlyComposable get() = currentColors.neumorphismRaised
    val NeumorphismPressed: Color @Composable @ReadOnlyComposable get() = currentColors.neumorphismPressed

    // ==================== 液态玻璃专用 ====================
    val GlassSpecular: Color @Composable @ReadOnlyComposable get() = currentColors.glassSpecular
    val GlassRefraction: Color @Composable @ReadOnlyComposable get() = currentColors.glassRefraction
    val GlassEdgeGlow: Color @Composable @ReadOnlyComposable get() = currentColors.glassEdgeGlow
    val GlassFrost: Color @Composable @ReadOnlyComposable get() = currentColors.glassFrost

    // ==================== 渐变刷 ====================
    val PrimaryGradient: Brush @Composable @ReadOnlyComposable get() = Brush.linearGradient(listOf(SystemBlue, AccentViolet))

    // ==================== 兼容别名 (无弃用标记) ====================
    val Primary: Color @Composable @ReadOnlyComposable get() = SystemBlue
    val Surface: Color @Composable @ReadOnlyComposable get() = BackgroundCard
    val SurfaceElevated: Color @Composable @ReadOnlyComposable get() = BackgroundSecondary
    val GlassCard: Color @Composable @ReadOnlyComposable get() = GlassFill
    val SurfaceBorder: Color @Composable @ReadOnlyComposable get() = BorderMain
    val White: Color get() = Color.White
    val Background: Color @Composable @ReadOnlyComposable get() = BackgroundPrimary
    val Error: Color @Composable @ReadOnlyComposable get() = SystemRed
    val TextOnNeon: Color @Composable @ReadOnlyComposable get() = TextOnPrimary
    val SuccessLight: Color @Composable @ReadOnlyComposable get() = SystemGreen.copy(alpha = 0.12f)
    val ErrorLight: Color @Composable @ReadOnlyComposable get() = SystemRed.copy(alpha = 0.12f)
}