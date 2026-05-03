package com.tools.gameserver.presentation.features.workspace.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.presentation.common.PrimaryButton
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import java.io.File

/** iOS 风格物品文件页 */
@Composable
fun ItemFilePage(
    file: File,
    items: List<Pair<String, String>>,
    ownerGameEntry: LocalGameEntry,
    selectedIndices: Set<Int>,
    searchQuery: String,
    currentPage: Int,
    pageSize: Int,
    onToggleSelection: (Int) -> Unit,
    onSelectAll: (Set<Int>) -> Unit,
    onDeselectAll: (Set<Int>) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onPageChange: (Int) -> Unit,
    onSaveSelected: (String, List<Pair<String, String>>) -> Unit,
    onConfirmSelection: () -> Unit,
    onBack: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    val filteredItemsWithIndex: List<Triple<Int, String, String>> = if (searchQuery.isBlank()) {
        items.mapIndexed { i, (code, name) -> Triple(i, code, name) }
    } else {
        items.mapIndexedNotNull { i, (code, name) ->
            if (name.contains(searchQuery, true) || code.contains(searchQuery, true)) Triple(i, code, name) else null
        }
    }
    val totalPages = maxOf(1, (filteredItemsWithIndex.size + pageSize - 1) / pageSize)
    val safePage = currentPage.coerceIn(0, totalPages - 1)
    val pageItems = filteredItemsWithIndex.drop(safePage * pageSize).take(pageSize)
    val filteredIndices = filteredItemsWithIndex.map { it.first }.toSet()

    Column(modifier = Modifier.fillMaxSize()) {
        // iOS 导航栏
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = AppColors.TextPrimary) }
            Text(file.name, color = AppColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 4.dp))
            val allFilteredSelected = filteredIndices.isNotEmpty() && filteredIndices.all { it in selectedIndices }
            TextButton(onClick = {
                if (allFilteredSelected) onDeselectAll(filteredIndices) else onSelectAll(filteredIndices)
            }) { Text(if (allFilteredSelected) "取消全选" else "全选", color = AppColors.SystemBlue, fontSize = 14.sp) }
        }

        // 搜索栏
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("搜索物品...", color = AppColors.TextHint) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = AppColors.TextHint, modifier = Modifier.size(20.dp)) },
            trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchQueryChange("") }) { Icon(Icons.Default.Close, "清除", tint = AppColors.TextHint, modifier = Modifier.size(18.dp)) } },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.SystemBlue, unfocusedBorderColor = AppColors.SeparatorLight, focusedTextColor = AppColors.TextPrimary, unfocusedTextColor = AppColors.TextPrimary, cursorColor = AppColors.SystemBlue)
        )

        // 物品列表
        LazyColumn(modifier = Modifier.weight(1f)) {
            item { Spacer(Modifier.height(4.dp)) }
            itemsIndexed(pageItems, key = { _, (idx, code, _) -> "$idx-$code" }) { _, (originalIdx, code, name) ->
                val isSelected = originalIdx in selectedIndices
                Row(
                    Modifier.fillMaxWidth()
                        .background(if (isSelected) AppColors.SystemBlue.copy(alpha = 0.06f) else Color.Transparent)
                        .clickable { onToggleSelection(originalIdx) }
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        null,
                        tint = if (isSelected) AppColors.SystemBlue else AppColors.TextHint,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(name, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("code: $code", color = AppColors.TextHint, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                HorizontalDivider(color = AppColors.SeparatorLight, modifier = Modifier.padding(start = 48.dp))
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        // 底部栏：分页 + 保存文件 + 确认选择
        Row(
            Modifier.fillMaxWidth().background(AppColors.Surface).padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { if (safePage > 0) onPageChange(safePage - 1) }, enabled = safePage > 0) { Text("<", color = if (safePage > 0) AppColors.SystemBlue else AppColors.TextHint) }
                Text("${safePage + 1}/$totalPages", color = AppColors.TextSecondary, fontSize = 13.sp)
                TextButton(onClick = { if (safePage < totalPages - 1) onPageChange(safePage + 1) }, enabled = safePage < totalPages - 1) { Text(">", color = if (safePage < totalPages - 1) AppColors.SystemBlue else AppColors.TextHint) }
            }
            Spacer(Modifier.weight(1f))
            // 保存文件按钮
            TextButton(
                onClick = { showSaveDialog = true },
                enabled = selectedIndices.isNotEmpty()
            ) {
                Icon(Icons.Default.Save, null, tint = if (selectedIndices.isNotEmpty()) AppColors.SystemGreen else AppColors.TextDisabled, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("保存文件", fontSize = 13.sp, color = if (selectedIndices.isNotEmpty()) AppColors.SystemGreen else AppColors.TextDisabled)
            }
            // 确认选择按钮
            PrimaryButton(
                text = "确认 (${selectedIndices.size})",
                onClick = onConfirmSelection,
                modifier = Modifier.height(40.dp),
                enabled = selectedIndices.isNotEmpty()
            )
        }
    }

    // ── 保存文件弹窗 ──
    if (showSaveDialog) {
        var fileName by remember { mutableStateOf("") }
        val selectedItems = remember(selectedIndices) {
            selectedIndices.sorted().mapNotNull { idx -> items.getOrNull(idx) }
        }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("保存已选物品", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("已选中 ${selectedItems.size} 个物品", fontSize = 13.sp, color = AppColors.TextSecondary)
                    Text("保存到: ${ownerGameEntry.dir.name}/", fontSize = 12.sp, color = AppColors.TextHint)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = fileName,
                        onValueChange = { fileName = it },
                        label = { Text("文件名", fontSize = 12.sp) },
                        placeholder = { Text("输入保存的文件名", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.SystemBlue, cursorColor = AppColors.SystemBlue)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (fileName.isNotBlank()) {
                            onSaveSelected(fileName, selectedItems)
                            showSaveDialog = false
                        }
                    },
                    enabled = fileName.isNotBlank()
                ) { Text("保存") }
            },
            dismissButton = { TextButton(onClick = { showSaveDialog = false }) { Text("取消") } }
        )
    }
}
