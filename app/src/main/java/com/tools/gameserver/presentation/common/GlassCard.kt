package com.tools.gameserver.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tools.gameserver.core.theme.AppColors

/**
 * Liquid Glass 液态玻璃卡片组件
 *
 * 设计层次 (从外到内)：
 * 1. 超柔和阴影 — 模拟玻璃浮起的深度感
 * 2. 磨砂玻璃底层 — 半透明白/暗填充
 * 3. 折射色散层 — 微弱的彩色渐变模拟光的折射
 * 4. 顶部镜面高光带 — 模拟光源照射的强反射
 * 5. 极细渐变边框 — 顶部亮底部暗的玻璃边缘
 *
 * @param elevated true=浮起玻璃（更明显的阴影和高光），false=嵌入玻璃
 * @param cornerRadius 液态玻璃特征性大圆角
 * @param showSpecular 是否显示顶部镜面高光带
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    elevated: Boolean = true,
    cornerRadius: Dp = 20.dp,
    showSpecular: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    val shadowElevation = if (elevated) 8.dp else 0.dp

    // 缓存 @Composable 色值到非 Composable lambda 中使用
    val specular = AppColors.GlassSpecular
    val refraction = AppColors.GlassRefraction
    val edgeGlow = AppColors.GlassEdgeGlow
    val glassFill = AppColors.GlassFill
    val shadowDark = AppColors.ShadowDark
    val highlight = AppColors.GlassHighlight
    val border = AppColors.GlassBorder

    Box(
        modifier = modifier
            // 第1层：超柔和阴影
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = shadowDark,
                spotColor = shadowDark
            )
            .clip(shape)
            // 第2层：磨砂玻璃底层
            .background(glassFill)
            // 第3层：折射色散 + 第4层：顶部镜面高光
            .drawBehind {
                if (elevated && showSpecular) {
                    // 顶部镜面高光带
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(specular, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.15f
                        )
                    )
                    // 微弱折射色散层
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(refraction, Color.Transparent, edgeGlow),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        )
                    )
                }
            }
            // 第5层：极细渐变边框
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(highlight, border, border),
                    startY = 0f,
                    endY = 200f
                ),
                shape = shape
            ),
        content = content
    )
}

/**
 * Liquid Glass 扁平变体 — 用于嵌入式卡片（无阴影、无高光）
 */
@Composable
fun GlassCardFlat(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = modifier
            .clip(shape)
            .background(AppColors.GlassFrost)
            .border(
                width = 1.dp,
                color = AppColors.GlassBorder,
                shape = shape
            ),
        content = content
    )
}