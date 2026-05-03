package com.tools.gameserver.presentation.features.workspace

import android.content.Context
import android.util.Log
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry
import java.io.File

private const val TAG = "WorkspaceUtils"

private val HTTP_METHOD_REGEX = Regex("""^(GET|POST|PUT|DELETE|PATCH|HEAD|OPTIONS|TRACE|CONNECT)\s+\S""", RegexOption.IGNORE_CASE)

/**
 * 检测文件内容是否为 HTTP 协议格式
 * 通过读取前几行判断是否以 HTTP 方法开头（如 POST /path）
 */
private fun isProtocolContent(file: File): Boolean {
    return try {
        if (file.length() == 0L) return false
        file.bufferedReader().useLines { lines ->
            for (line in lines.take(5)) {
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    // 跳过开头的非字母字符（如 === POST... ===）
                    val cleaned = trimmed.trimStart('=', '-', '*', '~', ' ', '#', '>')
                    if (cleaned.isNotEmpty() && HTTP_METHOD_REGEX.containsMatchIn(cleaned)) {
                        return@useLines true
                    }
                }
            }
            false
        }
    } catch (_: Exception) {
        false
    }
}

/**
 * 扫描本地游戏目录
 * 路径结构：/storage/emulated/0/游戏私服物品/{作者}/{游戏名}/
 */
fun scanLocalGameDirectories(): List<LocalGameEntry> {
    val baseDir = File("/storage/emulated/0/游戏私服物品")
    if (!baseDir.exists() || !baseDir.isDirectory) return emptyList()

    val entries = mutableListOf<LocalGameEntry>()
    try {
        for (authorDir in baseDir.listFiles()?.filter { it.isDirectory } ?: emptyList()) {
            for (gameDir in authorDir.listFiles()?.filter { it.isDirectory } ?: emptyList()) {
                val allFiles = gameDir.listFiles()?.filter { it.isFile } ?: emptyList()

                // 1️⃣ 协议文件：扩展名 .protocol，或文件名含"协议"，或内容是HTTP请求格式
                val protocolFiles = allFiles.filter { file ->
                    file.extension.equals("protocol", ignoreCase = true) ||
                    file.name.contains("协议", true) ||
                    isProtocolContent(file)
                }

                // 2️⃣ 物品文件：排除已归类为协议的文件，其余全部归为物品
                val itemFiles = allFiles.filter { file ->
                    file !in protocolFiles && (
                        file.extension.equals("item", ignoreCase = true) ||
                        file.extension.equals("txt", ignoreCase = true) ||
                        file.name.contains("物品", true) ||
                        file.name.contains("item", true)
                    )
                }
                if (protocolFiles.isNotEmpty() || itemFiles.isNotEmpty()) {
                    entries.add(
                        LocalGameEntry(
                            dir = gameDir,
                            author = authorDir.name,
                            gameName = gameDir.name,
                            protocolFiles = protocolFiles,
                            itemFiles = itemFiles,
                            lastModified = gameDir.lastModified()
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "扫描本地游戏目录失败: ${e.message}")
    }
    return entries
}

/**
 * 解析本地物品文件（每行格式：code,name 或 code|name 或 code\tname）
 */
fun parseItemFile(file: File): List<Pair<String, String>> {
    val items = mutableListOf<Pair<String, String>>()
    try {
        val content = file.readText()
        for (line in content.lines()) {
            val trimmed = line.trim()
            if (trimmed.isBlank() || trimmed.startsWith("#")) continue
            val parts = trimmed.split(Regex("[,;\\t|]"), limit = 2)
            if (parts.size == 2) {
                items.add(parts[0].trim() to parts[1].trim())
            } else if (parts.size == 1 && parts[0].isNotBlank()) {
                items.add(parts[0].trim() to parts[0].trim())
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "解析物品文件失败: ${e.message}")
    }
    return items
}