package com.tools.gameserver.presentation.common

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors

/**
 * Liquid Glass 液态玻璃按钮
 *
 * 设计：高透明度玻璃 + 顶部镜面高光 + 流体按压缩放
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    cornerRadius: Dp = 14.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "btn_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 0.dp else if (enabled) 4.dp else 0.dp,
        animationSpec = tween(100),
        label = "btn_elevation"
    )

    val shape = RoundedCornerShape(cornerRadius)
    // 缓存 Composable 色值
    val specular = AppColors.GlassSpecular
    val glassFill = AppColors.GlassFill
    val shadowDark = AppColors.ShadowDark
    val highlight = AppColors.GlassHighlight
    val border = AppColors.GlassBorder
    val blueColor = if (enabled) AppColors.SystemBlue else AppColors.TextDisabled

    Box(
        modifier = modifier
            .height(44.dp)
            .scale(scale)
            .shadow(elevation, shape, ambientColor = shadowDark, spotColor = shadowDark)
            .clip(shape)
            .background(glassFill)
            .drawBehind {
                if (!isPressed && enabled) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(specular, Color.Transparent),
                            startY = 0f,
                            endY = size.height * 0.2f
                        )
                    )
                }
            }
            .border(1.dp, Brush.verticalGradient(listOf(highlight, border)), shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(icon, null, modifier = Modifier.size(18.dp), tint = blueColor)
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = blueColor)
        }
    }
}

/**
 * Liquid Glass Chip — 轻量级标签按钮
 */
@Composable
fun QuickChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false
) {
    val shape = RoundedCornerShape(12.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "chip_scale"
    )

    Box(
        modifier = modifier
            .height(32.dp)
            .scale(scale)
            .shadow(
                if (selected) 2.dp else 0.dp,
                shape,
                ambientColor = AppColors.ShadowDark,
                spotColor = AppColors.ShadowDark
            )
            .clip(shape)
            .background(
                if (selected) AppColors.SystemBlue.copy(alpha = 0.12f)
                else AppColors.GlassFrost
            )
            .border(
                1.dp,
                if (selected) AppColors.SystemBlue.copy(alpha = 0.3f)
                else AppColors.GlassBorder,
                shape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (selected) AppColors.SystemBlue else AppColors.TextSecondary
        )
    }
}