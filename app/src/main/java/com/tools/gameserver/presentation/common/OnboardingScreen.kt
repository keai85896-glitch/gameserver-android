package com.tools.gameserver.presentation.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.theme.AppSpacing

/** 引导页内容数据类 */
private data class OnboardingPage(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val description: String,
    val needPermission: Boolean = false,
    val permissionType: String = ""
)

/** 引导页列表 */
private val OnboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.Explore,
        iconColor = Color(0xFF60A5FA),
        title = "协议调试工具",
        description = "支持 HTTP 协议头/体编辑，一键发送请求并查看响应"
    ),
    OnboardingPage(
        icon = Icons.Default.Security,
        iconColor = Color(0xFF34D399),
        title = "本地资源管理",
        description = "管理本地协议和物品文件，支持批量选择与批量发送"
    ),
    OnboardingPage(
        icon = Icons.Default.SettingsEthernet,
        iconColor = Color(0xFFF472B6),
        title = "社区分享",
        description = "浏览其他玩家分享的协议与物品，一键下载使用"
    ),
    OnboardingPage(
        icon = Icons.Default.FolderOpen,
        iconColor = Color(0xFFFBBF24),
        title = "存储权限",
        description = "需要「所有文件访问权限」以读取本地游戏协议与物品文件（路径：/storage/emulated/0/游戏私服物品/）",
        needPermission = true,
        permissionType = "storage"
    ),
    OnboardingPage(
        icon = Icons.Default.Layers,
        iconColor = Color(0xFFA78BFA),
        title = "悬浮窗权限",
        description = "需要悬浮窗权限以在其他应用上显示工作台悬浮球",
        needPermission = true,
        permissionType = "overlay"
    )
)

/** 首次使用引导页面 — 5 页滑动引导（含权限请求） */
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    var currentPage by remember { mutableIntStateOf(0) }
    val totalPages = OnboardingPages.size
    val context = androidx.compose.ui.platform.LocalContext.current

    // 跟踪权限状态
    var storageGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager()
            else true // Android 10 以下靠 manifest 声明即可
        )
    }
    var overlayGranted by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // 从设置页返回后刷新状态
    val settingsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        storageGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Environment.isExternalStorageManager() else true
        overlayGranted = Settings.canDrawOverlays(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(AppColors.BackgroundPrimary, AppColors.BackgroundSecondary)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = AppSpacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 跳过按钮
            Row(modifier = Modifier.fillMaxWidth().padding(top = AppSpacing.xl), horizontalArrangement = Arrangement.End) {
                if (currentPage < totalPages - 1) {
                    Text("跳过", color = AppColors.TextHint, fontSize = 15.sp,
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onComplete
                        ).padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.weight(0.15f))

            // 页面内容
            AnimatedContent(
                targetState = currentPage,
                transitionSpec = {
                    val d = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { w -> d * w / 4 } + fadeIn(tween(300)))
                        .togetherWith(slideOutHorizontally { w -> -d * w / 4 } + fadeOut(tween(300)))
                },
                label = "page",
                modifier = Modifier.weight(1f)
            ) { page ->
                val data = OnboardingPages[page]
                val isGranted = when (data.permissionType) {
                    "storage" -> storageGranted
                    "overlay" -> overlayGranted
                    else -> false
                }
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(120.dp).shadow(24.dp, RoundedCornerShape(30.dp), ambientColor = data.iconColor.copy(alpha = 0.2f), spotColor = data.iconColor.copy(alpha = 0.2f))
                        .clip(RoundedCornerShape(30.dp)).background(data.iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                        Icon(data.icon, null, tint = data.iconColor, modifier = Modifier.size(56.dp))
                    }
                    Spacer(Modifier.height(40.dp))
                    Text(data.title, color = AppColors.TextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(14.dp))
                    Text(data.description, color = AppColors.TextSecondary, fontSize = 16.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp), lineHeight = 24.sp)
                    // 权限状态标签
                    if (data.needPermission) {
                        Spacer(Modifier.height(20.dp))
                        val statusColor = if (isGranted) Color(0xFF34D399) else Color(0xFFFBBF24)
                        val statusText = if (isGranted) "已授权" else "未授权 — 点击下方按钮授权"
                        Box(
                            Modifier.clip(RoundedCornerShape(20.dp)).background(statusColor.copy(alpha = 0.15f))
                                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(statusText, color = statusColor, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // 页面指示器
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 32.dp)) {
                OnboardingPages.indices.forEach { index ->
                    val isSelected = index == currentPage
                    val w by animateDpAsState(if (isSelected) 24.dp else 8.dp, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "dw")
                    Box(Modifier.width(w).height(8.dp).clip(CircleShape).background(if (isSelected) AppColors.SystemBlue else AppColors.TextHint.copy(alpha = 0.3f)))
                }
            }

            // 按钮
            Row(Modifier.fillMaxWidth().padding(bottom = 48.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onComplete) { Text("跳过", color = AppColors.TextHint) }
                Box(Modifier.clip(RoundedCornerShape(14.dp)).background(AppColors.SystemBlue)
                    .clickable(remember { MutableInteractionSource() }, null, onClick = {
                        val page = OnboardingPages[currentPage]
                        if (page.needPermission) {
                            when (page.permissionType) {
                                "storage" -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                        // Android 11+：跳转到「所有文件访问权限」设置页
                                        settingsLauncher.launch(
                                            Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                data = Uri.parse("package:${context.packageName}")
                                            }
                                        )
                                    }
                                    // Android 10 及以下不需要运行时授权
                                }
                                "overlay" -> {
                                    settingsLauncher.launch(
                                        Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}"))
                                    )
                                }
                            }
                        }
                        if (currentPage < totalPages - 1) currentPage++ else onComplete()
                    }).padding(horizontal = 28.dp, vertical = 14.dp), contentAlignment = Alignment.Center) {
                    Text(
                        when {
                            currentPage >= totalPages - 1 -> "开始使用"
                            OnboardingPages[currentPage].needPermission -> "前往授权"
                            else -> "下一步"
                        }, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}