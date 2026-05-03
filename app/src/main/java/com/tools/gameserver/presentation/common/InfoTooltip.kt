package com.tools.gameserver.presentation.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.tools.gameserver.core.theme.AppColors

object InfoTooltipTexts {
    const val HEADERS = "请求头包含客户端和服务器之间的通信元数据，如认证信息、内容类型等。"
    const val BODY = "请求体是发送给服务器的数据内容，通常为 JSON 格式。不同接口有不同的数据结构要求。"
    const val PROTOCOL = "协议是一个完整的 HTTP 请求配置，包含 URL、方法、请求头、请求体等信息。导入后可直接发送。"
    const val URL = "请求的目标地址，包含协议（http/https）、域名、端口和路径。"
    const val METHOD = "HTTP 请求方法：GET（获取数据）、POST（提交数据）、PUT（更新数据）、DELETE（删除数据）等。"
    const val ITEM_FILE = "物品文件包含游戏道具代码，可批量选择并发送给服务器。"
    const val TIMEOUT = "请求超时时间，超过该时间未响应将自动取消请求。单位：毫秒。"
    const val BATCH_SEND = "批量发送会将选中的物品依次发送请求，中间有短暂间隔避免服务器压力过大。"
}

@Composable
fun InfoTooltip(
    title: String,
    explanation: String,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(RoundedCornerShape(50))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { isExpanded = !isExpanded }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = title,
                tint = AppColors.SystemBlue,
                modifier = Modifier.size(14.dp)
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Popup(
                alignment = Alignment.TopCenter,
                onDismissRequest = { isExpanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 260.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AppColors.BackgroundCard)
                        .padding(12.dp)
                ) {
                    Text(
                        explanation,
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}