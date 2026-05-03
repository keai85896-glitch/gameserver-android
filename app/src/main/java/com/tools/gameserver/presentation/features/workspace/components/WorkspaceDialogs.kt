package com.tools.gameserver.presentation.features.workspace.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import com.tools.gameserver.presentation.features.workspace.model.WorkspaceDialog
import com.tools.gameserver.presentation.features.workspace.WorkspaceViewModel

@Composable
fun WorkspaceDialogs(
    currentDialog: WorkspaceDialog,
    viewModel: WorkspaceViewModel,
    onAddItemFileConfirm: ((LocalGameEntry) -> Unit)? = null
) {
    val context = LocalContext.current

    when (currentDialog) {
        is WorkspaceDialog.None -> { /* 不显示 */ }

        is WorkspaceDialog.DeleteFile -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("删除文件", fontWeight = FontWeight.SemiBold) },
                text = { Text("确定删除 \"${currentDialog.file.name}\" 吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteFile(currentDialog.file)
                        viewModel.dismissDialog()
                    }) { Text("删除", color = AppColors.Error) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.DeleteGame -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("删除游戏目录", fontWeight = FontWeight.SemiBold) },
                text = { Text("确定删除整个游戏目录 \"${currentDialog.entry.gameName}\" 吗？目录内所有协议和物品文件都将被删除。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteGameDir(currentDialog.entry)
                        viewModel.dismissDialog()
                    }) { Text("删除", color = AppColors.Error) }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.CreateProtocol -> {
            var author by remember { mutableStateOf("") }
            var gameName by remember { mutableStateOf("") }
            var protoName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("新建协议文件", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            label = { Text("作者名", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = gameName,
                            onValueChange = { gameName = it },
                            label = { Text("游戏名", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = protoName,
                            onValueChange = { protoName = it },
                            label = { Text("协议名称", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (author.isNotBlank() && gameName.isNotBlank() && protoName.isNotBlank()) {
                                viewModel.createProtocolFile(author, gameName, protoName)
                                viewModel.dismissDialog()
                            }
                        },
                        enabled = author.isNotBlank() && gameName.isNotBlank() && protoName.isNotBlank()
                    ) { Text("创建") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.StoragePermission -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("需要存储权限", fontWeight = FontWeight.SemiBold) },
                text = { Text("为了访问本地游戏目录文件，需要授予\"所有文件访问\"权限。请在系统设置中开启。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.openStorageSettings(context)
                        viewModel.dismissDialog()
                    }) { Text("去设置") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.AddItemFile -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("添加物品文件", fontWeight = FontWeight.SemiBold) },
                text = { Text("将物品文件复制到游戏目录 \"${currentDialog.entry.gameName}\" 中。") },
                confirmButton = {
                    TextButton(onClick = {
                        onAddItemFileConfirm?.invoke(currentDialog.entry)
                        viewModel.dismissDialog()
                    }) { Text("选择文件") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.UploadProtocol -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("上传协议到社区", fontWeight = FontWeight.SemiBold) },
                text = { Text("将当前协议和物品文件上传到社区分享。") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.openUploadDialog(currentDialog.entry)
                        viewModel.dismissDialog()
                    }) { Text("上传") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.Help -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("使用说明", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📁 目录结构", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.SystemBlue)
                        Text("将协议文件(.protocol)和物品文件(.txt)放在同一游戏目录下，应用会自动识别。", fontSize = 13.sp, lineHeight = 18.sp)
                        HorizontalDivider(color = AppColors.SeparatorLight, modifier = Modifier.padding(vertical = 2.dp))
                        Text("📝 协议文件", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.SystemBlue)
                        Text("• 点击协议文件可查看和编辑 HTTP 请求\n• 支持 GET/POST 等方法\n• 请求头和请求体用 ---BODY--- 分隔\n• 支持 URL_ENCODED、JSON、RAW 格式", fontSize = 13.sp, lineHeight = 18.sp)
                        HorizontalDivider(color = AppColors.SeparatorLight, modifier = Modifier.padding(vertical = 2.dp))
                        Text("📦 物品文件", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.SystemBlue)
                        Text("• 点击物品文件后选择一个协议进行批量发送\n• 支持搜索和全选/部分选择\n• 物品代码将自动替换协议中的指定参数", fontSize = 13.sp, lineHeight = 18.sp)
                        HorizontalDivider(color = AppColors.SeparatorLight, modifier = Modifier.padding(vertical = 2.dp))
                        Text("💡 快捷操作", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AppColors.SystemBlue)
                        Text("• 长按游戏卡片：弹出管理菜单\n• 长按文件卡片：可删除文件\n• 右下角按钮：刷新列表\n• 下拉刷新：同步最新文件", fontSize = 13.sp, lineHeight = 18.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("知道了") }
                }
            )
        }

        is WorkspaceDialog.GameLongPressMenu -> {
            val entry = currentDialog.entry
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text(entry.gameName, fontWeight = FontWeight.SemiBold) },
                text = {
                    Column {
                        // 添加物品文件
                        Surface(
                            onClick = {
                                viewModel.dismissDialog()
                                viewModel.showDialog(WorkspaceDialog.AddItemFile(entry))
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, null, tint = AppColors.SystemBlue, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("添加物品文件", color = AppColors.TextPrimary, fontSize = 15.sp)
                            }
                        }
                        // 上传协议
                        if (entry.protocolFiles.isNotEmpty()) {
                            Surface(
                                onClick = {
                                    viewModel.dismissDialog()
                                    viewModel.openUploadDialog(entry)
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Transparent,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Upload, null, tint = AppColors.SystemGreen, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("上传协议到社区", color = AppColors.TextPrimary, fontSize = 15.sp)
                                }
                            }
                        }
                        // 删除游戏目录
                        Surface(
                            onClick = {
                                viewModel.dismissDialog()
                                viewModel.showDialog(WorkspaceDialog.DeleteGame(entry))
                            },
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Transparent,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Delete, null, tint = AppColors.Error, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("删除游戏目录", color = AppColors.Error, fontSize = 15.sp)
                            }
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }

        is WorkspaceDialog.SaveItems -> {
            var fileName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { viewModel.dismissDialog() },
                title = { Text("保存选中物品", fontWeight = FontWeight.SemiBold) },
                text = {
                    Column {
                        Text("已选中 ${currentDialog.snapshot.size} 个物品", fontSize = 13.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text("文件名", fontSize = 12.sp) },
                            placeholder = { Text("输入保存的文件名", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(fontSize = 13.sp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (fileName.isNotBlank()) {
                                viewModel.saveItemsToFile(currentDialog.snapshot, fileName)
                                viewModel.dismissDialog()
                            }
                        },
                        enabled = fileName.isNotBlank()
                    ) { Text("保存") }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissDialog() }) { Text("取消") }
                }
            )
        }
    }
}
