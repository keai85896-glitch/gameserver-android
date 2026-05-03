package com.tools.gameserver.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.tools.gameserver.data.repository.VersionCheckResult

/** 版本更新弹窗 */
@Composable
fun UpdateDialog(versionResult: VersionCheckResult, onDismiss: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发现新版本") },
        text = { Text("最新版本: ${versionResult.latestVersion}\n${versionResult.updateDescription}") },
        confirmButton = {
            TextButton(onClick = {
                try {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(versionResult.downloadUrl))
                    context.startActivity(intent)
                } catch (_: Exception) { }
                onDismiss()
            }) { Text("更新") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("稍后") } }
    )
}