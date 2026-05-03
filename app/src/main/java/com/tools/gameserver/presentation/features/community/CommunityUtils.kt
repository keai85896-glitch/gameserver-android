package com.tools.gameserver.presentation.features.community

import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import com.tools.gameserver.data.model.CommunityPost
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

internal fun hasStoragePermission(context: android.content.Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        @Suppress("DEPRECATION")
        context.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}

internal fun formatRelativeTime(dateStr: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return try {
        val date = dateFormat.parse(dateStr) ?: return dateStr
        val diff = System.currentTimeMillis() - date.time
        when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "刚刚"
            diff < TimeUnit.HOURS.toMillis(1) -> "${diff / TimeUnit.MINUTES.toMillis(1)} 分钟前"
            diff < TimeUnit.DAYS.toMillis(1) -> "${diff / TimeUnit.HOURS.toMillis(1)} 小时前"
            diff < TimeUnit.DAYS.toMillis(30) -> "${diff / TimeUnit.DAYS.toMillis(1)} 天前"
            else -> dateStr
        }
    } catch (_: Exception) { dateStr }
}

internal fun getFileIcon(fileName: String): ImageVector {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return when {
        ext in listOf("jpg", "jpeg", "png", "gif", "webp", "bmp") -> Icons.Default.Image
        ext in listOf("mp3", "wav", "flac", "aac") -> Icons.Default.AudioFile
        ext in listOf("mp4", "avi", "mkv", "mov") -> Icons.Default.VideoFile
        ext in listOf("zip", "rar", "7z", "tar", "gz") -> Icons.Default.FolderZip
        ext == "pdf" -> Icons.Default.PictureAsPdf
        else -> Icons.AutoMirrored.Filled.InsertDriveFile
    }
}

internal fun getSafeFileName(post: CommunityPost): String {
    return post.name.replace(Regex("[\\\\/:*?\"<>|]"), "_").ifBlank { "未命名" }
}

internal suspend fun reportProtocolDownload(protocolId: String) {
    try {
        ApiClient.downloadProtocol(protocolId)
    } catch (_: Exception) {
        // silent
    }
}