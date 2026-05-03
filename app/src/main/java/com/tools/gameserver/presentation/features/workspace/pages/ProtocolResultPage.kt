package com.tools.gameserver.presentation.features.workspace.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.util.SnackbarManager
import com.tools.gameserver.data.model.BodyType
import com.tools.gameserver.data.model.SendStatus
import com.tools.gameserver.presentation.common.GlassCard
import com.tools.gameserver.presentation.features.workspace.BatchSendResult
import com.tools.gameserver.presentation.features.workspace.WorkspaceViewModel

@Composable
fun ProtocolResultPage(viewModel: WorkspaceViewModel) {
    val parsedData by viewModel.parsedData.collectAsState()
    val sendStatus by viewModel.sendStatus.collectAsState()
    val proxyResponse by viewModel.proxyResponse.collectAsState()
    val isParsedExpanded by viewModel.isParsedExpanded.collectAsState()
    val isResponseExpanded by viewModel.isResponseExpanded.collectAsState()
    val editableParams by viewModel.editableParams.collectAsState()
    val itemsList by viewModel.itemsList.collectAsState()
    val itemsFileName by viewModel.itemsFileName.collectAsState()
    val isBatchSending by viewModel.isBatchSending.collectAsState()
    val batchDelayMs by viewModel.batchDelayMs.collectAsState()
    val loadedFromGameEntry by viewModel.loadedFromGameEntry.collectAsState()
    val sourceProtocolFile by viewModel.sourceProtocolFile.collectAsState()
    val batchProgress by viewModel.batchProgress.collectAsState()
    val batchTotal by viewModel.batchTotal.collectAsState()
    val batchTargetParam by viewModel.batchTargetParam.collectAsState()
    val batchResults by viewModel.batchResults.collectAsState()
    val rawBody by viewModel.rawBody.collectAsState()
    val availableParamNames by viewModel.availableParamNames.collectAsState()
    var showBatchConfirm by remember { mutableStateOf(false) }
    var showParamDropdown by remember { mutableStateOf(false) }
    // 用本地状态控制展开/折叠，避免污染 editableParams
    var headersExpanded by remember { mutableStateOf(true) }
    var responseExpanded by remember { mutableStateOf(true) }
    val selectedIndices by viewModel.selectedItemIndices.collectAsState()
    val parsed = parsedData ?: return
    val clipboardManager = LocalClipboardManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.navigateBack() }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回", tint = AppColors.SystemBlue)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("协议结果", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                }
            }
            item {
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(6.dp), color = AppColors.SystemBlue) {
                                Text(parsed.method, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(parsed.url, fontSize = 13.sp, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                GlassCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().clickable { headersExpanded = !headersExpanded }, verticalAlignment = Alignment.CenterVertically) {
                            Text("请求头", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(if (headersExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = AppColors.TextSecondary)
                        }
                        AnimatedVisibility(visible = headersExpanded) {
                            Column(modifier = Modifier.padding(top = 8.dp)) {
                                parsed.headers.forEach { (key, value) ->
                                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(key, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AppColors.SystemBlue, modifier = Modifier.widthIn(max = 140.dp).weight(0.35f, fill = false))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(value, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AppColors.TextSecondary, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // URL_ENCODED 类型用参数表格编辑（参数就是请求体）
            if (parsed.bodyType == BodyType.URL_ENCODED && editableParams.isNotEmpty()) {
                item {
                    GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("请求参数", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            editableParams.forEach { (key, value) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(key, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AppColors.SystemBlue, modifier = Modifier.widthIn(max = 120.dp).weight(0.3f, fill = false))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedTextField(
                                        value = value,
                                        onValueChange = { newValue -> viewModel.updateEditableParam(key, newValue) },
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 1.dp),
                                        textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace),
                                        singleLine = true,
                                        shape = RoundedCornerShape(8.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = AppColors.SystemBlue,
                                            unfocusedBorderColor = AppColors.SeparatorLight,
                                            cursorColor = AppColors.SystemBlue
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            // JSON/RAW 类型用文本框编辑请求体
            else if (parsed.bodyType != BodyType.NONE) {
                item {
                    GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("请求体", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            BasicTextField(
                                value = rawBody,
                                onValueChange = { viewModel.updateRawBody(it) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 200.dp).clip(RoundedCornerShape(8.dp)).background(AppColors.SeparatorLight).padding(12.dp),
                                textStyle = TextStyle(fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AppColors.TextPrimary),
                                cursorBrush = SolidColor(AppColors.SystemBlue)
                            )
                        }
                    }
                }
            }
            if (proxyResponse != null) {
                item {
                    GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().clickable { responseExpanded = !responseExpanded }, verticalAlignment = Alignment.CenterVertically) {
                                Text("响应", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Spacer(modifier = Modifier.weight(1f))
                                IconButton(onClick = { clipboardManager.setText(AnnotatedString(proxyResponse?.body ?: "")); SnackbarManager.show("已复制响应") }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "复制", tint = AppColors.TextSecondary, modifier = Modifier.size(16.dp))
                                }
                                Icon(if (responseExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null, tint = AppColors.TextSecondary)
                            }
                            AnimatedVisibility(visible = responseExpanded) {
                                Text(proxyResponse?.body ?: "", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = AppColors.TextSecondary, modifier = Modifier.padding(top = 8.dp), maxLines = 50)
                            }
                        }
                    }
                }
            }
            if (itemsList.isNotEmpty()) {
                item {
                    GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("批量发送: ${itemsFileName ?: "未命名"} (${itemsList.size} 个)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            // 替换参数下拉选择
                            Text("替换参数", fontSize = 12.sp, color = AppColors.TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.SeparatorLight),
                                    color = Color.Transparent,
                                    modifier = Modifier.fillMaxWidth().clickable { showParamDropdown = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (batchTargetParam.isBlank()) {
                                            Text("不替换（物品代码直接追加到请求体）", fontSize = 13.sp, color = AppColors.TextHint, modifier = Modifier.weight(1f))
                                        } else {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(batchTargetParam, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.SystemBlue, fontFamily = FontFamily.Monospace)
                                                val currentVal = editableParams[batchTargetParam] ?: ""
                                                if (currentVal.isNotBlank()) {
                                                    Text("当前值: $currentVal", fontSize = 11.sp, color = AppColors.TextHint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
                                        Icon(Icons.Default.ExpandMore, contentDescription = null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
                                    }
                                }
                                DropdownMenu(
                                    expanded = showParamDropdown,
                                    onDismissRequest = { showParamDropdown = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (batchTargetParam.isBlank()) {
                                                    Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.SystemBlue, modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                } else {
                                                    Spacer(modifier = Modifier.width(26.dp))
                                                }
                                                Text("不替换", fontSize = 14.sp)
                                            }
                                        },
                                        onClick = { viewModel.updateBatchTargetParam(""); showParamDropdown = false }
                                    )
                                    if (availableParamNames.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                    availableParamNames.forEach { name ->
                                        val isSelected = batchTargetParam == name
                                        val paramValue = editableParams[name] ?: ""
                                        DropdownMenuItem(
                                            text = {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    if (isSelected) {
                                                        Icon(Icons.Default.Check, contentDescription = null, tint = AppColors.SystemBlue, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                    } else {
                                                        Spacer(modifier = Modifier.width(26.dp))
                                                    }
                                                    Column {
                                                        Text(name, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) AppColors.SystemBlue else AppColors.TextPrimary)
                                                        if (paramValue.isNotBlank()) {
                                                            Text("= $paramValue", fontSize = 11.sp, color = AppColors.TextHint, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                        }
                                                    }
                                                }
                                            },
                                            onClick = { viewModel.updateBatchTargetParam(name); showParamDropdown = false }
                                        )
                                    }
                                }
                            }
                            if (batchTargetParam.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("💡 物品代码将替换「$batchTargetParam」的值发送", fontSize = 11.sp, color = AppColors.TextHint)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("间隔:", fontSize = 12.sp, color = AppColors.TextSecondary)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(50L, 100L, 200L, 500L, 1000L).forEach { ms ->
                                        FilterChip(selected = batchDelayMs == ms, onClick = { viewModel.updateBatchDelay(ms) }, label = { Text("${ms}ms", fontSize = 11.sp) })
                                    }
                                }
                            }
                        }
                    }
                }
                // ── 物品列表（带搜索 + 分页 + iOS 风格）──
                item {
                    GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("物品列表 (${itemsList.size} 个)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            var itemSearchQuery by remember { mutableStateOf("") }
                            var itemPage by remember { mutableIntStateOf(0) }
                            val pageSize = 30
                            val filteredItems = remember(itemsList, itemSearchQuery) {
                                if (itemSearchQuery.isBlank()) itemsList.withIndex().toList()
                                else itemsList.withIndex().filter { (_, pair) ->
                                    pair.first.contains(itemSearchQuery, ignoreCase = true) ||
                                    pair.second.contains(itemSearchQuery, ignoreCase = true)
                                }
                            }
                            val totalFilteredPages = maxOf(1, (filteredItems.size + pageSize - 1) / pageSize)
                            val safePage = itemPage.coerceIn(0, totalFilteredPages - 1)
                            if (safePage != itemPage) itemPage = safePage
                            val pageItems = filteredItems.drop(safePage * pageSize).take(pageSize)

                            OutlinedTextField(
                                value = itemSearchQuery,
                                onValueChange = { itemSearchQuery = it; itemPage = 0 },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("搜索物品代码或名称...", fontSize = 13.sp, color = AppColors.TextHint) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = AppColors.TextHint, modifier = Modifier.size(20.dp)) },
                                trailingIcon = {
                                    if (itemSearchQuery.isNotEmpty()) {
                                        IconButton(onClick = { itemSearchQuery = ""; itemPage = 0 }, modifier = Modifier.size(18.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "清空", tint = AppColors.TextHint, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                textStyle = TextStyle(fontSize = 14.sp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AppColors.SystemBlue,
                                    unfocusedBorderColor = AppColors.SeparatorLight,
                                    cursorColor = AppColors.SystemBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                val allFilteredSelected = filteredItems.isNotEmpty() && filteredItems.all { (idx, _) -> idx in selectedIndices }
                                Icon(
                                    if (allFilteredSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (allFilteredSelected) AppColors.SystemBlue else AppColors.TextHint,
                                    modifier = Modifier.size(20.dp).clickable {
                                        if (allFilteredSelected) {
                                            viewModel.clearSelection(filteredItems.map { it.index }.toSet())
                                        } else {
                                            viewModel.selectAllFiltered(filteredItems.map { it.index })
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("全选", fontSize = 12.sp, color = AppColors.TextSecondary)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("已选 ${selectedIndices.size} / ${itemsList.size}", fontSize = 12.sp, color = AppColors.TextSecondary)
                                if (itemSearchQuery.isNotBlank()) {
                                    Text("  筛选 ${filteredItems.size} 个", fontSize = 11.sp, color = AppColors.TextHint)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (filteredItems.isEmpty()) {
                                Text("无匹配物品", fontSize = 13.sp, color = AppColors.TextHint, modifier = Modifier.padding(vertical = 12.dp))
                            } else {
                                pageItems.forEachIndexed { pageIdx, (index, pair) ->
                                    val (code, name) = pair
                                    val isSelected = index in selectedIndices
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .background(if (isSelected) AppColors.SystemBlue.copy(alpha = 0.06f) else Color.Transparent)
                                            .clickable { viewModel.toggleItem(index) }
                                            .padding(vertical = 8.dp, horizontal = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                            contentDescription = null,
                                            tint = if (isSelected) AppColors.SystemBlue else AppColors.TextHint,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AppColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text("code: $code", fontSize = 11.sp, color = AppColors.TextHint, fontFamily = FontFamily.Monospace, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Text("#${index + 1}", fontSize = 11.sp, color = AppColors.TextDisabled, modifier = Modifier.padding(start = 4.dp))
                                    }
                                    if (pageIdx < pageItems.size - 1) {
                                        HorizontalDivider(color = AppColors.SeparatorLight, modifier = Modifier.padding(start = 36.dp))
                                    }
                                }
                            }
                            if (totalFilteredPages > 1) {
                                Spacer(modifier = Modifier.height(4.dp))
                                HorizontalDivider(color = AppColors.SeparatorLight)
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { itemPage-- }, enabled = safePage > 0, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "上一页", tint = if (safePage > 0) AppColors.SystemBlue else AppColors.TextDisabled)
                                    }
                                    Text("${safePage + 1} / $totalFilteredPages", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = AppColors.TextSecondary, modifier = Modifier.padding(horizontal = 12.dp))
                                    IconButton(onClick = { itemPage++ }, enabled = safePage < totalFilteredPages - 1, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "下一页", tint = if (safePage < totalFilteredPages - 1) AppColors.SystemBlue else AppColors.TextDisabled)
                                    }
                                }
                            }
                        }
                    }
                }
                if (isBatchSending) {
                    item {
                        GlassCard {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("发送进度", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(progress = { if (batchTotal > 0) batchProgress.toFloat() / batchTotal else 0f }, modifier = Modifier.fillMaxWidth())
                                Text("$batchProgress / $batchTotal", fontSize = 12.sp, color = AppColors.TextSecondary)
                            }
                        }
                    }
                }
                if (batchResults.isNotEmpty()) {
                    item {
                        GlassCard {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("发送结果 (${batchResults.size})", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.TextPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState())
                            ) {
                                batchResults.forEachIndexed { index, result ->
                                    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp)).background(if (result.success) AppColors.SuccessLight else AppColors.ErrorLight).padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(result.itemCode, fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = AppColors.TextPrimary, modifier = Modifier.width(80.dp))
                                        Text(result.itemName, fontSize = 12.sp, color = AppColors.TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                                        Text(if (result.success) "成功" else result.message, fontSize = 11.sp, color = if (result.success) AppColors.SystemGreen else AppColors.Error)
                                    }
                                    if (index < batchResults.size - 1) Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }
            }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().background(AppColors.BackgroundCard).padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.sendRequest() }, modifier = Modifier.weight(1f), enabled = sendStatus !is SendStatus.Loading && !isBatchSending, colors = ButtonDefaults.buttonColors(containerColor = AppColors.SystemBlue)) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("发送", fontSize = 13.sp)
            }
            if (itemsList.isNotEmpty()) {
                if (isBatchSending) {
                    Button(onClick = { viewModel.cancelBatchSend() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error)) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("停止", fontSize = 13.sp)
                    }
                } else {
                    val selCount = selectedIndices.size
                    Button(onClick = { if (selCount > 0) showBatchConfirm = true }, modifier = Modifier.weight(1f), enabled = selCount > 0, colors = ButtonDefaults.buttonColors(containerColor = AppColors.SystemGreen)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("批量 ($selCount)", fontSize = 13.sp)
                    }
                }
            }
        }
    }
    if (showBatchConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchConfirm = false },
            title = { Text("确认批量发送", fontWeight = FontWeight.SemiBold) },
            text = { Text("将向服务器发送 ${selectedIndices.size} 个已选物品，间隔 ${batchDelayMs}ms") },
            confirmButton = { TextButton(onClick = { showBatchConfirm = false; viewModel.startBatchSend(selectedIndices) }) { Text("发送") } },
            dismissButton = { TextButton(onClick = { showBatchConfirm = false }) { Text("取消") } }
        )
    }
}