package com.tools.gameserver.presentation.features.workspace.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LocalGameCard(
    entry: LocalGameEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {}
) {
    val protocolCount = entry.protocolFiles.size
    val itemCount = entry.itemFiles.size
    Row(
        modifier = Modifier.fillMaxWidth().combinedClickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick,
            onLongClick = onLongClick
        ).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(AppColors.SystemOrange.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Folder, contentDescription = null, tint = AppColors.SystemOrange, modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.gameName, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(entry.author, color = AppColors.TextSecondary, fontSize = 12.sp)
                if (protocolCount > 0) Text("· $protocolCount 协议", color = AppColors.TextSecondary, fontSize = 12.sp)
                if (itemCount > 0) Text("· $itemCount 物品", color = AppColors.TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.TextHint, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun ProtocolFileCard(file: File, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = AppColors.SeparatorLight.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.SystemBlue.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Code, contentDescription = null, tint = AppColors.SystemBlue, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.nameWithoutExtension, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val sizeText = if (file.length() < 1024) "${file.length()} B" else "${file.length() / 1024} KB"
                Text(sizeText, color = AppColors.TextHint, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.TextHint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun ItemFileCard(file: File, itemCount: Int, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Surface(onClick = onClick, shape = RoundedCornerShape(12.dp), color = AppColors.SeparatorLight.copy(alpha = 0.5f)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(AppColors.SystemGreen.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Inventory2, contentDescription = null, tint = AppColors.SystemGreen, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.nameWithoutExtension, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("$itemCount 个物品", color = AppColors.TextHint, fontSize = 11.sp)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.TextHint, modifier = Modifier.size(18.dp))
        }
    }
}