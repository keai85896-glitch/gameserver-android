package com.tools.gameserver.presentation.features.workspace.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.presentation.features.workspace.components.ItemFileCard
import com.tools.gameserver.presentation.features.workspace.components.ProtocolFileCard
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import com.tools.gameserver.presentation.features.workspace.parseItemFile
import java.io.File

@Composable
fun GameDetailPage(
    entry: LocalGameEntry,
    onProtocolFileClick: (File) -> Unit,
    onItemFileClick: (File, List<Pair<String, String>>, LocalGameEntry) -> Unit,
    onItemWithProtocol: (File, List<Pair<String, String>>, LocalGameEntry, File) -> Unit,
    onFileLongPress: (File) -> Unit,
    onRefreshEntries: () -> Unit,
    onBack: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showNewProtocolDialog by remember { mutableStateOf(false) }
    var showProtocolPicker by remember { mutableStateOf(false) }
    var pendingItemFile by remember { mutableStateOf<File?>(null) }
    var pendingItemPairs by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // ── 导航栏 ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = AppColors.SystemBlue)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.gameName, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text(entry.author, fontSize = 13.sp, color = AppColors.TextSecondary)
                }
            }
        }

        // ── 协议文件 ──
        item {
            Text("协议文件", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
        if (entry.protocolFiles.isEmpty()) {
            item {
                Text("暂无协议文件", fontSize = 13.sp, color = AppColors.TextHint, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(entry.protocolFiles, key = { it.absolutePath }) { file ->
                ProtocolFileCard(
                    file = file,
                    onClick = { onProtocolFileClick(file) },
                    onLongClick = { onFileLongPress(file) }
                )
            }
        }

        // ── 物品文件 ──
        item {
            Text("物品文件", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
        if (entry.itemFiles.isEmpty()) {
            item {
                Text("暂无物品文件", fontSize = 13.sp, color = AppColors.TextHint, modifier = Modifier.padding(vertical = 16.dp))
            }
        } else {
            items(entry.itemFiles, key = { it.absolutePath }) { file ->
                val items = remember(file) { parseItemFile(file) }
                ItemFileCard(
                    file = file,
                    itemCount = items.size,
                    onClick = {
                        if (entry.protocolFiles.size == 1) {
                            // 只有一个协议，直接绑定进入
                            onItemWithProtocol(file, items, entry, entry.protocolFiles.first())
                        } else if (entry.protocolFiles.size > 1) {
                            // 多个协议，弹选择框
                            pendingItemFile = file
                            pendingItemPairs = items
                            showProtocolPicker = true
                        } else {
                            // 没有协议，走原逻辑
                            onItemFileClick(file, items, entry)
                        }
                    },
                    onLongClick = { onFileLongPress(file) }
                )
            }
        }
    }

    // ── 协议选择弹窗 ──
    if (showProtocolPicker && pendingItemFile != null) {
        AlertDialog(
            onDismissRequest = { showProtocolPicker = false },
            title = { Text("选择协议文件", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    // 协议文件列表
                    entry.protocolFiles.forEach { protoFile ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showProtocolPicker = false
                                    onItemWithProtocol(pendingItemFile!!, pendingItemPairs, entry, protoFile)
                                }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Description, null, tint = AppColors.SystemBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(protoFile.nameWithoutExtension, color = AppColors.TextPrimary, fontSize = 15.sp)
                        }
                    }
                    // 分隔线
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = AppColors.Divider
                    )
                    // 编辑物品文件按钮
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                            .clickable {
                                showProtocolPicker = false
                                onItemFileClick(pendingItemFile!!, pendingItemPairs, entry)
                            }
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, null, tint = AppColors.SystemOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("编辑物品文件", color = AppColors.TextPrimary, fontSize = 15.sp)
                            Text("查看、筛选并保存为新文件", color = AppColors.TextHint, fontSize = 12.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showProtocolPicker = false }) { Text("取消") } }
        )
    }

    // ── 新建协议文件弹窗 ──
    if (showNewProtocolDialog) {
        var protoName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProtocolDialog = false },
            title = { Text("新建协议文件") },
            text = {
                OutlinedTextField(
                    value = protoName,
                    onValueChange = { protoName = it },
                    label = { Text("文件名") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (protoName.isNotBlank()) {
                        val newFile = File(entry.dir, "$protoName.protocol")
                        newFile.createNewFile()
                        showNewProtocolDialog = false
                        onRefreshEntries()
                    }
                }) { Text("创建") }
            },
            dismissButton = { TextButton(onClick = { showNewProtocolDialog = false }) { Text("取消") } }
        )
    }
}