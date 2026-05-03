package com.tools.gameserver.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.tools.gameserver.core.theme.AppColors

@Composable
fun SkeletonCardList(count: Int = 3) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AppColors.SeparatorLight.copy(alpha = 0.3f))
            )
        }
    }
}

@Composable
fun SearchEmptyState(query: String) {
    EmptyState(
        title = "未找到结果",
        subtitle = "没有找到与「$query」相关的内容"
    )
}

@Composable
fun ListEmptyState(title: String = "暂无数据", subtitle: String = "") {
    EmptyState(title = title, subtitle = subtitle)
}