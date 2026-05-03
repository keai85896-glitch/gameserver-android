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
import androidx.compose.material.icons.filled.PlayArrow
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
                            Box {
                                OutlinedTextField(
                                    value = if (batchTargetParam.isBlank()) "不替换" else batchTargetParam,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    label = { Text("替换参数名", fontSize = 12.sp) },
                                    placeholder = { Text("物品代码将替换此参数", fontSize = 12.sp) },
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    trailingIcon = {
                                        IconButton(onClick = { showParamDropdown = !showParamDropdown }) {
                                            Icon(Icons.Default.ExpandMore, contentDescription = null, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                )
                                DropdownMenu(expanded = showParamDropdown, onDismissRequest = { showParamDropdown = false }) {
                                    DropdownMenuItem(text = { Text("不替换") }, onClick = { viewModel.updateBatchTargetParam(""); showParamDropdown = false })
                                    availableParamNames.forEach { name ->
                                        DropdownMenuItem(text = { Text(name) }, onClick = { viewModel.updateBatchTargetParam(name); showParamDropdown = false })
                                    }
                                }
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
                            if (isBatchSending) {
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
                    Button(onClick = { showBatchConfirm = true }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = AppColors.SystemGreen)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("批量 (${itemsList.size})", fontSize = 13.sp)
                    }
                }
            }
        }
    }
    if (showBatchConfirm) {
        AlertDialog(
            onDismissRequest = { showBatchConfirm = false },
            title = { Text("确认批量发送", fontWeight = FontWeight.SemiBold) },
            text = { Text("将向服务器发送 ${itemsList.size} 个请求，间隔 ${batchDelayMs}ms") },
            confirmButton = { TextButton(onClick = { showBatchConfirm = false; viewModel.startBatchSend(viewModel.selectedItemIndices.value.ifEmpty { itemsList.indices.toSet() }) }) { Text("发送") } },
            dismissButton = { TextButton(onClick = { showBatchConfirm = false }) { Text("取消") } }
        )
    }
}