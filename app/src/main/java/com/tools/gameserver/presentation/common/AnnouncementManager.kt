package com.tools.gameserver.presentation.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.tools.gameserver.data.model.Announcement

@Composable
fun AnnouncementManager(
    announcements: List<Announcement>,
    alwaysShow: Boolean,
    onDismiss: () -> Unit
) {
    if (announcements.isNotEmpty()) {
        val ann = announcements.first()
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(ann.title) },
            text = { Text(ann.content) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text("知道了")
                }
            }
        )
    }
}