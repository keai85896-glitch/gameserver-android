package com.tools.gameserver.data.service.api

import android.util.Log
import com.tools.gameserver.core.constants.AppConstants
import com.tools.gameserver.core.util.UserAgentGenerator
import com.tools.gameserver.data.model.BodyType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream
import java.io.ByteArrayInputStream
import org.json.JSONObject
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val TAG = "ApiClient"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(AppConstants.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    private var tokenProvider: (() -> String?) = { null }

    fun init(provider: () -> String?) {
        tokenProvider = provider
    }

    // ==================== GET ====================
    suspend fun get(path: String, params: Map<String, String> = emptyMap()): ApiResult {
        return withContext(Dispatchers.IO) {
            val baseUrl = AppConstants.API_BASE_URL.trimEnd('/')
            val urlBuilder = StringBuilder("$baseUrl/api.php/$path")
            if (params.isNotEmpty()) {
                urlBuilder.append("?")
                urlBuilder.append(params.entries.joinToString("&") {
                    "${java.net.URLEncoder.encode(it.key, "UTF-8")}=${java.net.URLEncoder.encode(it.value, "UTF-8")}"
                })
            }
            val request = buildRequest(urlBuilder.toString(), method = "GET").build()
            executeRequest(request)
        }
    }

    // ==================== POST ====================
    suspend fun post(path: String, body: String): ApiResult {
        return withContext(Dispatchers.IO) {
            val url = "${AppConstants.API_BASE_URL.trimEnd('/')}/api.php/$path"
            val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = buildRequest(url, method = "POST").post(requestBody).build()
            executeRequest(request)
        }
    }

    // ==================== PUT ====================
    suspend fun put(path: String, body: String): ApiResult {
        return withContext(Dispatchers.IO) {
            val url = "${AppConstants.API_BASE_URL.trimEnd('/')}/api.php/$path"
            val requestBody = body.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = buildRequest(url, method = "PUT").put(requestBody).build()
            executeRequest(request)
        }
    }

    // ==================== DELETE ====================
    suspend fun delete(path: String): ApiResult {
        return withContext(Dispatchers.IO) {
            val url = "${AppConstants.API_BASE_URL.trimEnd('/')}/api.php/$path"
            val request = buildRequest(url, method = "DELETE").delete().build()
            executeRequest(request)
        }
    }

    // ==================== Upload File ====================
    suspend fun uploadFile(
        path: String,
        file: File,
        description: String = "",
        tags: String = "",
        protocolId: Int? = null,
        isFolder: Boolean = false,
        folderName: String = ""
    ): ApiResult {
        return withContext(Dispatchers.IO) {
            val url = "${AppConstants.API_BASE_URL.trimEnd('/')}/api.php/$path"
            val multipartBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("file", file.name, file.asRequestBody("application/octet-stream".toMediaType()))
            if (description.isNotBlank()) multipartBuilder.addFormDataPart("description", description)
            if (tags.isNotBlank()) multipartBuilder.addFormDataPart("tags", tags)
            if (protocolId != null) multipartBuilder.addFormDataPart("protocol_id", protocolId.toString())
            if (isFolder) multipartBuilder.addFormDataPart("is_folder", "1")
            if (folderName.isNotBlank()) multipartBuilder.addFormDataPart("folder_name", folderName)
            val request = buildRequest(url, method = "POST").post(multipartBuilder.build()).build()
            executeRequest(request)
        }
    }

    // ==================== Proxy Direct ====================
    suspend fun proxyDirect(
        targetUrl: String,
        headers: Map<String, String>,
        rawBody: String = "",
        method: String = "POST",
        bodyType: BodyType = BodyType.RAW,
        tags: String = ""
    ): ApiResult {
        return withContext(Dispatchers.IO) {
            val requestBuilder = Request.Builder()
                .url(targetUrl)
                .header("User-Agent", UserAgentGenerator.randomUserAgent())

            headers.forEach { (k, v) -> requestBuilder.header(k, v) }

            val contentType = when (bodyType) {
                BodyType.JSON -> "application/json"
                BodyType.URL_ENCODED -> "application/x-www-form-urlencoded"
                else -> "text/plain"
            }

            val request = when (method.uppercase()) {
                "GET" -> requestBuilder.get().build()
                "PUT" -> requestBuilder.put(rawBody.toRequestBody(contentType.toMediaType())).build()
                "DELETE" -> requestBuilder.delete(rawBody.toRequestBody(contentType.toMediaType())).build()
                "PATCH" -> requestBuilder.patch(rawBody.toRequestBody(contentType.toMediaType())).build()
                else -> requestBuilder.post(rawBody.toRequestBody(contentType.toMediaType())).build()
            }
            executeRequest(request)
        }
    }

    // ==================== Convenience APIs ====================
    suspend fun getAnnouncements(): ApiResult = get("announcements")
    suspend fun getSettings(): ApiResult = get("settings")
    suspend fun getProtocols(page: Int = 1, limit: Int = 50): ApiResult =
        get("protocols", mapOf("page" to page.toString(), "limit" to limit.toString()))
    suspend fun getFiles(page: Int = 1, limit: Int = 50): ApiResult =
        get("files", mapOf("page" to page.toString(), "limit" to limit.toString()))
    suspend fun searchCommunity(query: String, tag: String = "", page: Int = 1, limit: Int = 50): ApiResult =
        get("community/posts", mapOf("q" to query, "tag" to tag, "page" to page.toString(), "limit" to limit.toString()))
    suspend fun getCommunityPost(postId: String): ApiResult = get("community/posts/$postId")

    suspend fun createProtocol(name: String, rawHeaders: String, rawBody: String = "", method: String = "POST", tags: String = ""): ApiResult {
        val body = JSONObject().apply {
            put("name", name)
            put("raw_headers", rawHeaders)
            put("raw_body", rawBody)
            put("method", method)
            if (tags.isNotBlank()) put("tags", tags)
        }
        return post("protocols", body.toString())
    }

    suspend fun deleteProtocol(id: String): ApiResult = delete("protocols/$id")
    suspend fun downloadProtocol(protocolId: String): ApiResult = get("protocols/$protocolId/download")

    suspend fun downloadFileContent(fileId: String): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = AppConstants.API_BASE_URL.trimEnd('/')
                val url = "$baseUrl/api.php/files/$fileId/download"
                val request = buildRequest(url, method = "POST")
                    .post("".toRequestBody("application/json".toMediaType()))
                    .build()
                val response = httpClient.newCall(request).execute()
                val code = response.code
                val body = response.body?.string() ?: ""
                if (code == 200) {
                    ApiResult.Success(body, code)
                } else {
                    val errorMsg = try { JSONObject(body).optString("error", "") } catch (_: Exception) { "" }
                    ApiResult.Error(errorMsg.ifBlank { "HTTP $code" }, code)
                }
            } catch (e: Exception) {
                ApiResult.Error("下载失败: ${e.message}")
            }
        }
    }

    suspend fun downloadFile(fileId: String, destFile: File, onProgress: (Float) -> Unit = {}): ApiResult {
        return withContext(Dispatchers.IO) {
            try {
                val baseUrl = AppConstants.API_BASE_URL.trimEnd('/')
                val url = "$baseUrl/api.php/files/$fileId/download"
                val request = buildRequest(url, method = "POST")
                    .post("".toRequestBody("application/json".toMediaType()))
                    .build()
                val response = httpClient.newCall(request).execute()
                if (response.code != 200) {
                    val body = response.body?.string() ?: ""
                    val errorMsg = try { JSONObject(body).optString("error", "") } catch (_: Exception) { "" }
                    return@withContext ApiResult.Error(errorMsg.ifBlank { "HTTP ${response.code}" }, response.code)
                }
                val totalBytes = response.body?.contentLength() ?: -1L
                response.body?.byteStream()?.use { input ->
                    destFile.outputStream().use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            if (totalBytes > 0) onProgress(totalRead.toFloat() / totalBytes)
                        }
                    }
                }
                ApiResult.Success("File downloaded: ${destFile.name}", 200)
            } catch (e: Exception) {
                ApiResult.Error("下载失败: ${e.message}")
            }
        }
    }

    // ==================== Request Executor ====================
    private fun buildRequest(url: String, method: String): Request.Builder {
        return Request.Builder()
            .url(url)
            .header("User-Agent", UserAgentGenerator.randomUserAgent())
            .apply {
                val token = tokenProvider()
                if (!token.isNullOrBlank()) header("Authorization", "Bearer $token")
            }
    }

    /**
     * 智能解码响应体：
     * 1. 如果 Content-Encoding: gzip → 先 GZIP 解压
     * 2. 如果 Content-Type 含 charset → 用指定编码
     * 3. 否则依次尝试 UTF-8 → GBK → GB2312 → ISO-8859-1
     */
    private fun decodeResponseBody(response: okhttp3.Response): String {
        val contentType = response.header("Content-Type") ?: ""
        val contentEncoding = response.header("Content-Encoding") ?: ""
        val rawBytes = response.body?.bytes() ?: return ""

        // Step 1: gzip 解压
        val bytes = if (contentEncoding.contains("gzip", ignoreCase = true)) {
            try {
                GZIPInputStream(ByteArrayInputStream(rawBytes)).use { it.readBytes() }
            } catch (_: Exception) {
                rawBytes  // 不是真正的 gzip，原样处理
            }
        } else {
            rawBytes
        }

        // Step 2: 从 Content-Type 提取 charset
        val declaredCharset = contentType
            .split(";")
            .drop(1)
            .map { it.trim() }
            .firstOrNull { it.startsWith("charset=", ignoreCase = true) }
            ?.substringAfter("charset=", "")
            ?.trim()
            ?.removeSurrounding("\"", "\"")

        if (!declaredCharset.isNullOrBlank()) {
            return try {
                String(bytes, Charset.forName(declaredCharset))
            } catch (_: Exception) {
                // 声明的 charset 不可用，走 fallback
                fallbackDecode(bytes)
            }
        }

        // Step 3: 自动检测
        return smartDecode(bytes)
    }

    private fun smartDecode(bytes: ByteArray): String {
        // 先尝试 UTF-8
        val utf8Str = try { String(bytes, Charset.forName("UTF-8")) } catch (_: Exception) { null }
        if (utf8Str != null && !containsReplacementChars(utf8Str)) {
            return utf8Str
        }
        return fallbackDecode(bytes)
    }

    private fun fallbackDecode(bytes: ByteArray): String {
        // 依次尝试 GBK → GB2312 → ISO-8859-1
        for (charset in listOf("GBK", "GB2312", "GB18030")) {
            try {
                val s = String(bytes, Charset.forName(charset))
                if (!containsReplacementChars(s)) return s
            } catch (_: Exception) {}
        }
        // 最后兜底 ISO-8859-1（永远不会失败）
        return String(bytes, Charset.forName("ISO-8859-1"))
    }

    /**
     * 检测字符串中是否含有 Unicode 替换字符 (U+FFFD)，
     * 这通常意味着用错误的 charset 解码了。
     */
    private fun containsReplacementChars(s: String): Boolean {
        // 检查替换字符（解码失败标志）
        val replacementCount = s.count { it == '\uFFFD' }
        if (replacementCount > 0) return true
        // 也检查连续乱码特征：大量非中文非英文非数字不可打印字符
        val nonPrintableCount = s.count { it.code < 0x20 && it != '\n' && it != '\r' && it != '\t' }
        return nonPrintableCount > s.length * 0.1  // 超过 10% 不可打印字符 → 可能乱码
    }

    private fun executeRequest(request: Request): ApiResult {
        return try {
            val response = httpClient.newCall(request).execute()
            val code = response.code
            val body = decodeResponseBody(response)
            Log.d(TAG, "${request.method} ${request.url} → $code (${body.length} chars)")
            if (code in 200..299) {
                ApiResult.Success(body, code)
            } else {
                val errorMsg = try { JSONObject(body).optString("error", "") } catch (_: Exception) { "" }
                ApiResult.Error(errorMsg.ifBlank { "HTTP $code: $body" }, code)
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout: ${request.url}")
            ApiResult.Error("请求超时: ${request.url}")
        } catch (e: UnknownHostException) {
            Log.e(TAG, "No network: ${request.url}")
            ApiResult.Error("无法连接服务器: ${e.message}")
        } catch (e: ConnectException) {
            Log.e(TAG, "Connection refused: ${request.url}")
            ApiResult.Error("连接被拒绝: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${request.url}", e)
            ApiResult.Error("网络错误: ${e.message}")
        }
    }
}

sealed class ApiResult {
    data class Success(val body: String, val code: Int) : ApiResult()
    data class Error(val message: String, val code: Int = -1) : ApiResult()
}