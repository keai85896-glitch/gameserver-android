package com.tools.gameserver.data.service

import android.util.Log
import com.tools.gameserver.data.model.BodyType
import com.tools.gameserver.data.model.ParsedData
import java.net.URLEncoder

object ProtocolParser {

    private const val TAG = "ProtocolParser"

    private val METHOD_REGEX = Regex(
        """^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT)\s+(\S+)""",
        RegexOption.IGNORE_CASE
    )
    private val HEADER_REGEX = Regex("""^([A-Za-z0-9\-_]+)\s*:\s*(.+)$""")
    private val IP_REGEX = Regex("""^(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}|localhost)""", RegexOption.IGNORE_CASE)

    fun parse(rawHeaders: String, rawBody: String): ParsedData {
        if (rawHeaders.isBlank()) {
            return ParsedData(method = "GET", url = "", headers = emptyMap(), bodyType = BodyType.NONE)
        }
        val lines = rawHeaders.trim().lines()
        if (lines.isEmpty()) {
            return ParsedData(method = "GET", url = "", headers = emptyMap(), bodyType = BodyType.NONE)
        }

        // 1. Extract method and path from request line
        // 支持 === POST /path === 这种装饰格式
        val firstLine = lines[0].trim().trimStart('=', '-', '*', '~', ' ', '#', '>').trim()
        val methodMatch = METHOD_REGEX.find(firstLine)
        val method = methodMatch?.groupValues?.get(1)?.uppercase() ?: "GET"
        val rawPath = methodMatch?.groupValues?.get(2) ?: "/"

        // 2. Parse headers
        val headers = mutableMapOf<String, String>()
        for (line in lines.drop(1)) {
            val match = HEADER_REGEX.find(line.trim())
            if (match != null) {
                headers[match.groupValues[1]] = match.groupValues[2].trim()
            }
        }

        // 3. Build full URL
        val host = headers["Host"] ?: headers["host"] ?: ""
        val path = if (rawPath.startsWith("/")) rawPath else "/$rawPath"
        val scheme = if (IP_REGEX.containsMatchIn(host) || host.contains(":")) "http" else "https"
        val cleanPath = try { java.net.URI(path).path } catch (_: Exception) { path }
        val query = try { java.net.URI(path).query } catch (_: Exception) { null }
        val url = if (query != null) "$scheme://$host$cleanPath?$query" else "$scheme://$host$cleanPath"

        // 4. Determine body type
        val contentType = headers.entries.firstOrNull { it.key.equals("Content-Type", ignoreCase = true) }?.value ?: ""
        val bodyType = when {
            rawBody.isBlank() -> BodyType.NONE
            contentType.contains("application/json", true) -> BodyType.JSON
            contentType.contains("x-www-form-urlencoded", true) -> BodyType.URL_ENCODED
            contentType.contains("multipart/form-data", true) -> BodyType.MULTIPART
            // 智能检测：当没有 Content-Type 时，尝试自动识别
            rawBody.isNotBlank() && isUrlEncodedBody(rawBody) -> BodyType.URL_ENCODED
            rawBody.trimStart().startsWith("{") && rawBody.trimEnd().endsWith("}") -> BodyType.JSON
            else -> BodyType.RAW
        }

        // 5. Parse body params (for URL_ENCODED)
        val bodyParams = if (bodyType == BodyType.URL_ENCODED) {
            val params = mutableMapOf<String, String>()
            for (pair in rawBody.split("&")) {
                val eqIndex = pair.indexOf('=')
                if (eqIndex < 0) continue
                val keyRaw = pair.substring(0, eqIndex).trim()
                val valueRaw = pair.substring(eqIndex + 1).trim()
                val key = try { java.net.URLDecoder.decode(keyRaw, "UTF-8") } catch (_: Exception) { keyRaw }
                val value = try { java.net.URLDecoder.decode(valueRaw, "UTF-8") } catch (_: Exception) { valueRaw }
                params[key] = value
            }
            params
        } else emptyMap()

        return ParsedData(method = method, url = url, headers = headers, bodyParams = bodyParams, rawBody = rawBody, bodyType = bodyType)
    }

    /**
     * 智能检测 body 是否为 URL_ENCODED 格式
     * 规则：至少有 2 个 & 分隔的 key=value 对
     */
    private fun isUrlEncodedBody(body: String): Boolean {
        val pairs = body.split("&")
        if (pairs.size < 2) return false
        var validCount = 0
        for (pair in pairs) {
            val trimmed = pair.trim()
            if (trimmed.isNotBlank() && trimmed.contains("=")) {
                val eqIdx = trimmed.indexOf('=')
                if (eqIdx > 0 && eqIdx < trimmed.length - 1) validCount++
            }
        }
        return validCount >= 2
    }

    fun rebuildRawRequest(parsed: ParsedData): String {
        val urlPath = try { java.net.URI(parsed.url).let { it.path + if (it.query != null) "?" + it.query else "" } } catch (_: Exception) { "/" }
        val headerLines = mutableListOf("${parsed.method} $urlPath HTTP/1.1")
        parsed.headers.forEach { (key, value) -> headerLines.add("$key: $value") }
        return headerLines.joinToString("\n")
    }

    fun rebuildRawBody(parsed: ParsedData): String {
        return when (parsed.bodyType) {
            BodyType.URL_ENCODED -> parsed.bodyParams.entries.joinToString("&") { entry ->
                val encodedKey = try { URLEncoder.encode(entry.key, "UTF-8") } catch (_: Exception) { entry.key }
                val encodedValue = try { URLEncoder.encode(entry.value, "UTF-8") } catch (_: Exception) { entry.value }
                "$encodedKey=$encodedValue"
            }
            else -> parsed.rawBody
        }
    }
}