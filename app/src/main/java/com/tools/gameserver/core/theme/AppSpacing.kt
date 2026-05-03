package com.tools.gameserver.core.theme

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 统一间距 & 排版系统 — 4dp 基础网格
 *
 * 所有间距、圆角、字号均从此处引用，禁止使用魔法数字
 */
object AppSpacing {
    // 基础间距单位
    val xs = 2.dp     // 极小
    val sm = 4.dp     // 小
    val md = 8.dp     // 中
    val lg = 12.dp    // 大
    val xl = 16.dp    // 特大
    val xxl = 24.dp   // 超大
    val xxxl = 32.dp  // 巨大

    // 卡片内边距
    val cardPadding = 14.dp
    // 列表项间距
    val listItemSpacing = 10.dp
    // 区段间距
    val sectionSpacing = 16.dp

    // 圆角
    val radiusSm = 6.dp
    val radiusMd = 8.dp
    val radiusLg = 12.dp
    val radiusXl = 16.dp
    val radiusFull = 100.dp // 胶囊

    // 按钮高度
    val buttonHeight = 44.dp
    // 输入框高度
    val inputHeight = 44.dp

    // 图标尺寸
    val iconSm = 16.dp
    val iconMd = 18.dp
    val iconLg = 24.dp
    val iconXl = 28.dp
}

/**
 * 统一字号系统
 */
object AppTextSize {
    val caption = 12.sp
    val tiny = 12.sp
    val small = 12.sp
    val body = 13.sp
    val medium = 14.sp
    val title = 15.sp
    val headline = 18.sp
    val display = 24.sp
}
