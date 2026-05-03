package com.tools.gameserver.presentation.features.workspace.pages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.presentation.common.GlassCard
import com.tools.gameserver.presentation.features.workspace.WorkspaceViewModel

/**
 * 协议输入页
 */
@Composable
fun ProtocolInputPage(
    viewModel: WorkspaceViewModel,
    onParse: () -> Unit
) {
    val rawHeaders by viewModel.rawHeaders.collectAsState()
    val rawBody by viewModel.rawBody.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── 标题 + 返回 ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回",
                        tint = AppColors.SystemBlue
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "协议输入",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            }
        }

        // ── 请求头输入 ──
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "请求头 (Headers)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawHeaders,
                        onValueChange = viewModel::updateRawHeaders,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 260.dp),
                        placeholder = {
                            Text(
                                "GET /api/path HTTP/1.1\nHost: example.com\nContent-Type: application/json",
                                color = AppColors.TextHint,
                                fontSize = 13.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                }
            }
        }

        // ── 请求体输入 ──
        item {
            GlassCard {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "请求体 (Body)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = rawBody,
                        onValueChange = viewModel::updateRawBody,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 220.dp),
                        placeholder = {
                            Text(
                                "{\"key\": \"value\"} 或 param1=value1&param2=value2",
                                color = AppColors.TextHint,
                                fontSize = 13.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                }
            }
        }

        // ── 操作按钮 ──
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 粘贴
                FilledTonalButton(
                    onClick = {
                        clipboardManager.getText()?.text?.let { text ->
                            val parts = text.split("---BODY---", limit = 2)
                            viewModel.updateRawHeaders(parts[0].trim())
                            viewModel.updateRawBody(parts.getOrNull(1)?.trim() ?: "")
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("粘贴", fontSize = 13.sp)
                }
                // 清空
                FilledTonalButton(
                    onClick = {
                        viewModel.updateRawHeaders("")
                        viewModel.updateRawBody("")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("清空", fontSize = 13.sp)
                }
                // 自动修复
                FilledTonalButton(
                    onClick = {
                        val fixed = rawHeaders
                            .replace("\r\n", "\n")
                            .replace("\r", "\n")
                            .trim()
                        viewModel.updateRawHeaders(fixed)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("修复", fontSize = 13.sp)
                }
            }
        }

        // ── 解析按钮 ──
        item {
            Button(
                onClick = {
                    viewModel.parseProtocol()
                    onParse()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = rawHeaders.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.SystemBlue
                )
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("解析并发送", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
