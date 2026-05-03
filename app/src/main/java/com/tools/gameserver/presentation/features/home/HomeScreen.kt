package com.tools.gameserver.presentation.features.home

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.constants.AppConstants
import com.tools.gameserver.core.theme.Animations
import com.tools.gameserver.core.util.SnackbarManager
import com.tools.gameserver.data.service.api.UserInfo
import com.tools.gameserver.presentation.common.GlassCard
import com.tools.gameserver.presentation.common.PrimaryButton
import com.tools.gameserver.service.SystemFloatingWindowService

@Composable
fun HomeScreen(
    userInfo: UserInfo?,
    onOpenWorkspace: () -> Unit,
    onOpenCommunity: () -> Unit,
    onOpenProfile: () -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionGuide by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // ===== 帮助弹窗 =====
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text("使用帮助", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HelpRow(
                        icon = Icons.Default.HomeRepairService,
                        iconColor = AppColors.SystemBlue,
                        title = "工作台",
                        desc = "管理本地协议和物品文件，选择后直接发送"
                    )
                    HelpRow(
                        icon = Icons.Default.Layers,
                        iconColor = AppColors.SystemTeal,
                        title = "悬浮窗",
                        desc = "启动后可在任意应用上层显示，支持横竖屏自适应"
                    )
                    HelpRow(
                        icon = Icons.Default.Explore,
                        iconColor = AppColors.SystemGreen,
                        title = "社区",
                        desc = "浏览和下载其他玩家分享的协议与物品文件"
                    )
                    HorizontalDivider(color = AppColors.SeparatorLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                    HelpRow(
                        icon = Icons.Default.TouchApp,
                        iconColor = AppColors.SystemOrange,
                        title = "长按游戏卡片",
                        desc = "可上传到社区或删除本地目录"
                    )
                    HelpRow(
                        icon = Icons.Default.SelectAll,
                        iconColor = AppColors.SystemPurple,
                        title = "批量选择物品",
                        desc = "勾选多个物品后可一键批量发送，支持设置发送间隔"
                    )
                    HelpRow(
                        icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                        iconColor = AppColors.SystemRed,
                        title = "协议文件格式",
                        desc = "路径：/storage/emulated/0/游戏私服物品/{作者}/{游戏名}/"
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text("知道了", color = AppColors.SystemBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = AppColors.BackgroundCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    if (showPermissionGuide) {
        AlertDialog(
            onDismissRequest = { showPermissionGuide = false },
            title = {
                Text("开启悬浮窗权限", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("悬浮窗需要「显示在其他应用上层」权限才能工作。", color = AppColors.TextSecondary, fontSize = 14.sp)
                    Text("请按以下步骤操作：", color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    HelpRow(text = "1. 点击下方按钮跳转到系统设置")
                    HelpRow(text = "2. 找到「显示在其他应用的上层」或「悬浮窗」")
                    HelpRow(text = "3. 打开本应用的开关")
                    HelpRow(text = "4. 返回本页，再次点击「系统悬浮窗」即可启动")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showPermissionGuide = false
                    try {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        SnackbarManager.show("无法打开设置，请手动授予悬浮窗权限")
                    }
                }) {
                    Text("前往设置", color = AppColors.SystemBlue, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionGuide = false }) {
                    Text("取消", color = AppColors.TextHint)
                }
            },
            containerColor = AppColors.BackgroundCard,
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // ===== Hero 区域：渐变背景 + 大标题 + 用户卡片 =====
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                AppColors.GradientStart,
                                AppColors.BackgroundPrimary
                            )
                        )
                    )
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp, bottom = 20.dp)
            ) {
                Column {
                    Text(
                        text = "首页",
                        color = AppColors.TextPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.41).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (userInfo != null) "欢迎回来，${userInfo.username}" else "游戏私服物品管理工具",
                        color = AppColors.TextSecondary,
                        fontSize = 15.sp
                    )
                }
            }
        }

        // ===== 用户状态卡片 =====
        item {
            UserStatusCard(
                userInfo = userInfo,
                onOpenWorkspace = onOpenWorkspace,
                onLoginClick = onLoginClick
            )
        }

        // ===== 快捷入口网格 =====
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp)
            ) {
                Text(
                    text = "快捷操作",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridQuickEntry(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Layers,
                        iconColor = AppColors.SystemTeal,
                        title = "悬浮窗",
                        subtitle = "跨应用显示",
                        onClick = {
                            if (!Settings.canDrawOverlays(context)) {
                                showPermissionGuide = true
                            } else {
                                try {
                                    SystemFloatingWindowService.start(context)
                                    SnackbarManager.show("悬浮窗已启动")
                                } catch (e: Exception) {
                                    SnackbarManager.show("启动失败: ${e.message}")
                                }
                            }
                        }
                    )
                    GridQuickEntry(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Forum,
                        iconColor = AppColors.SystemYellow,
                        title = "QQ 群",
                        subtitle = AppConstants.QQ_GROUP_NUM,
                        onClick = {
                            try {
                                val qqIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.QQ_GROUP_URL))
                                qqIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startActivity(qqIntent)
                            } catch (_: Exception) {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("QQ群号", AppConstants.QQ_GROUP_NUM))
                                SnackbarManager.show("群号已复制：${AppConstants.QQ_GROUP_NUM}")
                            }
                        }
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GridQuickEntry(
                        modifier = Modifier.weight(1f),
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        iconColor = AppColors.SystemPurple,
                        title = "使用帮助",
                        subtitle = "快速入门",
                        onClick = { showHelpDialog = true }
                    )
                    if (userInfo == null) {
                        GridQuickEntry(
                            modifier = Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.Login,
                            iconColor = AppColors.SystemOrange,
                            title = "登录",
                            subtitle = "解锁全部功能",
                            onClick = onLoginClick
                        )
                    } else {
                        GridQuickEntry(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.HomeRepairService,
                            iconColor = AppColors.SystemBlue,
                            title = "工作台",
                            subtitle = "本地资源",
                            onClick = onOpenWorkspace
                        )
                    }
                }
            }
        }

        // ===== 更多功能 =====
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp)
            ) {
                Text(
                    text = "更多",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        QuickEntryItem(
                            icon = Icons.Default.Forum,
                            iconColor = AppColors.SystemYellow,
                            title = "官方 QQ 群",
                            subtitle = "群号 ${AppConstants.QQ_GROUP_NUM}，点击加入",
                            onClick = {
                                try {
                                    val qqIntent = Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.QQ_GROUP_URL))
                                    qqIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(qqIntent)
                                } catch (_: Exception) {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    clipboard.setPrimaryClip(android.content.ClipData.newPlainText("QQ群号", AppConstants.QQ_GROUP_NUM))
                                    SnackbarManager.show("群号已复制：${AppConstants.QQ_GROUP_NUM}，请手动搜索加入")
                                }
                            }
                        )
                        if (userInfo == null) {
                            QuickEntryDivider()
                            QuickEntryItem(
                                icon = Icons.AutoMirrored.Filled.Login,
                                iconColor = AppColors.SystemOrange,
                                title = "登录",
                                subtitle = "登录后可上传社区资源",
                                onClick = onLoginClick
                            )
                        }
                    }
                }
            }
        }

        // ===== 使用说明 =====
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 28.dp, bottom = 32.dp)
            ) {
                Text(
                    text = "使用说明",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(4.dp)) {
                        HelpRow(
                            icon = Icons.Default.HomeRepairService,
                            iconColor = AppColors.SystemBlue,
                            title = "工作台",
                            desc = "管理本地协议和物品文件，选择后直接发送"
                        )
                        HelpRow(
                            icon = Icons.Default.Layers,
                            iconColor = AppColors.SystemTeal,
                            title = "悬浮窗",
                            desc = "启动后可在任意应用上层显示，支持横竖屏自适应"
                        )
                        HelpRow(
                            icon = Icons.Default.Explore,
                            iconColor = AppColors.SystemGreen,
                            title = "社区",
                            desc = "浏览和下载其他玩家分享的协议与物品文件"
                        )
                        HorizontalDivider(color = AppColors.SeparatorLight, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 2.dp))
                        HelpRow(
                            icon = Icons.Default.TouchApp,
                            iconColor = AppColors.SystemOrange,
                            title = "长按游戏卡片",
                            desc = "可上传到社区或删除本地目录"
                        )
                        HelpRow(
                            icon = Icons.Default.SelectAll,
                            iconColor = AppColors.SystemPurple,
                            title = "批量选择物品",
                            desc = "勾选多个物品后可一键批量发送，支持设置发送间隔"
                        )
                        HelpRow(
                            icon = Icons.AutoMirrored.Filled.InsertDriveFile,
                            iconColor = AppColors.SystemRed,
                            title = "协议文件格式",
                            desc = "路径：/storage/emulated/0/游戏私服物品/{作者}/{游戏名}/"
                        )
                    }
                }
            }
        }
    }
}

// ==================== 网格快捷入口卡片 ====================

@Composable
private fun GridQuickEntry(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "grid_entry_scale"
    )

    GlassCard(
        modifier = modifier
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(10.dp),
                        ambientColor = iconColor.copy(alpha = 0.3f),
                        spotColor = iconColor.copy(alpha = 0.3f)
                    )
                    .background(
                        color = iconColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = AppColors.TextHint,
                fontSize = 12.sp
            )
        }
    }
}

// ==================== 帮助行组件 ====================

@Composable
private fun HelpRow(
    icon: ImageVector?,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        } else {
            Spacer(modifier = Modifier.width(28.dp))
        }
        Column {
            Text(
                text = title,
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = desc,
                color = AppColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

/** 简单文本行 — 用于权限引导弹窗中的步骤说明 */
@Composable
private fun HelpRow(text: String) {
    Text(
        text = text,
        color = AppColors.TextSecondary,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}

// ==================== 用户状态卡片 ====================

@Composable
private fun UserStatusCard(
    userInfo: UserInfo?,
    onOpenWorkspace: () -> Unit,
    onLoginClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (userInfo != null) "欢迎回来" else "开始使用",
                    color = AppColors.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (userInfo != null) {
                        val uploadText = if (userInfo.uploadAuthorized || userInfo.role == "admin") "已授权上传" else "只读浏览"
                        "${userInfo.username} · $uploadText"
                    } else {
                        "登录以解锁全部功能"
                    },
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            PrimaryButton(
                text = if (userInfo != null) "进入" else "登录",
                onClick = if (userInfo != null) onOpenWorkspace else onLoginClick,
                modifier = Modifier.height(36.dp).width(80.dp)
            )
        }
    }
}

// ==================== 列表快捷入口（用于"更多"区域）====================

@Composable
private fun QuickEntryItem(
    icon: ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "list_entry_scale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(text = title, color = AppColors.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = AppColors.TextHint, fontSize = 12.sp)
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextHint.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun QuickEntryDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 46.dp)
            .height(0.5.dp)
            .background(AppColors.SeparatorLight)
    )
}
