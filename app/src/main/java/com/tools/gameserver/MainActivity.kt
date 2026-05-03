package com.tools.gameserver

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.theme.AppTheme
import com.tools.gameserver.core.theme.ThemeModeService
import com.tools.gameserver.data.model.Announcement
import com.tools.gameserver.data.repository.AnnouncementRepository
import com.tools.gameserver.data.repository.VersionCheckResult
import com.tools.gameserver.data.service.api.AuthManager
import com.tools.gameserver.presentation.common.AnnouncementManager
import com.tools.gameserver.presentation.common.OnboardingScreen
import com.tools.gameserver.presentation.common.UpdateDialog
import com.tools.gameserver.presentation.features.community.CommunityScreen
import com.tools.gameserver.presentation.features.home.HomeScreen
import com.tools.gameserver.presentation.features.profile.ProfileScreen
import com.tools.gameserver.presentation.features.workspace.WorkspaceScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val themeModeService by lazy { ThemeModeService.getInstance(applicationContext) }

    companion object {
        val pendingIntentExtras = kotlinx.coroutines.flow.MutableStateFlow<Map<String, String?>>(emptyMap())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AuthManager.init(applicationContext)
        enableEdgeToEdge()
        val extras = extractIntentExtras(intent)
        pendingIntentExtras.value = extras
        setContent {
            AppTheme.GameServerTheme(themeModeService = themeModeService) {
                GameServerApp()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIntentExtras.value = extractIntentExtras(intent)
    }

    private fun extractIntentExtras(intent: Intent?): Map<String, String?> {
        if (intent == null) return emptyMap()
        val map = mutableMapOf<String, String?>()
        intent.getStringExtra("open_workspace")?.let { map["open_workspace"] = it }
        intent.getStringExtra("workspace_id")?.let { map["workspace_id"] = it }
        return map
    }
}

// ==================== 底部导航标签定义 ====================
enum class BottomTab(val label: String) {
    HOME("首页"),
    WORKSPACE("工作台"),
    COMMUNITY("社区"),
    PROFILE("我的")
}

data class TabConfig(
    val tab: BottomTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

// ==================== 主入口 Composable ====================
@Composable
fun GameServerApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableIntStateOf(0) }
    var isLoggedIn by remember { mutableStateOf(AuthManager.isLoggedIn) }
    var showOnboarding by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var announcements by remember { mutableStateOf<List<Announcement>>(emptyList()) }
    var alwaysShow by remember { mutableStateOf(false) }
    var allowEnterMain by remember { mutableStateOf(true) }
    var blockTitle by remember { mutableStateOf("") }
    var blockMessage by remember { mutableStateOf("") }
    var versionCheckResult by remember { mutableStateOf<VersionCheckResult?>(null) }
    val intentExtras by MainActivity.pendingIntentExtras.collectAsState()
    val tabConfigs = listOf(
        TabConfig(BottomTab.HOME, Icons.Filled.Home, Icons.Outlined.Home),
        TabConfig(BottomTab.WORKSPACE, Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        TabConfig(BottomTab.COMMUNITY, Icons.Filled.People, Icons.Outlined.People),
        TabConfig(BottomTab.PROFILE, Icons.Filled.Person, Icons.Outlined.Person)
    )

    // 检查是否首次启动
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
        val hasSeenOnboarding = prefs.getBoolean("seen_onboarding", false)
        if (!hasSeenOnboarding) {
            showOnboarding = true
        }
    }

    // 加载公告和版本检查
    LaunchedEffect(Unit) {
        scope.launch {
            val result = withContext(Dispatchers.IO) { AnnouncementRepository.loadAnnouncements() }
            result.onSuccess { loadResult ->
                announcements = loadResult.announcements
                alwaysShow = loadResult.alwaysShow
                allowEnterMain = loadResult.allowEnterMain
                blockTitle = loadResult.blockTitle
                blockMessage = loadResult.blockMessage
            }
            isLoading = false
        }
    }

    // 监听登录状态变化
    LaunchedEffect(Unit) {
        scope.launch {
            AuthManager.userState.collect { userInfo ->
                isLoggedIn = userInfo != null
            }
        }
    }

    // 监听 Intent 深链接
    LaunchedEffect(intentExtras) {
        val workspaceId = intentExtras["workspace_id"]
        if (workspaceId != null) {
            currentTab = 1 // Workspace tab
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(AppColors.Background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = AppColors.SystemBlue)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(AppColors.GradientStart, AppColors.BackgroundPrimary, AppColors.GradientEnd))
    )) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 状态栏占位
            Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars))

            // 内容区
Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        when (currentTab) {
                            0 -> HomeScreen(
                                userInfo = null,
                                onOpenWorkspace = { currentTab = 1 },
                                onOpenCommunity = { currentTab = 2 },
                                onOpenProfile = { currentTab = 3 },
                                onLoginClick = { }
                            )
                            1 -> WorkspaceScreen()
                            2 -> CommunityScreen()
                            3 -> ProfileScreen(
                                userInfo = null,
                                onLoginClick = { },
                                onLogoutClick = {
                                    AuthManager.logout()
                                    isLoggedIn = false
                                }
                            )
                        }
                    }

            // 底部导航栏
            BottomNavBar(
                tabConfigs = tabConfigs,
                currentTab = currentTab,
                onSelect = { currentTab = it }
            )
        }

        // 公告弹窗
        if (announcements.isNotEmpty()) {
            val readPrefs = remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                // 检查是否需要弹出公告
            }
            AnnouncementManager(
                announcements = announcements,
                alwaysShow = alwaysShow,
                onDismiss = { announcements = emptyList() }
            )
        }

        // 版本更新弹窗
        versionCheckResult?.let { result ->
            if (result.needUpdate) {
                UpdateDialog(
                    versionResult = result,
                    onDismiss = { versionCheckResult = null }
                )
            }
        }

        // 强制阻止进入
        if (!allowEnterMain && blockTitle.isNotBlank()) {
            ForceBlockDialog(
                title = blockTitle,
                message = blockMessage
            )
        }

        // 首次启动引导
        if (showOnboarding) {
            OnboardingScreen(
                onComplete = {
                    showOnboarding = false
                    // 标记已看过引导
                    val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("seen_onboarding", true).apply()
                }
            )
        }
    }
}

@Composable
private fun ForceBlockDialog(title: String, message: String) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { /* 不允许关闭 */ },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {}
    )
}

@Composable
private fun BottomNavBar(
    tabConfigs: List<TabConfig>,
    currentTab: Int,
    onSelect: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    // 缓存 Composable 色值到局部变量（drawBehind lambda 内非 @Composable 上下文）
    val glassFrostColor = AppColors.GlassFrost
    val specularHighlight = AppColors.GlassSpecular.copy(alpha = 0.5f)
    val shadowDarkColor = AppColors.ShadowDark.copy(alpha = 0.12f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                ambientColor = shadowDarkColor,
                spotColor = shadowDarkColor
            )
            .background(glassFrostColor)
            .drawBehind {
                // 顶部镜面高光
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(specularHighlight, Color.Transparent),
                        startY = 0f,
                        endY = size.height * 0.15f
                    )
                )
            }
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabConfigs.forEachIndexed { index, tabConfig ->
                val isSelected = currentTab == index
                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) AppColors.SystemBlue else AppColors.TextHint,
                    animationSpec = tween(250),
                    label = "tab_color"
                )
                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1f,
                    animationSpec = spring(dampingRatio = 0.6f),
                    label = "tab_icon_scale"
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelect(index)
                            }
                        )
                        .padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .shadow(
                                elevation = if (isSelected) 3.dp else 0.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = AppColors.SystemBlue.copy(alpha = if (isSelected) 0.15f else 0f),
                                spotColor = AppColors.SystemBlue.copy(alpha = if (isSelected) 0.15f else 0f)
                            )
                            .background(
                                color = if (isSelected) AppColors.SystemBlue.copy(alpha = 0.1f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tabConfig.selectedIcon else tabConfig.unselectedIcon,
                            contentDescription = tabConfig.tab.label,
                            modifier = Modifier.size(20.dp).scale(iconScale),
                            tint = animatedColor
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = tabConfig.tab.label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = animatedColor
                    )
                }
            }
        }
    }
}