package com.tools.gameserver.core.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 统一动画配置 — Deep Space 动效系统
 * 
 * 所有组件的动画参数应从此处引用，确保全应用动画一致性
 */
object Animations {

    // ==================== 通用时长配置 ====================
    
    /** 快速交互反馈 */
    const val DurationFast = 100
    
    /** 标准过渡 */
    const val DurationNormal = 200
    
    /** 慢速优雅过渡 */
    const val DurationSlow = 300
    
    /** 页面转场 */
    const val DurationPage = 250

    // ==================== Spring 弹力配置 ====================
    
    /** 按钮/卡片按压弹力 */
    val SpringBouncy = Spring.DampingRatioMediumBouncy
    
    /** 动态岛展开弹力 */
    val SpringIsland = Spring.DampingRatioMediumBouncy
    
    /** 列表项滑入弹力 */
    val SpringList = Spring.DampingRatioLowBouncy

    // ==================== Easing 曲线 ====================
    
    /** 平滑进入 */
    val EaseInOut = FastOutSlowInEasing
    
    /** 弹性弹出 */
    val EaseOutBack = FastOutSlowInEasing
    
    /** 快速响应 */
    val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)

    // ==================== 动态岛动画效果 ====================
    
    /**
     * 脉冲动画状态 — 用于动态岛发送中状态
     */
    @Composable
    fun pulseAnimation(
        enabled: Boolean = true,
        minAlpha: Float = 0.7f,
        maxAlpha: Float = 1.0f,
        duration: Int = 600
    ): Float {
        if (!enabled) return 1f
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val alpha by infiniteTransition.animateFloat(
            initialValue = minAlpha,
            targetValue = maxAlpha,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseAlpha"
        )
        return alpha
    }

    @Composable
    fun breatheAnimation(
        enabled: Boolean = true,
        minScale: Float = 0.95f,
        maxScale: Float = 1.05f,
        duration: Int = 1500
    ): Float {
        if (!enabled) return 1f
        val infiniteTransition = rememberInfiniteTransition(label = "breathe")
        val scale by infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breatheScale"
        )
        return scale
    }

    @Composable
    fun swingAnimation(
        enabled: Boolean = true,
        minRotation: Float = -3f,
        maxRotation: Float = 3f,
        duration: Int = 2000
    ): Float {
        if (!enabled) return 0f
        val infiniteTransition = rememberInfiniteTransition(label = "swing")
        val rotation by infiniteTransition.animateFloat(
            initialValue = minRotation,
            targetValue = maxRotation,
            animationSpec = infiniteRepeatable(
                animation = tween(duration, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "swingRotation"
        )
        return rotation
    }

    // ==================== 页面转场动画 ====================
    
    /**
     * 页面滑入转场 spec
     */
    fun slideInTransitionSpec() = slideInHorizontally(
        initialOffsetX = { it / 3 },
        animationSpec = tween(DurationPage, easing = EaseOutCubic)
    ) + fadeIn(tween(DurationPage, easing = EaseOutCubic))

    /**
     * 页面滑出转场 spec
     */
    fun slideOutTransitionSpec() = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(DurationPage, easing = EaseOutCubic)
    ) + fadeOut(tween(DurationPage, easing = EaseOutCubic))

    /**
     * 卡片展开/收起动画
     */
    fun expandTransitionSpec() = expandVertically(
        animationSpec = tween(DurationNormal, easing = EaseOutCubic)
    ) + fadeIn(tween(DurationNormal, easing = EaseOutCubic))

    fun shrinkTransitionSpec() = shrinkVertically(
        animationSpec = tween(DurationNormal, easing = EaseOutCubic)
    ) + fadeOut(tween(DurationNormal, easing = EaseOutCubic))
}

/**
 * 动画化的组件修饰器
 */
@Composable
fun Modifier.animatePulse(
    enabled: Boolean = true,
    minAlpha: Float = 0.7f,
    maxAlpha: Float = 1.0f
): Modifier {
    val alpha = Animations.pulseAnimation(enabled, minAlpha, maxAlpha)
    return this.graphicsLayer { this.alpha = alpha }
}

@Composable
fun Modifier.animateBreathe(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f
): Modifier {
    val scale = Animations.breatheAnimation(enabled, minScale, maxScale)
    return this.graphicsLayer { 
        scaleX = scale
        scaleY = scale
    }
}

@Composable
fun Modifier.animateSwing(
    enabled: Boolean = true,
    minRotation: Float = -3f,
    maxRotation: Float = 3f
): Modifier {
    val rotation = Animations.swingAnimation(enabled, minRotation, maxRotation)
    return this.graphicsLayer { this.rotationZ = rotation }
}
