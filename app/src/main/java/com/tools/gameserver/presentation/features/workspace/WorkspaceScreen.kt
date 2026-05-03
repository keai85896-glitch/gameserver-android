package com.tools.gameserver.presentation.features.workspace

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.util.SnackbarManager
import com.tools.gameserver.presentation.common.GlassCard
import com.tools.gameserver.presentation.common.PrimaryButton
import com.tools.gameserver.presentation.features.workspace.components.LocalGameCard
import com.tools.gameserver.presentation.features.workspace.components.WorkspaceDialogs
import com.tools.gameserver.presentation.features.workspace.model.*
import com.tools.gameserver.presentation.features.workspace.pages.GameDetailPage
import com.tools.gameserver.presentation.features.workspace.pages.ItemFilePage
import com.tools.gameserver.presentation.features.workspace.pages.ProtocolResultPage
import com.tools.gameserver.presentation.features.workspace.parseItemFile
import java.io.File

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WorkspaceScreen(
    modifier: Modifier = Modifier,
    prefilledHeaders: String? = null,
    prefilledBody: String? = null,
    prefilledItemsContent: String? = null,
    initialGameDirPath: String? = null,
    initialOpenTargetType: String? = null,
    initialOpenTargetPath: String? = null
) {
    val context = LocalContext.current
    val viewModel = remember { WorkspaceViewModel() }

    val currentPage by viewModel.currentPage.collectAsState()
    val localGameEntries by viewModel.localGameEntries.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val currentDialog by viewModel.currentDialog.collectAsState()
    val showStoragePrompt by viewModel.showStoragePermissionPrompt.collectAsState()
    val hasAutoOpenedTarget = remember { mutableStateOf(false) }

    // 文件选择器 — 用于添加物品文件到游戏目录
    val pendingAddItemDir = remember { mutableStateOf<File?>(null) }
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        val dir = pendingAddItemDir.value
        if (dir != null && uris.isNotEmpty()) {
            viewModel.copyFilesToGameDir(uris, dir, context)
        }
        pendingAddItemDir.value = null
    }

    // 触发带 MIME 过滤的文件选择
    fun launchFilePicker(dir: File) {
        pendingAddItemDir.value = dir
        filePickerLauncher.launch(arrayOf("text/*", "application/octet-stream"))
    }

    // ItemFile 页状态
    val selectedIndices by viewModel.selectedItemIndices.collectAsState()
    val itemSearchQuery by viewModel.itemSearchQuery.collectAsState()
    val itemCurrentPage by viewModel.itemCurrentPage.collectAsState()

    BackHandler(enabled = currentPage !is WorkspacePage.GameList) { viewModel.navigateBack() }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        SnackbarManager.messages.collect { msg -> snackbarHostState.showSnackbar(message = msg.message, actionLabel = msg.actionLabel, duration = msg.duration) }
    }

    LaunchedEffect(prefilledHeaders, prefilledBody, prefilledItemsContent) {
        val file = if (!prefilledItemsContent.isNullOrBlank()) File(context.cacheDir, "prefill_items.txt").also { it.writeText(prefilledItemsContent!!) } else null
        viewModel.initFromPrefill(prefilledHeaders ?: "", prefilledBody ?: "", file)
    }

    LaunchedEffect(Unit) { viewModel.onScreenReady(context) }

    // 存储权限提示 → 自动弹出权限弹窗
    LaunchedEffect(showStoragePrompt) {
        if (showStoragePrompt) {
            viewModel.showDialog(WorkspaceDialog.StoragePermission)
            viewModel.dismissStoragePermissionPrompt()
        }
    }

    LaunchedEffect(initialGameDirPath, initialOpenTargetType, initialOpenTargetPath, localGameEntries, isLoading) {
        if (!hasAutoOpenedTarget.value && !isLoading && localGameEntries.isNotEmpty()) {
            val targetEntry = when {
                !initialGameDirPath.isNullOrBlank() -> localGameEntries.firstOrNull { it.dir.absolutePath == initialGameDirPath }
                !initialOpenTargetPath.isNullOrBlank() -> localGameEntries.firstOrNull { entry ->
                    entry.protocolFiles.any { it.absolutePath == initialOpenTargetPath } || entry.itemFiles.any { it.absolutePath == initialOpenTargetPath }
                }
                else -> null
            }
            if (targetEntry != null) {
                hasAutoOpenedTarget.value = true
                when (initialOpenTargetType) {
                    "protocol" -> {
                        val f = targetEntry.protocolFiles.firstOrNull { it.absolutePath == initialOpenTargetPath }
                        if (f != null) viewModel.loadProtocolFromFile(f)
                    }
                    "item" -> {
                        val f = targetEntry.itemFiles.firstOrNull { it.absolutePath == initialOpenTargetPath }
                        if (f != null) viewModel.navigateToItemFile(f, parseItemFile(f), targetEntry)
                    }
                    else -> viewModel.navigateToGameDetail(targetEntry)
                }
            }
        }
    }

    // 液态玻璃 Hero 渐变色
    val heroGradient = Brush.verticalGradient(
        listOf(AppColors.SystemBlue.copy(alpha = 0.08f), Color.Transparent)
    )

    Box(modifier = modifier.fillMaxSize().background(AppColors.BackgroundPrimary)) {
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                (slideInHorizontally(tween(350, easing = FastOutSlowInEasing)) { w -> w / 3 } + fadeIn(tween(250)))
                    .togetherWith(slideOutHorizontally(tween(350)) { w -> -w / 3 } + fadeOut(tween(250)))
            },
            label = "workspace_nav"
        ) { page ->
            when (page) {
                is WorkspacePage.GameList -> {
                    val pullRefreshState = rememberPullRefreshState(isRefreshing, { viewModel.onRefreshTriggered() })
                    Box(Modifier.fillMaxSize().pullRefresh(pullRefreshState)) {
                        Column(Modifier.fillMaxSize()) {
                            // Hero 区域 — 液态玻璃渐变
                            Box(
                                Modifier.fillMaxWidth().background(heroGradient)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "工作台",
                                        color = AppColors.TextPrimary,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f).padding(start = 12.dp, top = 8.dp, bottom = 8.dp)
                                    )
                                    IconButton(onClick = { viewModel.showDialog(WorkspaceDialog.Help) }) {
                                        Icon(Icons.AutoMirrored.Filled.HelpOutline, "帮助", tint = AppColors.TextHint)
                                    }
                                }
                            }
                            if (isLoading && localGameEntries.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        CircularProgressIndicator(color = AppColors.SystemBlue)
                                        Spacer(Modifier.height(12.dp))
                                        Text("正在扫描本地游戏...", color = AppColors.TextSecondary, fontSize = 14.sp)
                                    }
                                }
                            } else if (localGameEntries.isEmpty()) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(horizontal = 32.dp)
                                    ) {
                                        Icon(Icons.Default.Folder, null, tint = AppColors.TextHint.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                                        Spacer(Modifier.height(16.dp))
                                        Text("暂无本地游戏目录", color = AppColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                        Spacer(Modifier.height(8.dp))
                                        Text("将游戏目录放在指定路径即可自动识别", color = AppColors.TextSecondary, fontSize = 14.sp)
                                        Spacer(Modifier.height(24.dp))
                                        PrimaryButton(
                                            text = "创建示例目录",
                                            onClick = { viewModel.createSampleDirectory(context) },
                                            modifier = Modifier.height(44.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        TextButton(onClick = { viewModel.showDialog(WorkspaceDialog.CreateProtocol) }) { 
                                            Text("新建协议文件", color = AppColors.SystemBlue, fontSize = 14.sp) 
                                        }
                                        Spacer(Modifier.height(16.dp))
                                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                                            Column(
                                                modifier = Modifier.padding(14.dp),
                                                verticalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text("目录结构示例", color = AppColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                Text("/storage/emulated/0/游戏私服物品/", color = AppColors.SystemBlue, fontSize = 12.sp)
                                                Text("  └── 作者名/", color = AppColors.TextHint, fontSize = 12.sp)
                                                Text("      └── 游戏名/", color = AppColors.TextHint, fontSize = 12.sp)
                                                Text("          ├── *.protocol", color = AppColors.SystemGreen, fontSize = 12.sp)
                                                Text("          └── *.txt (物品)", color = AppColors.SystemPurple, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    items(localGameEntries, key = { it.dir.absolutePath }) { entry ->
                                        LocalGameCard(
                                            entry = entry,
                                            onClick = { viewModel.navigateToGameDetail(entry) },
                                            onLongClick = { viewModel.showDialog(WorkspaceDialog.GameLongPressMenu(entry)) }
                                        )
                                    }
                                    item { Spacer(Modifier.height(80.dp)) }
                                }
                            }
                        }
                        PullRefreshIndicator(isRefreshing, pullRefreshState, Modifier.align(Alignment.TopCenter), contentColor = AppColors.SystemBlue)
                        FloatingActionButton(
                            onClick = { viewModel.onRefreshTriggered() },
                            containerColor = AppColors.SystemBlue,
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(56.dp)
                        ) { Icon(Icons.Default.Refresh, "刷新", tint = AppColors.White) }
                    }
                }
                is WorkspacePage.GameDetail -> GameDetailPage(
                    entry = page.entry,
                    onBack = { viewModel.navigateBack() },
                    onProtocolFileClick = { viewModel.loadProtocolFromFile(it) },
                    onItemFileClick = { file, items, gameEntry -> viewModel.navigateToItemFile(file, items, gameEntry) },
                    onItemWithProtocol = { itemFile, items, gameEntry, protocolFile -> viewModel.loadProtocolAndNavigateItems(protocolFile, itemFile, items, gameEntry) },
                    onFileLongPress = { viewModel.showDialog(WorkspaceDialog.DeleteFile(it)) },
                    onRefreshEntries = { viewModel.refreshGameList() }
                )
                is WorkspacePage.ProtocolResult -> ProtocolResultPage(
                    viewModel = viewModel
                )
                is WorkspacePage.ItemFile -> ItemFilePage(
                    file = page.file,
                    items = page.items,
                    ownerGameEntry = page.ownerGameEntry,
                    selectedIndices = selectedIndices,
                    searchQuery = itemSearchQuery,
                    currentPage = itemCurrentPage,
                    pageSize = viewModel.itemPageSize,
                    onToggleSelection = { viewModel.toggleItem(it) },
                    onSelectAll = { viewModel.selectAll(it) },
                    onDeselectAll = { viewModel.clearSelection(it) },
                    onSearchQueryChange = { viewModel.updateItemSearch(it) },
                    onPageChange = { viewModel.updateItemPage(it) },
                    onSaveSelected = { name, items -> viewModel.saveItemsToFile(items, name, page.ownerGameEntry) },
                    onConfirmSelection = { viewModel.navigateBack() },
                    onBack = { viewModel.navigateBack() }
                )
            }
        }

        WorkspaceDialogs(
        currentDialog = currentDialog,
        viewModel = viewModel,
        onAddItemFileConfirm = { entry ->
            launchFilePicker(entry.dir)
        }
    )

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}