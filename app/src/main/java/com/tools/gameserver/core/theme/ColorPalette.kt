package com.tools.gameserver.core.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 色板数据类 — Liquid Glass (液态玻璃) 设计语言
 *
 * 设计理念：iOS 26 Liquid Glass
 * - 极致透明：高透明度毛玻璃层，透出背景色彩
 * - 多层折射：双层渐变模拟光的折射与色散
 * - 顶部强光条：顶部边缘的镜面高光带
 * - 流体圆角：大圆角 + 柔和阴影
 * - 环境着色：玻璃边缘吸收背景环境色
 *
 * Light: 清透暖白底 + 柔光折射
 * Dark:  深邃蓝紫底 + 冷光折射
 */
data class ColorPalette(
    val systemBlue: Color,
    val systemBluePressed: Color,
    val systemBlueLight: Color,
    val systemGreen: Color,
    val systemRed: Color,
    val systemOrange: Color,
    val systemYellow: Color,
    val systemPurple: Color,
    val systemTeal: Color,
    val systemIndigo: Color,
    val backgroundPrimary: Color,
    val backgroundCard: Color,
    val backgroundSecondary: Color,
    val backgroundOverlay: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    val textDisabled: Color,
    val textOnPrimary: Color,
    val borderMain: Color,
    val borderFocus: Color,
    val divider: Color,
    val separatorLight: Color,
    val statusSuccess: Color,
    val statusRedirect: Color,
    val statusClientError: Color,
    val statusServerError: Color,
    val methodGet: Color,
    val methodPost: Color,
    val methodPut: Color,
    val methodDelete: Color,
    val methodPatch: Color,
    // 液态玻璃核心色
    val glassFill: Color,
    val glassBorder: Color,
    val glassHighlight: Color,
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
    val accentCyan: Color,
    val accentViolet: Color,
    val accentGlow: Color,
    // 阴影（液态玻璃：超柔和）
    val shadowLight: Color,
    val shadowDark: Color,
    val neumorphismRaised: Color,
    val neumorphismPressed: Color,
    // 液态玻璃专用
    val glassSpecular: Color,       // 顶部镜面高光带
    val glassRefraction: Color,     // 折射色散层
    val glassEdgeGlow: Color,       // 边缘环境光
    val glassFrost: Color,          // 磨砂底层
)

internal val LocalCurrentColors = staticCompositionLocalOf { lightPalette }

/**
 * Liquid Glass Light 色板 — 清透暖白
 *
 * 灵感：阳光穿过磨砂玻璃的柔和折射
 * - 底色偏暖白 (F2F2F7)，玻璃层极高透明度
 * - 顶部高光为纯白镜面反射
 * - 边缘微弱的蓝紫环境光色散
 */
internal val lightPalette = ColorPalette(
    systemBlue = Color(0xFF007AFF),
    systemBluePressed = Color(0xFF0056CC),
    systemBlueLight = Color(0xFFD6E4FF),
    systemGreen = Color(0xFF34C759),
    systemRed = Color(0xFFFF3B30),
    systemOrange = Color(0xFFFF9500),
    systemYellow = Color(0xFFFFCC00),
    systemPurple = Color(0xFFAF52DE),
    systemTeal = Color(0xFF5AC8FA),
    systemIndigo = Color(0xFF5856D6),
    // 背景 — 清透暖白
    backgroundPrimary = Color(0xFFF2F2F7),
    backgroundCard = Color(0xFFFFFFFF),
    backgroundSecondary = Color(0xFFE5E5EA),
    backgroundOverlay = Color(0xFFF8F8FC),
    // 文本 — 更深更清晰
    textPrimary = Color(0xFF000000),
    textSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f),
    textHint = Color(0xFF3C3C43).copy(alpha = 0.3f),
    textDisabled = Color(0xFF3C3C43).copy(alpha = 0.18f),
    textOnPrimary = Color(0xFFFFFFFF),
    // 边框 — 极细透明
    borderMain = Color(0x14000000),
    borderFocus = Color(0xFF007AFF),
    divider = Color(0x0D000000),
    separatorLight = Color(0x0A000000),
    // HTTP 状态色
    statusSuccess = Color(0xFF34C759),
    statusRedirect = Color(0xFFFF9500),
    statusClientError = Color(0xFFFF9500),
    statusServerError = Color(0xFFFF3B30),
    // HTTP 方法色
    methodGet = Color(0xFF34C759),
    methodPost = Color(0xFF007AFF),
    methodPut = Color(0xFFFF9500),
    methodDelete = Color(0xFFFF3B30),
    methodPatch = Color(0xFFAF52DE),
    // 液态玻璃 — 极高透明度
    glassFill = Color(0xCCFFFFFF),
    glassBorder = Color(0x28FFFFFF),
    glassHighlight = Color(0x80FFFFFF),
    // 渐变背景 — 柔和彩虹折射
    gradientStart = Color(0xFFE8DEFF),
    gradientMid = Color(0xFFD6E8FF),
    gradientEnd = Color(0xFFE0F5E8),
    // 强调色
    accentCyan = Color(0xFF5AC8FA),
    accentViolet = Color(0xFFAF52DE),
    accentGlow = Color(0x18007AFF),
    // 阴影 — 液态玻璃：超柔和散射
    shadowLight = Color(0x0C000000),
    shadowDark = Color(0x18000000),
    neumorphismRaised = Color(0xFFF8F8FC),
    neumorphismPressed = Color(0xFFE5E5EA),
    // 液态玻璃专用
    glassSpecular = Color(0xFFFFFFFF),
    glassRefraction = Color(0x14AF52DE),
    glassEdgeGlow = Color(0x185AC8FA),
    glassFrost = Color(0x99FFFFFF),
)

/**
 * Liquid Glass Dark 色板 — 深邃蓝紫
 *
 * 灵感：月光透过深色水晶的冷光折射
 * - 底色偏深蓝紫 (0A0A1A)，玻璃层带冷色调
 * - 顶部高光为蓝白色冷光
 * - 边缘微弱的青蓝色环境光色散
 */
internal val darkPalette = ColorPalette(
    systemBlue = Color(0xFF5E9EFF),
    systemBluePressed = Color(0xFF88B8FF),
    systemBlueLight = Color(0xFF1A2744),
    systemGreen = Color(0xFF4ADE80),
    systemRed = Color(0xFFFF6B6B),
    systemOrange = Color(0xFFFFBE76),
    systemYellow = Color(0xFFFFE66D),
    systemPurple = Color(0xFFC084FC),
    systemTeal = Color(0xFF67E8F9),
    systemIndigo = Color(0xFF818CF8),
    // 背景 — 深邃蓝紫
    backgroundPrimary = Color(0xFF0A0A1A),
    backgroundCard = Color(0xFF161630),
    backgroundSecondary = Color(0xFF1E1E3A),
    backgroundOverlay = Color(0xFF252545),
    // 文本 — 清冷白
    textPrimary = Color(0xFFF5F5F7),
    textSecondary = Color(0xFFF5F5F7).copy(alpha = 0.55f),
    textHint = Color(0xFFF5F5F7).copy(alpha = 0.3f),
    textDisabled = Color(0xFFF5F5F7).copy(alpha = 0.15f),
    textOnPrimary = Color(0xFFFFFFFF),
    // 边框
    borderMain = Color(0x18FFFFFF),
    borderFocus = Color(0xFF5E9EFF),
    divider = Color(0x0CFFFFFF),
    separatorLight = Color(0x08FFFFFF),
    // HTTP 状态色
    statusSuccess = Color(0xFF4ADE80),
    statusRedirect = Color(0xFFFFBE76),
    statusClientError = Color(0xFFFFBE76),
    statusServerError = Color(0xFFFF6B6B),
    // HTTP 方法色
    methodGet = Color(0xFF4ADE80),
    methodPost = Color(0xFF5E9EFF),
    methodPut = Color(0xFFFFBE76),
    methodDelete = Color(0xFFFF6B6B),
    methodPatch = Color(0xFFC084FC),
    // 液态玻璃 — 暗色半透明（更高透明度）
    glassFill = Color(0xB31A1A35),
    glassBorder = Color(0x1CFFFFFF),
    glassHighlight = Color(0x40FFFFFF),
    // 渐变背景 — 深蓝紫极光
    gradientStart = Color(0xFF0D0D24),
    gradientMid = Color(0xFF0F0F2A),
    gradientEnd = Color(0xFF111128),
    // 强调色
    accentCyan = Color(0xFF67E8F9),
    accentViolet = Color(0xFFC084FC),
    accentGlow = Color(0x205E9EFF),
    // 阴影 — 液态玻璃：柔和发光
    shadowLight = Color(0x08FFFFFF),
    shadowDark = Color(0x28000000),
    neumorphismRaised = Color(0xFF222244),
    neumorphismPressed = Color(0xFF0D0D20),
    // 液态玻璃专用
    glassSpecular = Color(0x38FFFFFF),
    glassRefraction = Color(0x12C084FC),
    glassEdgeGlow = Color(0x1467E8F9),
    glassFrost = Color(0x661A1A35),
)

internal val currentColors: ColorPalette
    @Composable @ReadOnlyComposable get() = LocalCurrentColors.current