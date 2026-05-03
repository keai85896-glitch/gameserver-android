package com.tools.gameserver.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.tools.gameserver.MainActivity
import com.tools.gameserver.R
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.theme.AppTheme
import com.tools.gameserver.core.theme.ThemeModeService
import com.tools.gameserver.core.constants.AppConstants
import com.tools.gameserver.data.model.BodyType
import com.tools.gameserver.data.model.ParsedData
import com.tools.gameserver.data.service.ProtocolParser
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import com.tools.gameserver.presentation.features.workspace.parseItemFile
import com.tools.gameserver.presentation.features.workspace.scanLocalGameDirectories
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URLEncoder
import kotlin.math.abs

class SystemFloatingWindowService : Service(), LifecycleOwner, SavedStateRegistryOwner {

    companion object {
        private const val CHANNEL_ID = "system_floating_window"
        private const val NOTIFICATION_ID = 12041
        private const val TAG = "SystemFloatingWindow"

        fun start(context: Context) {
            if (!Settings.canDrawOverlays(context)) {
                Toast.makeText(context, "请先授予悬浮窗权限", Toast.LENGTH_SHORT).show()
                return
            }
            try {
                val intent = Intent(context, SystemFloatingWindowService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "悬浮窗启动失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SystemFloatingWindowService::class.java))
        }
    }

    // ==================== Lifecycle ====================
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private lateinit var windowManager: WindowManager

    // ==================== 主题服务 ====================
    private lateinit var themeModeService: ThemeModeService

    // ==================== 状态 ====================
    private var ballView: ComposeView? = null
    private var panelView: ComposeView? = null
    private val showPanel = mutableStateOf(false)

    // 页面状态
    private val currentPage = mutableStateOf<com.tools.gameserver.service.floating.FloatingPage>(
        com.tools.gameserver.service.floating.FloatingPage.GameList
    )

    // 批量发送状态
    private val savedBatchTargetParam = mutableStateOf("")
    private val savedBatchDelayMs = mutableStateOf(1000L)
    private val savedBatchEditableBody = mutableStateMapOf<String, String>()
    private val savedSelectedIndices = mutableStateOf(setOf<Int>())

    // 位置记忆
    private val posPrefs by lazy { getSharedPreferences(AppConstants.FLOATING_POSITIONS, MODE_PRIVATE) }

    private fun saveBallPosition(x: Int, y: Int) {
        posPrefs.edit().putInt("ball_x", x).putInt("ball_y", y).apply()
    }
    private fun loadBallPosition(): Pair<Int, Int> {
        return Pair(posPrefs.getInt("ball_x", -1), posPrefs.getInt("ball_y", -1))
    }
    private fun savePanelPosition(x: Int, y: Int) {
        posPrefs.edit().putInt("panel_x", x).putInt("panel_y", y).apply()
    }
    private fun loadPanelPosition(): Pair<Int, Int> {
        return Pair(posPrefs.getInt("panel_x", -1), posPrefs.getInt("panel_y", -1))
    }

    // ==================== Service 生命周期 ====================
    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        themeModeService = ThemeModeService.getInstance(this)
        createNotificationChannel()
        startForeground()
        showFloatingBall()
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        serviceScope.cancel()
        removeBall()
        removePanel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 横竖屏切换时重新布局
        if (showPanel.value) {
            removePanel()
            showFloatingPanel()
        }
    }

    // ==================== 通知 ====================
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "悬浮窗服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "悬浮窗运行中" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("游戏私服工具箱")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("游戏私服工具箱")
                .setContentText("悬浮窗运行中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    // ==================== View 管理 ====================
    private fun attachOwners(view: ComposeView) {
        view.setViewTreeLifecycleOwner(this)
        view.setViewTreeSavedStateRegistryOwner(this)
    }

    private fun removeBall() {
        ballView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        ballView = null
    }

    private fun removePanel() {
        panelView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        panelView = null
    }

    private fun getScreenMetrics(): DisplayMetrics {
        return resources.displayMetrics
    }

    private fun isLandscape(): Boolean {
        return resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    // ==================== 悬浮球 ====================
    private fun showFloatingBall() {
        if (ballView != null) return
        try {
            val dm = getScreenMetrics()
            val size = (48 * dm.density).toInt()
            val savedPos = loadBallPosition()
            val params = WindowManager.LayoutParams(
                size, size,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = if (savedPos.first >= 0) savedPos.first else dm.widthPixels - size - (16 * dm.density).toInt()
                y = if (savedPos.second >= 0) savedPos.second else (dm.heightPixels / 3)
            }
            val view = ComposeView(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setClipToOutline(false)
                attachOwners(this)
                setContent {
                    AppTheme.GameServerTheme(themeModeService = themeModeService) { FloatingBall(params = params) }
                }
            }
            windowManager.addView(view, params)
            ballView = view
        } catch (e: Exception) {
            Log.e(TAG, "显示悬浮球失败: ${e.message}")
        }
    }

    @Composable
    private fun FloatingBall(params: WindowManager.LayoutParams) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.88f else 1f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 400f),
            label = "ball_scale"
        )
        val haptic = LocalHapticFeedback.current

        var dragStartX = 0f
        var dragStartY = 0f
        var totalDragDistance = 0f
        var isDragging = false

        // 缓存 @Composable 色值
        val ballGlassFill = AppColors.GlassFill
        val ballShadowDark = AppColors.ShadowDark
        val ballSpecular = AppColors.GlassSpecular
        val ballHighlight = AppColors.GlassHighlight
        val ballBorder = AppColors.GlassBorder
        val ballBlue = AppColors.SystemBlue

        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .shadow(10.dp, CircleShape, ambientColor = ballShadowDark, spotColor = ballShadowDark)
                .clip(CircleShape)
                .background(ballBlue.copy(alpha = 0.85f))
                .border(1.dp, ballHighlight, CircleShape)
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        dragStartX = down.position.x
                        dragStartY = down.position.y
                        totalDragDistance = 0f
                        isDragging = false

                        do {
                            val event = awaitPointerEvent()
                            event.changes.forEach { change ->
                                if (change.pressed) {
                                    val dx = change.position.x - dragStartX
                                    val dy = change.position.y - dragStartY
                                    val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                                    totalDragDistance = maxOf(totalDragDistance, dist)
                                    if (totalDragDistance > 10f) {
                                        isDragging = true
                                        val moveX = change.position.x - change.previousPosition.x
                                        val moveY = change.position.y - change.previousPosition.y
                                        params.x += moveX.toInt()
                                        params.y += moveY.toInt()
                                        ballView?.let { windowManager.updateViewLayout(it, params) }
                                        change.consume()
                                    }
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        if (!isDragging) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            removeBall()
                            showFloatingPanel()
                        } else {
                            saveBallPosition(params.x, params.y)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Layers, null, tint = AppColors.White, modifier = Modifier.size(22.dp))
        }
    }

    // ==================== 悬浮面板 ====================
    private fun showFloatingPanel() {
        if (panelView != null) return
        try {
            val dm = getScreenMetrics()
            val width = if (isLandscape()) (dm.widthPixels * 0.6f).toInt() else (dm.widthPixels * 0.92f).toInt()
            val height = if (isLandscape()) (dm.heightPixels * 0.85f).toInt() else (dm.heightPixels * 0.7f).toInt()
            val savedPos = loadPanelPosition()
            val params = WindowManager.LayoutParams(
                width, height,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                else @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = if (savedPos.first >= 0) savedPos.first else (dm.widthPixels - width) / 2
                y = if (savedPos.second >= 0) savedPos.second else (dm.heightPixels - height) / 3
                softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
            }
            showPanel.value = true
            val view = ComposeView(this).apply {
                attachOwners(this)
                setContent {
                    AppTheme.GameServerTheme(themeModeService = themeModeService) {
                        FloatingPanel(
                            params = params,
                            onClose = {
                                removePanel()
                                showPanel.value = false
                                showFloatingBall()
                            }
                        )
                    }
                }
            }
            windowManager.addView(view, params)
            panelView = view
        } catch (e: Exception) {
            Log.e(TAG, "显示悬浮窗失败: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    @Composable
    private fun FloatingPanel(params: WindowManager.LayoutParams, onClose: () -> Unit) {
        val context = LocalContext.current
        val page by currentPage
        val isLandscape = isLandscape()
        val haptic = LocalHapticFeedback.current

        // 缓存 @Composable 色值
        val panelGlassFill = AppColors.GlassFill
        val panelShadowDark = AppColors.ShadowDark
        val panelSpecular = AppColors.GlassSpecular
        val panelHighlight = AppColors.GlassHighlight
        val panelBorder = AppColors.GlassBorder
        val panelRefraction = AppColors.GlassRefraction
        val panelEdgeGlow = AppColors.GlassEdgeGlow
        val panelShape = RoundedCornerShape(20.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .shadow(16.dp, panelShape, ambientColor = panelShadowDark, spotColor = panelShadowDark)
                .clip(panelShape)
                .background(panelGlassFill)
                .border(1.dp, panelHighlight, panelShape)
                .imePadding()
        ) {
            // 顶部标题栏（拖动手势放在工具栏，避免和内部滚动冲突）
            FloatingToolbar(
                page = page,
                onClose = onClose,
                params = params
            )

            // 内容区
            Box(modifier = Modifier.fillMaxSize()) {
                when (val p = page) {
                    is com.tools.gameserver.service.floating.FloatingPage.GameList -> FloatingGameList()
                    is com.tools.gameserver.service.floating.FloatingPage.GameDetail -> FloatingGameDetail(p.entry, isLandscape)
                    is com.tools.gameserver.service.floating.FloatingPage.ProtocolResult -> FloatingProtocolResult(p, isLandscape)
                }
            }
        }
    }

    @Composable
    private fun FloatingToolbar(page: com.tools.gameserver.service.floating.FloatingPage, onClose: () -> Unit, params: WindowManager.LayoutParams) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.GlassFrost)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page !is com.tools.gameserver.service.floating.FloatingPage.GameList) {
                IconButton(onClick = {
                    currentPage.value = when (page) {
                        is com.tools.gameserver.service.floating.FloatingPage.GameDetail ->
                            com.tools.gameserver.service.floating.FloatingPage.GameList
                        is com.tools.gameserver.service.floating.FloatingPage.ProtocolResult ->
                            com.tools.gameserver.service.floating.FloatingPage.GameDetail(page.entry)
                        else -> com.tools.gameserver.service.floating.FloatingPage.GameList
                    }
                }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = AppColors.TextPrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = when (page) {
                    is com.tools.gameserver.service.floating.FloatingPage.GameList -> "游戏列表"
                    is com.tools.gameserver.service.floating.FloatingPage.GameDetail -> page.entry.gameName
                    is com.tools.gameserver.service.floating.FloatingPage.ProtocolResult -> "物品列表"
                },
                color = AppColors.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        params.x += dragAmount.x.toInt()
                        params.y += dragAmount.y.toInt()
                        panelView?.let { windowManager.updateViewLayout(it, params) }
                        savePanelPosition(params.x, params.y)
                    }
                }
            )
            IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, "关闭", tint = AppColors.TextHint, modifier = Modifier.size(18.dp))
            }
        }
    }

    // ==================== 游戏列表页 ====================
    @Composable
    private fun FloatingGameList() {
        val context = LocalContext.current
        var entries by remember { mutableStateOf<List<LocalGameEntry>>(emptyList()) }
        var isLoading by remember { mutableStateOf(true) }
        var searchQuery by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                entries = scanLocalGameDirectories()
            }
            isLoading = false
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 搜索栏
            FloatingSearchBar(searchQuery) { searchQuery = it }

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                }
            } else {
                val filtered = if (searchQuery.isBlank()) entries
                else entries.filter {
                    it.gameName.contains(searchQuery, true) || it.author.contains(searchQuery, true)
                }
                if (filtered.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无游戏目录", color = AppColors.TextHint, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        items(filtered) { entry ->
                            GameListCard(entry) {
                                currentPage.value = com.tools.gameserver.service.floating.FloatingPage.GameDetail(entry)
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun GameListCard(entry: LocalGameEntry, onClick: () -> Unit) {
        val haptic = LocalHapticFeedback.current
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 3.dp)
                .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = AppColors.ShadowDark, spotColor = AppColors.ShadowDark)
                .clip(RoundedCornerShape(14.dp))
                .background(AppColors.GlassFill)
                .border(0.5.dp, AppColors.GlassBorder, RoundedCornerShape(14.dp))
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.gameName, color = AppColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${entry.author} · ${entry.protocolFiles.size} 协议 · ${entry.itemFiles.size} 物品",
                    color = AppColors.TextSecondary, fontSize = 12.sp
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextHint, modifier = Modifier.size(18.dp))
        }
    }

    // ==================== 协议文件内容解析 ====================
    private fun parseProtocolContent(content: String): Pair<String, String> {
        if (content.contains("---BODY---")) {
            val parts = content.split("---BODY---", limit = 2)
            return parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
        }
        val lines = content.lines()
        val blankLineIdx = lines.indexOfFirst { it.isBlank() }
        if (blankLineIdx > 0) {
            val headers = lines.subList(0, blankLineIdx).joinToString("\n").trim()
            val body = lines.subList(blankLineIdx + 1, lines.size).joinToString("\n").trim()
            return headers to body
        }
        return content.trim() to ""
    }

    // ==================== 游戏详情页（悬浮窗只显示物品文件） ====================
    @Composable
    private fun FloatingGameDetail(entry: LocalGameEntry, isLandscape: Boolean) {
        val context = LocalContext.current
        var showProtocolPicker by remember { mutableStateOf(false) }
        var pendingItemFile by remember { mutableStateOf<java.io.File?>(null) }
        var pendingItems by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(8.dp)) {
                if (entry.itemFiles.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("该目录下暂无物品文件", color = AppColors.TextHint, fontSize = 12.sp)
                    }
                } else {
                                        Text("物品文件", color = AppColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                    entry.itemFiles.forEach { file ->
                        FileCard(file.nameWithoutExtension, "物品") {
                            serviceScope.launch {
                                try {
                                    val items = withContext(Dispatchers.IO) {
                                        if (!file.exists()) {
                                            Toast.makeText(this@SystemFloatingWindowService, "文件不存在: ${file.name}", Toast.LENGTH_SHORT).show()
                                            return@withContext emptyList<Pair<String, String>>()
                                        }
                                        parseItemFile(file)
                                    }
                                    if (items.isNotEmpty()) {
                                        if (entry.protocolFiles.isEmpty()) {
                                            Toast.makeText(this@SystemFloatingWindowService, "该目录下没有协议文件", Toast.LENGTH_SHORT).show()
                                        } else if (entry.protocolFiles.size == 1) {
                                            val protocolFile = entry.protocolFiles.first()
                                            if (!protocolFile.exists()) {
                                                Toast.makeText(this@SystemFloatingWindowService, "协议文件不存在: ${protocolFile.name}", Toast.LENGTH_SHORT).show()
                                                return@launch
                                            }
                                            goToProtocolResult(entry, protocolFile, file, items)
                                        } else {
                                            pendingItemFile = file
                                            pendingItems = items
                                            showProtocolPicker = true
                                        }
                                    } else {
                                        Toast.makeText(this@SystemFloatingWindowService, "物品文件为空或解析失败", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "处理物品文件失败: ${e.message}", e)
                                    Toast.makeText(this@SystemFloatingWindowService, "处理失败: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }

            // 协议选择覆盖层（不使用 AlertDialog，避免悬浮窗环境崩溃）
            if (showProtocolPicker && pendingItemFile != null) {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f))
                        .clickable(onClick = { showProtocolPicker = false })
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center)
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AppColors.GlassFill)
                            .border(0.5.dp, AppColors.GlassBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Text("选择协议文件", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = AppColors.TextPrimary)
                        Spacer(Modifier.height(12.dp))
                        entry.protocolFiles.forEach { file ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        showProtocolPicker = false
                                        goToProtocolResult(entry, file, pendingItemFile!!, pendingItems)
                                    }
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Description, null, tint = AppColors.SystemBlue, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(10.dp))
                                Text(file.nameWithoutExtension, color = AppColors.TextPrimary, fontSize = 14.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "取消",
                            color = AppColors.SystemBlue, fontSize = 13.sp,
                            modifier = Modifier.align(Alignment.End).clip(RoundedCornerShape(6.dp))
                                .clickable { showProtocolPicker = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }

    private fun goToProtocolResult(
        entry: LocalGameEntry,
        protocolFile: java.io.File,
        itemFile: java.io.File,
        items: List<Pair<String, String>>
    ) {
        serviceScope.launch {
            try {
                val raw = withContext(Dispatchers.IO) { protocolFile.readText() }
                val (headers, body) = parseProtocolContent(raw)
                val parsed = ProtocolParser.parse(headers, body)
                currentPage.value = com.tools.gameserver.service.floating.FloatingPage.ProtocolResult(
                    entry = entry, parsedData = parsed, rawHeaders = headers, rawBody = body,
                    allItems = items, itemFileName = itemFile.nameWithoutExtension
                )
            } catch (e: Exception) {
                Log.e(TAG, "加载协议失败: ${e.message}", e)
                Toast.makeText(this@SystemFloatingWindowService, "加载协议失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Composable
    private fun FileCard(name: String, type: String, onClick: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.ShadowDark, spotColor = AppColors.ShadowDark)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.GlassFill)
                .border(0.5.dp, AppColors.GlassBorder, RoundedCornerShape(12.dp))
                .clickable(onClick = onClick)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (type == "协议") Icons.Default.Description else Icons.Default.FolderZip,
                null,
                tint = if (type == "协议") AppColors.SystemBlue else AppColors.SystemPurple,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextHint, modifier = Modifier.size(16.dp))
        }
    }

    // ==================== 搜索栏 ====================
    @Composable
    private fun FloatingSearchBar(query: String, onQueryChange: (String) -> Unit) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = AppColors.ShadowDark, spotColor = AppColors.ShadowDark)
                .clip(RoundedCornerShape(12.dp))
                .background(AppColors.GlassFill)
                .border(0.5.dp, AppColors.GlassBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, tint = AppColors.TextHint, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                BasicTextField(
                    value = query, onValueChange = onQueryChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 14.sp),
                    singleLine = true,
                    cursorBrush = SolidColor(AppColors.SystemBlue),
                    decorationBox = { inner ->
                        if (query.isBlank()) Text("搜索游戏...", color = AppColors.TextHint, fontSize = 14.sp)
                        inner()
                    }
                )
            }
        }
    }

    // ==================== 协议结果页（批量发送核心） ====================
    @Composable
    private fun FloatingProtocolResult(
        page: com.tools.gameserver.service.floating.FloatingPage.ProtocolResult,
        isLandscape: Boolean
    ) {
        val context = LocalContext.current
        val parsed = page.parsedData
        val allItems = page.allItems
        val pageSize = if (isLandscape) 50 else 20
        var selectedIndices by savedSelectedIndices
        var batchTargetParam by savedBatchTargetParam
        var batchDelayMs by savedBatchDelayMs
        var editableBody by remember { mutableStateOf(page.rawBody) }
        var isBatchSending by remember { mutableStateOf(false) }
        var batchProgress by remember { mutableIntStateOf(0) }
        var batchSuccessCount by remember { mutableIntStateOf(0) }
        var batchFailCount by remember { mutableIntStateOf(0) }
        var searchQuery by remember { mutableStateOf("") }
        var currentPage by remember { mutableIntStateOf(0) }
        var showProtocolDetail by remember { mutableStateOf(false) }
        var batchJob by remember { mutableStateOf<Job?>(null) }

        val filteredItems = if (searchQuery.isBlank()) {
            allItems.mapIndexed { i, item -> i to item }
        } else {
            allItems.mapIndexedNotNull { i, (code, name) ->
                if (name.contains(searchQuery, true) || code.contains(searchQuery, true)) i to (code to name) else null
            }
        }
        val totalPages = maxOf(1, (filteredItems.size + pageSize - 1) / pageSize)
        val safePage = currentPage.coerceIn(0, totalPages - 1)
        val pageItems = filteredItems.drop(safePage * pageSize).take(pageSize)
        val filteredIndices = filteredItems.map { it.first }.toSet()
        LaunchedEffect(searchQuery) { currentPage = 0 }

        // URL 编码行
        val urlEncodedRows = remember {
            val rows = mutableStateListOf<Pair<String, String>>()
            if (parsed.bodyType == BodyType.URL_ENCODED) {
                parsed.bodyParams.forEach { (k, v) -> rows.add(k to v) }
            }
            rows
        }

        Column(modifier = Modifier.fillMaxSize()) {
            // 可用参数列表
            val availableParams = remember {
                when (parsed.bodyType) {
                    BodyType.URL_ENCODED -> urlEncodedRows.map { it.first }
                    BodyType.JSON -> {
                        try {
                            org.json.JSONObject(page.rawBody).keys().asSequence().toList()
                        } catch (_: Exception) { emptyList() }
                    }
                    else -> emptyList()
                }
            }

            // 批量控制栏
            BatchControlBar(
                selectedCount = selectedIndices.size,
                totalCount = allItems.size,
                filteredCount = filteredItems.size,
                targetParam = batchTargetParam,
                availableParams = availableParams,
                delayMs = batchDelayMs,
                isSending = isBatchSending,
                progress = batchProgress,
                successCount = batchSuccessCount,
                failCount = batchFailCount,
                pageSize = pageSize,
                safePage = safePage,
                totalPages = totalPages,
                onToggleAll = {
                    if (selectedIndices.size == allItems.size) selectedIndices = emptySet()
                    else selectedIndices = allItems.indices.toSet()
                },
                onSelectFiltered = { selectedIndices = filteredIndices },
                onClearSelection = { selectedIndices = emptySet() },
                onParamChange = { batchTargetParam = it },
                onDelayChange = { batchDelayMs = it },
                onPageChange = { currentPage = it },
                onStartBatch = {
                    isBatchSending = true
                    batchProgress = 0
                    batchSuccessCount = 0
                    batchFailCount = 0
                    batchJob = serviceScope.launch {
                        val indices = selectedIndices.sorted()
                        for (idx in indices) {
                            ensureActive()
                            if (!isBatchSending) break
                            val (code, name) = allItems[idx]
                            val bodyToSend = buildBodyWithItem(parsed, editableBody, urlEncodedRows, batchTargetParam, code)
                            try {
                                val result = withContext(Dispatchers.IO) {
                                    ApiClient.proxyDirect(
                                        targetUrl = parsed.url,
                                        headers = parsed.headers,
                                        rawBody = bodyToSend,
                                        method = parsed.method,
                                        bodyType = parsed.bodyType
                                    )
                                }
                                if (result is ApiResult.Success) batchSuccessCount++
                                else batchFailCount++
                            } catch (e: Exception) {
                                batchFailCount++
                            }
                            batchProgress++
                            delay(batchDelayMs)
                        }
                        isBatchSending = false
                    }
                },
                onCancelBatch = {
                    isBatchSending = false
                    batchJob?.cancel()
                    batchJob = null
                },
                showDetail = showProtocolDetail,
                onToggleDetail = { showProtocolDetail = !showProtocolDetail }
            )

            // 搜索栏
            FloatingSearchBar(searchQuery) { searchQuery = it }

            // Body 编辑区
            if (showProtocolDetail) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp)).background(AppColors.GlassFill).border(0.5.dp, AppColors.GlassBorder, RoundedCornerShape(8.dp)).padding(8.dp)) {
                    // 请求信息摘要
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(4.dp), color = AppColors.SystemBlue) {
                            Text(parsed.method, color = AppColors.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(parsed.url, color = AppColors.TextSecondary, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(6.dp))
                    // URL_ENCODED: 可编辑的参数列表（参数即请求体）
                    if (parsed.bodyType == BodyType.URL_ENCODED && urlEncodedRows.isNotEmpty()) {
                        Text("请求参数", color = AppColors.TextSecondary, fontSize = 10.sp)
                        Spacer(Modifier.height(3.dp))
                        urlEncodedRows.forEachIndexed { idx, (k, v) ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(k, color = AppColors.SystemBlue, fontSize = 10.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.width(60.dp))
                                BasicTextField(
                                    value = v,
                                    onValueChange = { nv -> urlEncodedRows[idx] = k to nv; editableBody = urlEncodedRows.joinToString("&") { "${it.first}=${it.second}" } },
                                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(4.dp)).background(AppColors.GlassFrost).padding(horizontal = 6.dp, vertical = 3.dp),
                                    textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                                    singleLine = true,
                                    cursorBrush = SolidColor(AppColors.SystemBlue)
                                )
                            }
                        }
                    }
                    // JSON/RAW: 可编辑的原始请求体
                    else if (parsed.bodyType != BodyType.NONE) {
                        Text("请求体", color = AppColors.TextSecondary, fontSize = 10.sp)
                        Spacer(Modifier.height(3.dp))
                        BasicTextField(
                            value = editableBody,
                            onValueChange = { editableBody = it },
                            modifier = Modifier.fillMaxWidth().height(80.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.GlassFrost).padding(6.dp),
                            textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 10.sp, fontFamily = FontFamily.Monospace),
                            cursorBrush = SolidColor(AppColors.SystemBlue)
                        )
                    }
                    // Headers 折叠区
                    Spacer(Modifier.height(4.dp))
                    var headersVisible by remember { mutableStateOf(false) }
                    Row(modifier = Modifier.clickable { headersVisible = !headersVisible }.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("请求头", color = AppColors.TextHint, fontSize = 10.sp)
                        Icon(if (headersVisible) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = AppColors.TextHint, modifier = Modifier.size(14.dp))
                    }
                    if (headersVisible) {
                        Text(page.rawHeaders, color = AppColors.TextSecondary, fontSize = 9.sp, fontFamily = FontFamily.Monospace, maxLines = 10, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }

            // 物品列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                itemsIndexed(pageItems) { _, (idx, item) ->
                    val (code, name) = item
                    ItemRow(
                        index = idx,
                        code = code,
                        name = name,
                        isSelected = selectedIndices.contains(idx),
                        onToggle = {
                            selectedIndices = if (selectedIndices.contains(idx))
                                selectedIndices - idx
                            else selectedIndices + idx
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun ItemRow(index: Int, code: String, name: String, isSelected: Boolean, onToggle: () -> Unit) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) AppColors.SystemBlue.copy(alpha = 0.12f) else AppColors.GlassFill)
                .border(0.5.dp, if (isSelected) AppColors.SystemBlue.copy(alpha = 0.3f) else AppColors.GlassBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onToggle)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(20.dp).clip(RoundedCornerShape(5.dp))
                    .background(if (isSelected) AppColors.SystemBlue else AppColors.BorderMain),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) Icon(Icons.Filled.Check, "已选", tint = AppColors.White, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(code, color = AppColors.TextHint, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }

    // ==================== 批量控制栏 ====================
    @Composable
    private fun BatchControlBar(
        selectedCount: Int, totalCount: Int, filteredCount: Int,
        targetParam: String, availableParams: List<String>,
        delayMs: Long,
        isSending: Boolean, progress: Int, successCount: Int, failCount: Int,
        pageSize: Int, safePage: Int, totalPages: Int,
        onToggleAll: () -> Unit, onSelectFiltered: () -> Unit, onClearSelection: () -> Unit,
        onParamChange: (String) -> Unit, onDelayChange: (Long) -> Unit,
        onPageChange: (Int) -> Unit, onStartBatch: () -> Unit, onCancelBatch: () -> Unit,
        showDetail: Boolean, onToggleDetail: () -> Unit
    ) {
        var paramExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth().background(AppColors.GlassFrost).padding(10.dp)
        ) {
            // 信息行
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("已选 $selectedCount / $totalCount", color = AppColors.TextSecondary, fontSize = 11.sp)
                Text("结果 $filteredCount 项", color = AppColors.TextSecondary, fontSize = 11.sp)
            }

            // 进度（发送中显示）
            if (isSending) {
                Spacer(Modifier.height(4.dp))
                Text(
                    "$successCount/$failCount · $progress / $selectedCount",
                    color = AppColors.TextPrimary, fontSize = 11.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            // 参数选择（下拉菜单）和间隔
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // 替换参数下拉选择
                Box(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp))
                            .background(AppColors.GlassFill)
                            .clickable { paramExpanded = true }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (targetParam.isBlank()) "替换参数" else targetParam,
                            color = if (targetParam.isBlank()) AppColors.TextHint else AppColors.TextPrimary,
                            fontSize = 11.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Icon(Icons.Default.ExpandMore, null, tint = AppColors.TextHint, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = paramExpanded, onDismissRequest = { paramExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("不替换", fontSize = 12.sp) },
                            onClick = { onParamChange(""); paramExpanded = false }
                        )
                        availableParams.forEach { param ->
                            DropdownMenuItem(
                                text = { Text(param, fontSize = 12.sp) },
                                onClick = { onParamChange(param); paramExpanded = false }
                            )
                        }
                    }
                }
                // 间隔
                Box(modifier = Modifier.width(60.dp).clip(RoundedCornerShape(6.dp)).background(AppColors.GlassFill).padding(horizontal = 8.dp, vertical = 4.dp)) {
                    BasicTextField(
                        value = (delayMs / 1000f).toString(),
                        onValueChange = { v ->
                            v.toFloatOrNull()?.let { onDelayChange((it * 1000).toLong()) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = AppColors.TextPrimary, fontSize = 11.sp),
                        singleLine = true,
                        cursorBrush = SolidColor(AppColors.SystemBlue),
                        decorationBox = { inner ->
                            if (delayMs.toString().isBlank()) Text("间隔(s)", color = AppColors.TextHint, fontSize = 11.sp)
                            inner()
                        }
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            // 分页 + 全选/取消
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ActionButton("全选", onToggleAll)
                    ActionButton("仅选中", onSelectFiltered)
                    ActionButton("清除", onClearSelection)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onPageChange((safePage - 1).coerceAtLeast(0)) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ChevronLeft, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                    Text("${safePage + 1}/$totalPages", color = AppColors.TextSecondary, fontSize = 11.sp)
                    IconButton(onClick = { onPageChange((safePage + 1).coerceAtMost(totalPages - 1)) }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.ChevronRight, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 发送/取消按钮
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isSending) {
                    ActionButton("取消 · $progress/$selectedCount", onCancelBatch, isDestructive = true)
                } else {
                    ActionButton("发送 ($selectedCount)", onStartBatch, isEnabled = selectedCount > 0)
                    ActionButton(if (showDetail) "收起详情" else "协议详情", onToggleDetail)
                }
            }
        }
    }

    @Composable
    private fun ActionButton(text: String, onClick: () -> Unit, isEnabled: Boolean = true, isDestructive: Boolean = false) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    if (isDestructive) AppColors.SystemRed.copy(alpha = 0.15f)
                    else if (isEnabled) AppColors.SystemBlue.copy(alpha = 0.15f)
                    else AppColors.GlassFill
                )
                .clickable(enabled = isEnabled, onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text, fontSize = 11.sp,
                color = if (isDestructive) AppColors.SystemRed else if (isEnabled) AppColors.SystemBlue else AppColors.TextHint
            )
        }
    }

    /**
     * 根据 body 类型正确替换物品代码
     * - URL_ENCODED: 直接替换参数值 (item=46 -> item=99001)
     * - JSON: 替换 JSON 对象中的值
     * - RAW: 替换占位符 {param}
     */
    private fun buildBodyWithItem(
        parsed: ParsedData,
        rawBody: String,
        urlEncodedRows: List<Pair<String, String>>,
        targetParam: String,
        itemCode: String
    ): String {
        if (targetParam.isBlank()) {
            return when (parsed.bodyType) {
                BodyType.URL_ENCODED -> urlEncodedRows.joinToString("&") { "${it.first}=${it.second}" }
                else -> rawBody
            }
        }
        return when (parsed.bodyType) {
            BodyType.URL_ENCODED -> {
                urlEncodedRows.joinToString("&") { (k, v) ->
                    if (k.equals(targetParam, true)) "$k=$itemCode" else "$k=$v"
                }
            }
            BodyType.JSON -> {
                try {
                    val json = org.json.JSONObject(rawBody)
                    if (json.has(targetParam)) {
                        json.put(targetParam, itemCode.toIntOrNull() ?: itemCode)
                    }
                    json.toString()
                } catch (_: Exception) {
                    rawBody.replace("{$targetParam}", itemCode)
                }
            }
            else -> rawBody.replace("{$targetParam}", itemCode)
        }
    }
}