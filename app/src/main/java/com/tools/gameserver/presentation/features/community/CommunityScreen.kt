package com.tools.gameserver.presentation.features.community

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.tools.gameserver.data.model.CommunityPost
import com.tools.gameserver.data.repository.CommunityRepository
import com.tools.gameserver.data.service.api.AuthManager
import com.tools.gameserver.presentation.common.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** 社区页面入口 */
@Composable
fun CommunityScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var detailPost by remember { mutableStateOf<CommunityPost?>(null) }
    var isDetailLoading by remember { mutableStateOf(false) }

    BackHandler(enabled = selectedPostId != null) { selectedPostId = null; detailPost = null }

    val userInfo by AuthManager.userState.collectAsState()
    var showUploadTypeDialog by remember { mutableStateOf(false) }
    var selectedUploadIntent by remember { mutableStateOf(UploadIntent.PROTOCOL) }
    var showUploadDialog by remember { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }

    var rawSearchQuery by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    LaunchedEffect(rawSearchQuery) { delay(300L); searchQuery = rawSearchQuery }

    var pendingDownloadPost by remember { mutableStateOf<CommunityPost?>(null) }
    var isDownloadingPost by remember { mutableStateOf(false) }

    val storagePermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        pendingDownloadPost?.let { post ->
            if (hasStoragePermission(context)) {
                scope.launch {
                    isDownloadingPost = true
                    try {
                        val ok = withContext(Dispatchers.IO) { downloadProtocolFile(post) }
                    if (ok) SnackbarManager.show("下载完成") else SnackbarManager.show("下载失败")
                } catch (e: Exception) { SnackbarManager.show("下载失败: ${e.message}") }
                    isDownloadingPost = false
                }
            }
        }
    }

    if (selectedPostId != null && detailPost != null) {
        // ========== 详情页 ==========
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { selectedPostId = null; detailPost = null }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = AppColors.TextPrimary) }
                Text(detailPost!!.name, color = AppColors.TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f).padding(start = 4.dp))
            }
            if (isDetailLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.SystemBlue) }
            } else {
                val post = detailPost!!
                LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item { Spacer(Modifier.height(8.dp)) }
                    item {
                        GlassCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(post.name, color = AppColors.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text("作者: ${post.author.ifBlank { "匿名" }}", color = AppColors.TextSecondary, fontSize = 13.sp)
                                if (post.description.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(post.description, color = AppColors.TextSecondary, fontSize = 14.sp, lineHeight = 20.sp) }
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(post.method.ifBlank { "POST" }, color = AppColors.SystemBlue, fontSize = 12.sp,
                                        modifier = Modifier.background(AppColors.SystemBlue.copy(alpha = 0.1f), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp))
                                    if (post.url.isNotBlank()) Text(post.url, color = AppColors.TextHint, fontSize = 12.sp, maxLines = 1)
                                }
                                if (post.tags.isNotEmpty()) {
                                    Spacer(Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        post.tags.take(5).forEach { tag ->
                                            Text(tag, color = AppColors.AccentViolet, fontSize = 11.sp,
                                                modifier = Modifier.background(AppColors.AccentViolet.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (post.rawHeaders.isNotBlank()) {
                        item {
                            GlassCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("请求头", color = AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(post.rawHeaders, color = AppColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                    if (post.rawBody.isNotBlank()) {
                        item {
                            GlassCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text("请求体", color = AppColors.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(6.dp))
                                    Text(post.rawBody, color = AppColors.TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                                }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { /* TODO: load to workspace */ }, modifier = Modifier.weight(1f)) { Text("加载到工作台") }
                            OutlinedButton(onClick = {
                                scope.launch {
                                    if (!hasStoragePermission(context)) {
                                        pendingDownloadPost = post
                                        try { storagePermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)) } catch (_: Exception) {}
                                    } else {
                                        val ok = withContext(Dispatchers.IO) { downloadProtocolFile(post) }
                                        if (ok) SnackbarManager.show("下载完成") else SnackbarManager.show("下载失败")
                                    }
                                }
                            }, modifier = Modifier.weight(1f)) { Text("下载到本地") }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    } else {
        // ========== 列表页 ==========
        var posts by remember { mutableStateOf<List<CommunityPost>>(emptyList()) }
        var isLoadingPosts by remember { mutableStateOf(true) }
        var loadError by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(searchQuery, refreshTrigger) {
            isLoadingPosts = true; loadError = null
            val result = withContext(Dispatchers.IO) { CommunityRepository.loadPosts(search = searchQuery) }
            result.onSuccess { posts = it }.onFailure { loadError = it.message }
            isLoadingPosts = false
        }

        Column(Modifier.fillMaxSize().background(AppColors.BackgroundPrimary)) {
            // Hero 区域 — 液态玻璃渐变（含搜索栏）
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(AppColors.SystemBlue.copy(alpha = 0.08f), Color.Transparent)))) {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("社区", color = AppColors.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f).padding(start = 4.dp, top = 8.dp, bottom = 8.dp))
                        if (userInfo != null) IconButton(onClick = { showUploadTypeDialog = true }) { Icon(Icons.Default.CloudUpload, "上传", tint = AppColors.TextPrimary) }
                    }
                    // 搜索栏嵌入Hero
                    OutlinedTextField(
                        value = rawSearchQuery,
                        onValueChange = { rawSearchQuery = it },
                        placeholder = { Text("搜索社区资源...", color = AppColors.TextHint) },
                        leadingIcon = { Icon(Icons.Default.Search, null, tint = AppColors.TextHint, modifier = Modifier.size(20.dp)) },
                        trailingIcon = { if (rawSearchQuery.isNotEmpty()) IconButton(onClick = { rawSearchQuery = "" }) { Icon(Icons.Default.Close, "清除", tint = AppColors.TextHint, modifier = Modifier.size(18.dp)) } },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AppColors.SystemBlue, unfocusedBorderColor = AppColors.SeparatorLight, focusedTextColor = AppColors.TextPrimary, unfocusedTextColor = AppColors.TextPrimary, cursorColor = AppColors.SystemBlue)
                    )
                }
            }

            if (isLoadingPosts) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = AppColors.SystemBlue) }
            } else if (loadError != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败", color = AppColors.TextSecondary, fontSize = 16.sp)
                        Text(loadError!!, color = AppColors.TextHint, fontSize = 13.sp)
                        TextButton(onClick = { refreshTrigger++ }) { Text("重试", color = AppColors.SystemBlue) }
                    }
                }
            } else if (posts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Explore, null, tint = AppColors.TextHint.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("暂无内容", color = AppColors.TextSecondary, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(posts, key = { it.id }) { post ->
                        GlassCard(Modifier.fillMaxWidth().clickable { selectedPostId = post.id; isDetailLoading = true; scope.launch {
                            val r = withContext(Dispatchers.IO) { CommunityRepository.getPost(post.id) }
                            r.onSuccess { detailPost = it }.onFailure { SnackbarManager.show("加载失败") }
                            isDetailLoading = false
                        }}) {
                            Column(Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(post.method.ifBlank { if (post.postType == "file") "文件" else "POST" }, color = AppColors.SystemBlue, fontSize = 11.sp,
                                        modifier = Modifier.background(AppColors.SystemBlue.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(post.name, color = AppColors.TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                                }
                                if (post.description.isNotBlank()) { Spacer(Modifier.height(4.dp)); Text(post.description, color = AppColors.TextSecondary, fontSize = 13.sp, maxLines = 2) }
                                if (post.files.isNotEmpty()) {
                                    Spacer(Modifier.height(6.dp))
                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        post.files.take(3).forEach { file ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.FolderZip, null, tint = AppColors.SystemPurple, modifier = Modifier.size(12.dp))
                                                Spacer(Modifier.width(4.dp))
                                                Text(file.name, color = AppColors.TextSecondary, fontSize = 11.sp, maxLines = 1)
                                            }
                                        }
                                        if (post.files.size > 3) Text("...还有 ${post.files.size - 3} 个文件", color = AppColors.TextHint, fontSize = 11.sp)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("作者: ${post.author.ifBlank { "匿名" }}", color = AppColors.TextHint, fontSize = 12.sp)
                                    Text("↓ ${post.downloads}", color = AppColors.TextHint, fontSize = 12.sp)
                                    if (post.fileCount > 0) Text("${post.fileCount} 文件", color = AppColors.SystemPurple, fontSize = 11.sp,
                                        modifier = Modifier.background(AppColors.SystemPurple.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                    if (post.tags.isNotEmpty()) Text(post.tags.first(), color = AppColors.AccentViolet, fontSize = 11.sp,
                                        modifier = Modifier.background(AppColors.AccentViolet.copy(alpha = 0.1f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }

        // 上传类型选择弹窗
        if (showUploadTypeDialog) {
            AlertDialog(
                onDismissRequest = { showUploadTypeDialog = false },
                title = { Text("选择上传类型", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        UploadTypeOption(Icons.Default.CloudUpload, "上传协议", "分享您的协议配置") { selectedUploadIntent = UploadIntent.PROTOCOL; showUploadTypeDialog = false; showUploadDialog = true }
                        UploadTypeOption(Icons.Default.FolderZip, "上传文件", "上传资源文件") { selectedUploadIntent = UploadIntent.RESOURCE; showUploadTypeDialog = false; showUploadDialog = true }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { showUploadTypeDialog = false }) { Text("取消", color = AppColors.TextHint) } },
                containerColor = AppColors.BackgroundCard, shape = RoundedCornerShape(18.dp)
            )
        }

        if (showUploadDialog) {
            UnifiedUploadDialog(visible = showUploadDialog, onDismiss = { showUploadDialog = false }, onSuccess = { refreshTrigger++; showUploadDialog = false }, uploadIntent = selectedUploadIntent)
        }
    }
}

private suspend fun downloadProtocolFile(post: CommunityPost): Boolean {
    return try {
        val baseDir = java.io.File("/storage/emulated/0/游戏私服物品/${post.author}/${post.name}")
        baseDir.mkdirs()
        val protocolFile = java.io.File(baseDir, "${post.name}.protocol")
        val content = buildString {
            if (post.url.isNotBlank()) appendLine("url=${post.url}")
            if (post.method.isNotBlank()) appendLine("method=${post.method}")
            if (post.rawHeaders.isNotBlank()) appendLine("headers=${post.rawHeaders}")
            if (post.rawBody.isNotBlank()) appendLine("body=${post.rawBody}")
        }
        protocolFile.writeText(content)
        true
    } catch (_: Exception) { false }
}

@Composable
private fun UploadTypeOption(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).background(AppColors.BackgroundSecondary.copy(alpha = 0.45f), RoundedCornerShape(14.dp)).padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(Brush.linearGradient(listOf(AppColors.SystemBlue.copy(alpha = 0.18f), AppColors.AccentViolet.copy(alpha = 0.16f))), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = AppColors.SystemBlue)
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(subtitle, color = AppColors.TextHint, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}