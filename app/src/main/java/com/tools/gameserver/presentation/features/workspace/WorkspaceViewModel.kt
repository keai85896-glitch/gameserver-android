package com.tools.gameserver.presentation.features.workspace

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tools.gameserver.core.util.SnackbarManager
import com.tools.gameserver.data.model.BodyType
import com.tools.gameserver.data.model.ParsedData
import com.tools.gameserver.data.model.ProxyResponse
import com.tools.gameserver.data.model.SendStatus
import com.tools.gameserver.data.service.ProtocolParser
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import com.tools.gameserver.presentation.features.workspace.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class BatchSendResult(
    val itemCode: String,
    val itemName: String,
    val success: Boolean,
    val message: String
)

class WorkspaceViewModel : ViewModel() {

    // ==================== 页面导航 ====================
    private val _currentPage = MutableStateFlow<WorkspacePage>(WorkspacePage.GameList)
    val currentPage: StateFlow<WorkspacePage> = _currentPage.asStateFlow()

    // ==================== 游戏列表 ====================
    private val _localGameEntries = MutableStateFlow<List<LocalGameEntry>>(emptyList())
    val localGameEntries: StateFlow<List<LocalGameEntry>> = _localGameEntries.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ==================== 协议输入/解析 ====================
    private val _rawHeaders = MutableStateFlow("")
    val rawHeaders: StateFlow<String> = _rawHeaders.asStateFlow()

    private val _rawBody = MutableStateFlow("")
    val rawBody: StateFlow<String> = _rawBody.asStateFlow()

    private val _parsedData = MutableStateFlow<ParsedData?>(null)
    val parsedData: StateFlow<ParsedData?> = _parsedData.asStateFlow()

    private val _sendStatus = MutableStateFlow<SendStatus>(SendStatus.Idle)
    val sendStatus: StateFlow<SendStatus> = _sendStatus.asStateFlow()

    private val _proxyResponse = MutableStateFlow<ProxyResponse?>(null)
    val proxyResponse: StateFlow<ProxyResponse?> = _proxyResponse.asStateFlow()

    private val _isParsedExpanded = MutableStateFlow(true)
    val isParsedExpanded: StateFlow<Boolean> = _isParsedExpanded.asStateFlow()

    private val _isResponseExpanded = MutableStateFlow(true)
    val isResponseExpanded: StateFlow<Boolean> = _isResponseExpanded.asStateFlow()

    private val _editableParams = MutableStateFlow<Map<String, String>>(emptyMap())
    val editableParams: StateFlow<Map<String, String>> = _editableParams.asStateFlow()

    private val _loadedFromGameEntry = MutableStateFlow<LocalGameEntry?>(null)
    val loadedFromGameEntry: StateFlow<LocalGameEntry?> = _loadedFromGameEntry.asStateFlow()

    private val _sourceProtocolFile = MutableStateFlow<File?>(null)
    val sourceProtocolFile: StateFlow<File?> = _sourceProtocolFile.asStateFlow()

    // ==================== 物品文件页 ====================
    private val _selectedItemIndices = MutableStateFlow<Set<Int>>(emptySet())
    val selectedItemIndices: StateFlow<Set<Int>> = _selectedItemIndices.asStateFlow()

    private val _itemSearchQuery = MutableStateFlow("")
    val itemSearchQuery: StateFlow<String> = _itemSearchQuery.asStateFlow()

    private val _itemCurrentPage = MutableStateFlow(0)
    val itemCurrentPage: StateFlow<Int> = _itemCurrentPage.asStateFlow()

    val itemPageSize = 20

    // ==================== 批量发送 ====================
    private val _itemsList = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val itemsList: StateFlow<List<Pair<String, String>>> = _itemsList.asStateFlow()

    private val _itemsFileName = MutableStateFlow<String?>(null)
    val itemsFileName: StateFlow<String?> = _itemsFileName.asStateFlow()

    private val _batchProgress = MutableStateFlow(0)
    val batchProgress: StateFlow<Int> = _batchProgress.asStateFlow()

    private val _batchTotal = MutableStateFlow(0)
    val batchTotal: StateFlow<Int> = _batchTotal.asStateFlow()

    private val _isBatchSending = MutableStateFlow(false)
    val isBatchSending: StateFlow<Boolean> = _isBatchSending.asStateFlow()

    private val _batchResults = MutableStateFlow<List<BatchSendResult>>(emptyList())
    val batchResults: StateFlow<List<BatchSendResult>> = _batchResults.asStateFlow()

    private val _batchDelayMs = MutableStateFlow(100L)
    val batchDelayMs: StateFlow<Long> = _batchDelayMs.asStateFlow()

    private val _batchTargetParam = MutableStateFlow("")
    val batchTargetParam: StateFlow<String> = _batchTargetParam.asStateFlow()

    // 可用的替换参数名列表（自动从解析结果中提取）
    val availableParamNames: StateFlow<List<String>> = combine(_parsedData, _editableParams, _rawBody) { parsed, params, rawBody ->
        when {
            parsed?.bodyType == BodyType.URL_ENCODED -> params.keys.toList()
            parsed?.bodyType == BodyType.JSON -> {
                try {
                    org.json.JSONObject(rawBody).keys().asSequence().toList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            else -> emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ==================== 弹窗 ====================
    private val _currentDialog = MutableStateFlow<WorkspaceDialog>(WorkspaceDialog.None)
    val currentDialog: StateFlow<WorkspaceDialog> = _currentDialog.asStateFlow()

    // ==================== 上传预填 ====================
    private val _showUploadDialog = MutableStateFlow(false)
    val showUploadDialog: StateFlow<Boolean> = _showUploadDialog.asStateFlow()

    private val _uploadPrefilledHeaders = MutableStateFlow("")
    val uploadPrefilledHeaders: StateFlow<String> = _uploadPrefilledHeaders.asStateFlow()

    private val _uploadPrefilledBody = MutableStateFlow("")
    val uploadPrefilledBody: StateFlow<String> = _uploadPrefilledBody.asStateFlow()

    private val _uploadPrefilledFiles = MutableStateFlow<List<File>>(emptyList())
    val uploadPrefilledFiles: StateFlow<List<File>> = _uploadPrefilledFiles.asStateFlow()

    private val _uploadPrefilledFolder = MutableStateFlow<File?>(null)
    val uploadPrefilledFolder: StateFlow<File?> = _uploadPrefilledFolder.asStateFlow()

    val createProtocolAuthor = MutableStateFlow("")
    val createProtocolName = MutableStateFlow("")
    val createProtocolHeaders = MutableStateFlow("")
    val createProtocolBody = MutableStateFlow("")

    // ==================== 存储权限 ====================
    private val _showStoragePermissionPrompt = MutableStateFlow(false)
    val showStoragePermissionPrompt: StateFlow<Boolean> = _showStoragePermissionPrompt.asStateFlow()

    init {
        _isLoading.value = true
    }

    fun onScreenReady(context: Context) {
        if (!hasStoragePermission(context)) {
            _showStoragePermissionPrompt.value = true
        }
        refreshGameList()
    }

    fun dismissStoragePermissionPrompt() {
        _showStoragePermissionPrompt.value = false
    }

    // ==================== 页面导航 ====================
    fun navigateToGameList() { _currentPage.value = WorkspacePage.GameList }
    fun navigateToGameDetail(entry: LocalGameEntry) { _currentPage.value = WorkspacePage.GameDetail(entry) }
    fun navigateToItemFile(file: File, items: List<Pair<String, String>>, ownerGame: LocalGameEntry) {
        _selectedItemIndices.value = emptySet()
        _itemSearchQuery.value = ""
        _itemCurrentPage.value = 0
        _currentPage.value = WorkspacePage.ItemFile(file, items, ownerGame)
    }
    fun navigateToProtocolResult() { _currentPage.value = WorkspacePage.ProtocolResult }

    fun navigateBack() {
        when (val page = _currentPage.value) {
            is WorkspacePage.GameDetail -> navigateToGameList()
            is WorkspacePage.ItemFile -> navigateToGameDetail(page.ownerGameEntry)
            is WorkspacePage.ProtocolResult -> {
                val fromGame = _loadedFromGameEntry.value
                _loadedFromGameEntry.value = null
                resetProtocolState()
                if (fromGame != null) {
                    navigateToGameDetail(fromGame)
                } else {
                    navigateToGameList()
                }
            }
            else -> {}
        }
    }

    // ==================== 游戏列表 ====================
    fun refreshGameList() {
        viewModelScope.launch {
            _isLoading.value = true
            val newEntries = withContext(Dispatchers.IO) { scanLocalGameDirectories() }
            _localGameEntries.value = newEntries
            _isLoading.value = false
            _isRefreshing.value = false

            val current = _currentPage.value
            if (current is WorkspacePage.GameDetail) {
                val updated = newEntries.find { it.dir == current.entry.dir }
                if (updated != null) _currentPage.value = WorkspacePage.GameDetail(updated)
                else navigateToGameList()
            }
            if (current is WorkspacePage.ItemFile) {
                val updated = newEntries.find { it.dir == current.ownerGameEntry.dir }
                if (updated != null) _currentPage.value = WorkspacePage.ItemFile(current.file, current.items, updated)
            }
        }
    }

    fun onRefreshTriggered() {
        _isRefreshing.value = true
        refreshGameList()
    }

    fun createSampleDirectory(context: Context) {
        viewModelScope.launch {
            try {
                val baseDir = java.io.File("/storage/emulated/0/游戏私服物品/示例作者/示例游戏")
                baseDir.mkdirs()
                // 创建示例协议文件
                val protocolFile = java.io.File(baseDir, "示例协议.protocol")
                if (!protocolFile.exists()) {
                    protocolFile.writeText(buildString {
                        appendLine("GET /api/example HTTP/1.1")
                        appendLine("Host: example.com")
                        appendLine("Authorization: Bearer your_token_here")
                        appendLine("")
                        appendLine("---BODY---")
                        appendLine("param1=value1&param2=value2")
                    })
                }
                // 创建示例物品文件
                val itemFile = java.io.File(baseDir, "示例物品.txt")
                if (!itemFile.exists()) {
                    itemFile.writeText(buildString {
                        appendLine("物品ID|物品名称|数量|类型")
                        appendLine("1001|金币|99999|货币")
                        appendLine("1002|钻石|500|货币")
                        appendLine("2001|屠龙刀|1|武器")
                        appendLine("2002|凤翅甲|1|防具")
                    })
                }
                SnackbarManager.show("示例目录已创建")
                refreshGameList()
            } catch (e: Exception) {
                SnackbarManager.show("创建失败: ${e.message}")
            }
        }
    }

    // ==================== 协议输入/解析 ====================
    fun updateRawHeaders(value: String) { _rawHeaders.value = value }
    fun updateRawBody(value: String) { _rawBody.value = value }

    fun parseProtocol() {
        if (_rawHeaders.value.isBlank()) {
            _sendStatus.value = SendStatus.Error("请先输入请求头")
            return
        }
        try {
            val parsed = ProtocolParser.parse(_rawHeaders.value, _rawBody.value)
            _parsedData.value = parsed
            _editableParams.value = parsed.bodyParams
            _sendStatus.value = SendStatus.Idle
            navigateToProtocolResult()
        } catch (e: Exception) {
            _sendStatus.value = SendStatus.Error("解析失败: ${e.message}")
        }
    }

    /**
     * 从协议文件内容中提取 headers 和 body
     * 支持两种格式：
     * 1. ---BODY--- 分隔符
     * 2. 标准 HTTP 格式（headers 与 body 之间用空行分隔）
     */
    private fun parseProtocolContent(content: String): Pair<String, String> {
        // 优先检查 ---BODY--- 分隔符
        if (content.contains("---BODY---")) {
            val parts = content.split("---BODY---", limit = 2)
            return parts[0].trim() to (parts.getOrNull(1)?.trim() ?: "")
        }
        // 标准 HTTP：第一个空行之前是 headers，之后是 body
        val lines = content.lines()
        val blankLineIdx = lines.indexOfFirst { it.isBlank() }
        if (blankLineIdx > 0) {
            val headers = lines.subList(0, blankLineIdx).joinToString("\n").trim()
            val body = lines.subList(blankLineIdx + 1, lines.size).joinToString("\n").trim()
            return headers to body
        }
        // 没有空行，整个内容当 headers
        return content.trim() to ""
    }

    fun loadProtocolFromFile(file: File) {
        try {
            val content = file.readText()
            val (headers, body) = parseProtocolContent(content)
            _rawHeaders.value = headers
            _rawBody.value = body
            _sourceProtocolFile.value = file
            parseProtocol()
            SnackbarManager.show("已加载: ${file.nameWithoutExtension}")
        } catch (e: Exception) {
            _sendStatus.value = SendStatus.Error("解析失败: ${e.message}")
        }
    }

    fun loadProtocolAndNavigateItems(protocolFile: File, itemFile: File, items: List<Pair<String, String>>, gameEntry: LocalGameEntry) {
        try {
            // 先设置物品列表和批量状态（确保在 parseProtocol 触发页面跳转前就绑定好）
            _itemsList.value = items
            _itemsFileName.value = itemFile.nameWithoutExtension
            _selectedItemIndices.value = emptySet()
            _batchProgress.value = 0
            _batchTotal.value = 0
            _batchResults.value = emptyList()
            _loadedFromGameEntry.value = gameEntry

            val content = protocolFile.readText()
            val (headers, body) = parseProtocolContent(content)
            _rawHeaders.value = headers
            _rawBody.value = body
            _sourceProtocolFile.value = protocolFile
            parseProtocol()
            SnackbarManager.show("已加载: ${protocolFile.nameWithoutExtension} + ${itemFile.nameWithoutExtension}")
        } catch (e: Exception) {
            _sendStatus.value = SendStatus.Error("解析失败: ${e.message}")
        }
    }

    fun loadItemsContent(content: String) {
        val items = mutableListOf<Pair<String, String>>()
        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) continue
            val parts = trimmed.split(Regex("[,;\\t|]"), limit = 2)
            if (parts.size == 2) items.add(parts[0].trim() to parts[1].trim())
            else if (parts.size == 1 && parts[0].isNotBlank()) items.add(parts[0].trim() to parts[0].trim())
        }
        _itemsList.value = items
        _selectedItemIndices.value = emptySet()
        _batchProgress.value = 0
        _batchTotal.value = 0
        _batchResults.value = emptyList()
        SnackbarManager.show("已加载 ${items.size} 个物品代码")
    }

    fun initFromPrefill(headers: String, body: String, file: File? = null) {
        _rawHeaders.value = headers
        _rawBody.value = body
        _sourceProtocolFile.value = file
        if (headers.isNotBlank()) parseProtocol()
    }

    fun updateEditableParam(key: String, value: String) {
        _editableParams.value = _editableParams.value.toMutableMap().apply { put(key, value) }
    }

    fun saveProtocol() {
        val parsed = _parsedData.value ?: return
        try {
            val rawPath = try { java.net.URI(parsed.url).let { it.path + if (it.query != null) "?" + it.query else "" } } catch (_: Exception) { "/" }
            val headerLines = mutableListOf("${parsed.method} $rawPath HTTP/1.1")
            parsed.headers.forEach { (key, value) -> headerLines.add("$key: $value") }
            val headerText = headerLines.joinToString("\n")
            val bodyText = when (parsed.bodyType) {
                BodyType.URL_ENCODED -> parsed.bodyParams.entries.joinToString("&") { entry ->
                    "${java.net.URLEncoder.encode(entry.key, "UTF-8")}=${java.net.URLEncoder.encode(entry.value, "UTF-8")}"
                }
                else -> parsed.rawBody
            }
            val content = if (bodyText.isNotBlank()) "$headerText\n---BODY---\n$bodyText" else headerText
            val file = _sourceProtocolFile.value ?: File("/storage/emulated/0/游戏私服物品", "protocol_${System.currentTimeMillis()}.protocol")
            file.parentFile?.mkdirs()
            file.writeText(content)
            _sourceProtocolFile.value = file
            SnackbarManager.show("已保存: ${file.nameWithoutExtension}")
        } catch (e: Exception) {
            SnackbarManager.show("保存失败: ${e.message}")
        }
    }

    // ==================== 发送请求 ====================
    fun sendRequest() {
        val parsed = _parsedData.value ?: return
        viewModelScope.launch {
            _sendStatus.value = SendStatus.Loading()
            try {
                val bodyToSend = when (parsed.bodyType) {
                    BodyType.URL_ENCODED -> {
                        val updatedParsed = parsed.copy(bodyParams = _editableParams.value)
                        ProtocolParser.rebuildRawBody(updatedParsed)
                    }
                    else -> _rawBody.value  // 使用编辑后的最新 body
                }
                val result = withContext(Dispatchers.IO) {
                    ApiClient.proxyDirect(
                        targetUrl = parsed.url,
                        headers = parsed.headers,
                        rawBody = bodyToSend,
                        method = parsed.method,
                        bodyType = parsed.bodyType
                    )
                }
                when (result) {
                    is ApiResult.Success -> {
                        _sendStatus.value = SendStatus.Success("发送成功")
                        _proxyResponse.value = ProxyResponse(result.code, "OK", emptyMap(), result.body, "direct", System.currentTimeMillis(), 0)
                    }
                    is ApiResult.Error -> {
                        _sendStatus.value = SendStatus.Error("请求失败: ${result.message}")
                    }
                }
            } catch (e: Exception) {
                _sendStatus.value = SendStatus.Error("请求失败: ${e.message ?: "未知错误"}")
            }
        }
    }

    // ==================== 批量发送 ====================
    fun startBatchSend(selectedIndices: Set<Int>) {
        val parsed = _parsedData.value ?: return
        val items = _itemsList.value
        if (selectedIndices.isEmpty() || items.isEmpty()) return

        _isBatchSending.value = true
        _batchProgress.value = 0
        _batchTotal.value = selectedIndices.size
        val results = mutableListOf<BatchSendResult>()
        _batchResults.value = results

        viewModelScope.launch {
            val sortedIndices = selectedIndices.sorted()
            var successCount = 0
            var failCount = 0
            var sentCount = 0
            for (idx in sortedIndices) {
                ensureActive()
                if (!_isBatchSending.value) break
                val (code, itemName) = items[idx]
                val bodyToSend = buildBodyWithItem(parsed, _editableParams.value, _rawBody.value, _batchTargetParam.value, code)
                try {
                    val result = withContext(Dispatchers.IO) {
                        ApiClient.proxyDirect(parsed.url, parsed.headers, bodyToSend, parsed.method, parsed.bodyType)
                    }
                    if (result is ApiResult.Success) {
                        successCount++
                        results.add(BatchSendResult(code, itemName, true, "成功"))
                    } else {
                        failCount++
                        results.add(BatchSendResult(code, itemName, false, (result as? ApiResult.Error)?.message ?: "失败"))
                    }
                } catch (e: Exception) {
                    failCount++
                    results.add(BatchSendResult(code, itemName, false, e.message ?: "异常"))
                }
                sentCount++
                _batchProgress.value = sentCount
                _batchResults.value = results.toList()
                delay(_batchDelayMs.value)
            }
            _isBatchSending.value = false
            val summary = if (!_isBatchSending.value && sentCount < selectedIndices.size) {
                "已取消: 成功 $successCount / 失败 $failCount / 已发 $sentCount / 共 ${selectedIndices.size}"
            } else {
                "批量完成: 成功 $successCount / 失败 $failCount / 共 ${selectedIndices.size}"
            }
            SnackbarManager.show(summary)
        }
    }

    /**
     * 根据 body 类型正确替换物品代码
     * - URL_ENCODED: 替换参数值 (item=46 -> item=99001)
     * - JSON: 替换 JSON 对象中的值
     * - RAW/其他: 替换占位符 {param}
     */
    private fun buildBodyWithItem(
        parsed: ParsedData,
        editableParams: Map<String, String>,
        rawBody: String,
        targetParam: String,
        itemCode: String
    ): String {
        if (targetParam.isBlank()) {
            return when (parsed.bodyType) {
                BodyType.URL_ENCODED -> ProtocolParser.rebuildRawBody(parsed.copy(bodyParams = editableParams))
                else -> rawBody
            }
        }
        
        return when (parsed.bodyType) {
            BodyType.URL_ENCODED -> {
                val params = editableParams.toMutableMap()
                if (params.containsKey(targetParam)) {
                    params[targetParam] = itemCode
                }
                ProtocolParser.rebuildRawBody(parsed.copy(bodyParams = params))
            }
            BodyType.JSON -> {
                try {
                    val json = org.json.JSONObject(rawBody)
                    if (json.has(targetParam)) {
                        json.put(targetParam, itemCode.toIntOrNull() ?: itemCode)
                    }
                    json.toString()
                } catch (e: Exception) {
                    rawBody.replace("{$targetParam}", itemCode)
                }
            }
            else -> rawBody.replace("{$targetParam}", itemCode)
        }
    }

    fun cancelBatchSend() {
        _isBatchSending.value = false
    }

    fun updateBatchDelay(ms: Long) { _batchDelayMs.value = ms }
    fun updateBatchTargetParam(param: String) { _batchTargetParam.value = param }

    fun retryFailedBatchItems() {
        val failedCodes = _batchResults.value.filter { !it.success }.map { it.itemCode }.toSet()
        if (failedCodes.isEmpty()) {
            SnackbarManager.show("没有失败项需要重试")
            return
        }
        val failedIndices = _itemsList.value.mapIndexedNotNull { index, (code, name) ->
            if (code in failedCodes) index else null
        }.toSet()
        if (failedIndices.isEmpty()) {
            SnackbarManager.show("无法定位失败项")
            return
        }
        // 清除旧结果中失败项，保留成功的
        _batchResults.value = _batchResults.value.filter { it.success }
        SnackbarManager.show("重试 ${failedIndices.size} 个失败项...")
        startBatchSend(failedIndices)
    }

    fun selectAll() { _selectedItemIndices.value = _itemsList.value.indices.toSet() }
    fun selectAll(indices: Set<Int>) { _selectedItemIndices.value = _selectedItemIndices.value + indices }
    fun selectAllFiltered(filteredIndices: List<Int>) { _selectedItemIndices.value = filteredIndices.toSet() }
    fun clearSelection() { _selectedItemIndices.value = emptySet() }
    fun clearSelection(indices: Set<Int>) { _selectedItemIndices.value = _selectedItemIndices.value - indices }
    fun toggleItem(index: Int) {
        _selectedItemIndices.value = if (_selectedItemIndices.value.contains(index)) _selectedItemIndices.value - index
        else _selectedItemIndices.value + index
    }
    fun updateItemSearch(query: String) { _itemSearchQuery.value = query; _itemCurrentPage.value = 0 }
    fun updateItemPage(page: Int) { _itemCurrentPage.value = page }

    // ==================== 保存物品到文件 ====================
    fun saveItemsToFile(items: List<Pair<String, String>>, fileName: String) {
        try {
            val dir = File("/storage/emulated/0/游戏私服物品")
            dir.mkdirs()
            val outFile = File(dir, "$fileName.txt")
            outFile.writeText(items.joinToString("\n") { (code, name) -> "$code|$name" })
            SnackbarManager.show("已保存到 ${outFile.name} (${items.size} 个)")
        } catch (e: Exception) {
            SnackbarManager.show("保存失败: ${e.message}")
        }
    }

    // ==================== 删除 ====================
    fun deleteFile(file: File) {
        try {
            val renamed = File(file.parent, ".deleting_${System.currentTimeMillis()}_${file.name}")
            if (file.renameTo(renamed)) renamed.delete() else file.delete()
            SnackbarManager.show("已删除: ${file.name}")
            refreshGameList()
        } catch (e: Exception) {
            SnackbarManager.show("删除失败: ${e.message}")
        }
    }

    fun deleteGameDir(entry: LocalGameEntry) {
        try {
            val dir = entry.dir
            val renamed = File(dir.parent, ".deleting_${System.currentTimeMillis()}")
            if (dir.renameTo(renamed)) renamed.deleteRecursively() else dir.deleteRecursively()
            SnackbarManager.show("已删除: ${entry.gameName}")
            refreshGameList()
        } catch (e: Exception) {
            SnackbarManager.show("删除失败: ${e.message}")
        }
    }

    fun createProtocolFile(author: String, game: String, name: String) {
        try {
            val dir = File("/storage/emulated/0/游戏私服物品/$author/$game")
            dir.mkdirs()
            val file = File(dir, "$name.protocol")
            file.createNewFile()
            SnackbarManager.show("已创建: ${name}.protocol")
            refreshGameList()
        } catch (e: Exception) {
            SnackbarManager.show("创建失败: ${e.message}")
        }
    }

    fun clearCreateProtocolFields() {
        createProtocolAuthor.value = ""
        createProtocolName.value = ""
        createProtocolHeaders.value = ""
        createProtocolBody.value = ""
    }

    // ==================== 弹窗 ====================
    fun showDialog(dialog: WorkspaceDialog) { _currentDialog.value = dialog }
    fun dismissDialog() { _currentDialog.value = WorkspaceDialog.None }

    // ==================== 添加物品文件 ====================
    fun copyFilesToGameDir(uris: List<Uri>, gameDir: File, context: Context) {
        if (!hasStoragePermission(context)) {
            showDialog(WorkspaceDialog.StoragePermission)
            return
        }
        if (uris.isEmpty()) return
        viewModelScope.launch {
            var successCount = 0
            var failCount = 0
            withContext(Dispatchers.IO) {
                for (uri in uris) {
                    try {
                        val fileName = getFileNameFromUri(uri, context) ?: "item_${System.currentTimeMillis()}.txt"
                        val dest = File(gameDir, fileName)
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            dest.outputStream().use { output -> input.copyTo(output) }
                        }
                        successCount++
                    } catch (e: Exception) {
                        failCount++
                    }
                }
            }
            when {
                failCount == 0 -> SnackbarManager.show("$successCount 个文件已添加")
                successCount > 0 -> SnackbarManager.show("$successCount 个成功，$failCount 个失败")
                else -> SnackbarManager.show("$failCount 个文件添加失败")
            }
            refreshGameList()
        }
    }

    fun openUploadDialog(entry: LocalGameEntry) {
        val firstProtocolFile = entry.protocolFiles.firstOrNull() ?: return
        val parsed = parseLocalProtocolFile(firstProtocolFile) ?: return
        val headers = parsed.first
        val body = parsed.second
        _uploadPrefilledHeaders.value = headers
        _uploadPrefilledBody.value = body
        _uploadPrefilledFiles.value = entry.itemFiles.toList()
        _uploadPrefilledFolder.value = entry.dir
        _showUploadDialog.value = true
    }

    fun closeUploadDialog() {
        _showUploadDialog.value = false
        _uploadPrefilledFolder.value = null
    }

    // ==================== 工具方法 ====================
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            @Suppress("DEPRECATION")
            context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun openStorageSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.startActivity(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:${context.packageName}")
            })
        }
    }

    fun isNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun resetProtocolState() {
        _rawHeaders.value = ""
        _rawBody.value = ""
        _parsedData.value = null
        _editableParams.value = emptyMap()
        _sendStatus.value = SendStatus.Idle
        _proxyResponse.value = null
        _isParsedExpanded.value = true
        _isResponseExpanded.value = true
        _sourceProtocolFile.value = null
    }

    private fun parseLocalProtocolFile(file: File): Pair<String, String>? {
        return try {
            val content = file.readText()
            parseProtocolContent(content)
        } catch (e: Exception) {
            Log.e("WorkspaceVM", "解析本地协议文件失败: ${e.message}")
            null
        }
    }

    private fun getFileNameFromUri(uri: Uri, context: Context): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return uri.lastPathSegment
    }

    /**
     * 从 Intent 预填数据（WorkspaceScreen 中调用）
     */
    fun initFromIntent(context: Context, headers: String?, body: String?, file: File? = null) {
        if (!headers.isNullOrBlank()) {
            _rawHeaders.value = headers
            if (!body.isNullOrBlank()) _rawBody.value = body
            _sourceProtocolFile.value = file
            parseProtocol()
        }
        if (!body.isNullOrBlank() && headers.isNullOrBlank()) {
            loadItemsContent(body)
        }
    }
}