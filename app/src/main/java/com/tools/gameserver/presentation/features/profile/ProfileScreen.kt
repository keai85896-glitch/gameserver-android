package com.tools.gameserver.presentation.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.constants.AppConstants
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.theme.ThemeMode
import com.tools.gameserver.core.theme.ThemeModeService
import com.tools.gameserver.data.service.api.UserInfo
import com.tools.gameserver.presentation.common.GlassCard
import com.tools.gameserver.core.util.SnackbarManager
import kotlinx.coroutines.launch

/** iOS 液态玻璃风格『我的』页面 */
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userInfo: UserInfo? = null,
    onLoginClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeService = remember { ThemeModeService.getInstance(context.applicationContext) }
    val currentThemeMode by themeService.themeModeFlow.collectAsState(initial = ThemeMode.SYSTEM)
    var showAboutDialog by remember { mutableStateOf(false) }

    // 液态玻璃 Hero 渐变色
    val heroStart = AppColors.SystemBlue.copy(alpha = 0.08f)
    val heroEnd = Color.Transparent

    LazyColumn(
        modifier = modifier.fillMaxSize().background(AppColors.BackgroundPrimary),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Hero 区域 — 液态玻璃渐变背景
        item {
            Box(
                modifier = Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(heroStart, heroEnd)))
            ) {
                Text(
                    "我的",
                    color = AppColors.TextPrimary,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 16.dp, bottom = 12.dp)
                )
            }
        }

        // 用户卡片 — 液态玻璃
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 4.dp)) {
                if (userInfo != null) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(48.dp).clip(CircleShape)
                                    .background(AppColors.SystemBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    userInfo.username.take(1).uppercase(),
                                    color = AppColors.SystemBlue,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(Modifier.weight(1f)) {
                                Text(userInfo.username, color = AppColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                                Text(userInfo.email, color = AppColors.TextSecondary, fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusChip(
                                if (userInfo.uploadAuthorized || userInfo.role == "admin") "已授权上传" else "只读用户",
                                if (userInfo.uploadAuthorized) AppColors.SystemGreen else AppColors.TextHint
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onLoginClick).padding(vertical = 14.dp)
                    ) {
                        Box(
                            Modifier.size(48.dp).clip(CircleShape)
                                .background(AppColors.TextHint.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Palette, null, tint = AppColors.TextHint, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("未登录", color = AppColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text("点击登录以解锁完整功能", color = AppColors.TextSecondary, fontSize = 13.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextHint)
                    }
                }
            }
        }

// 数据统计卡片
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 8.dp)) {
                Column(modifier = Modifier.padding(vertical = 14.dp, horizontal = 16.dp)) {
                    Text(
                        "本地数据",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "游戏",
                            value = "—",
                            iconColor = AppColors.SystemBlue
                        )
                        StatItem(
                            label = "协议",
                            value = "—",
                            iconColor = AppColors.SystemGreen
                        )
                        StatItem(
                            label = "物品",
                            value = "—",
                            iconColor = AppColors.SystemPurple
                        )
                    }
                }
            }
        }

        // 设置分组 — 主题
        item { SettingsSectionHeader("个性化") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                SettingsRow(
                    icon = Icons.Default.ColorLens,
                    iconColor = AppColors.SystemBlue,
                    title = "主题模式",
                    subtitle = currentThemeMode.displayName,
                    onClick = {
                        coroutineScope.launch {
                            val next = when (currentThemeMode) {
                                ThemeMode.SYSTEM -> ThemeMode.LIGHT
                                ThemeMode.LIGHT -> ThemeMode.DARK
                                ThemeMode.DARK -> ThemeMode.SYSTEM
                            }
                            themeService.setThemeMode(next)
                        }
                    }
                )
            }
        }

        // 数据管理
        item { SettingsSectionHeader("数据") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column {
                    SettingsRow(
                        icon = Icons.Default.Storage,
                        iconColor = AppColors.SystemTeal,
                        title = "存储位置",
                        subtitle = "/storage/emulated/0/游戏私服物品/",
                        onClick = { /* 打开文件管理器 */ }
                    )
                    HorizontalDivider(color = AppColors.SeparatorLight, thickness = 0.5.dp, modifier = Modifier.padding(start = 44.dp))
                    SettingsRow(
                        icon = Icons.Default.CleaningServices,
                        iconColor = AppColors.SystemOrange,
                        title = "清除缓存",
                        subtitle = "清除临时文件和缓存数据",
                        onClick = {
                            coroutineScope.launch {
                                val cacheDir = context.cacheDir
                                cacheDir.deleteRecursively()
                                SnackbarManager.show("缓存已清除")
                            }
                        }
                    )
                }
            }
        }

        // 关于
        item { SettingsSectionHeader("关于") }
        item {
            GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                SettingsRow(
                    icon = Icons.Default.Info,
                    iconColor = AppColors.SystemBlue,
                    title = "关于",
                    subtitle = "v${AppConstants.APP_VERSION} · 检查更新",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        // 账户操作
        if (userInfo != null) {
            item { SettingsSectionHeader("账户") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                    SettingsRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        iconColor = AppColors.SystemRed,
                        title = "退出登录",
                        subtitle = "",
                        onClick = onLogoutClick
                    )
                }
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }

    // 关于弹窗 — 液态玻璃风格
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column {
                    Text(
                        "GameServer Toolbox v${AppConstants.APP_VERSION}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "用于游戏私服的协议调试与资源管理工具。支持协议编辑、批量发送、本地资源管理和社区分享。",
                        color = AppColors.TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(
                        "官方 QQ 群: ${AppConstants.QQ_GROUP_NUM}",
                        color = AppColors.SystemYellow,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showAboutDialog = false }) { Text("关闭") } }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        color = AppColors.TextSecondary,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(30.dp).clip(CircleShape).background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(17.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = AppColors.TextPrimary, fontSize = 16.sp)
            if (subtitle.isNotBlank()) {
                Text(subtitle, color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextHint.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun StatusChip(label: String, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier.clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .border(0.5.dp, color.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp, horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center)
    }
}

private val ThemeMode.displayName: String get() = when (this) {
    ThemeMode.SYSTEM -> "跟随系统"
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    iconColor: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(40.dp).clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                color = iconColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(label, color = AppColors.TextSecondary, fontSize = 12.sp)
    }
}