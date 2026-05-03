package com.tools.gameserver.presentation.features.community

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tools.gameserver.core.theme.AppColors
import com.tools.gameserver.core.util.SnackbarManager
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File

enum class UploadIntent { PROTOCOL, RESOURCE }

internal data class SelectedFile(
    val uri: Uri,
    val fileName: String,
    val fileSize: Long,
    val name: String = "",
    val description: String = "",
    val tags: String = ""
)

@Composable
fun UnifiedUploadDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    uploadIntent: UploadIntent = UploadIntent.PROTOCOL,
    prefilledHeaders: String = "",
    prefilledBody: String = "",
    prefilledFiles: List<File> = emptyList()
) {
    if (!visible) return
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isUploading by remember(visible) { mutableStateOf(false) }
    var uploadProgress by remember(visible) { mutableIntStateOf(0) }
    var errorMsg by remember(visible) { mutableStateOf<String?>(null) }
    var pName by remember(visible) { mutableStateOf("") }
    var pDescription by remember(visible) { mutableStateOf("") }
    var pHeaders by remember(visible) { mutableStateOf(prefilledHeaders) }
    var pBody by remember(visible) { mutableStateOf(prefilledBody) }
    var selectedFiles by remember(visible) {
        mutableStateOf<List<SelectedFile>>(
            prefilledFiles.map { file ->
                SelectedFile(uri = Uri.fromFile(file), fileName = file.name, fileSize = file.length(), name = file.nameWithoutExtension)
            }
        )
    }
    val multiFilePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) {
            selectedFiles = selectedFiles + uris.map { uri ->
                SelectedFile(uri = uri, fileName = uri.lastPathSegment ?: "file", fileSize = 0)
            }
        }
    }

    AlertDialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        title = { Text("上传文件", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("名称") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = pDescription, onValueChange = { pDescription = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                if (errorMsg != null) {
                    Text(errorMsg!!, color = AppColors.SystemRed, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                }
                if (isUploading) {
                    LinearProgressIndicator(progress = { uploadProgress.toFloat() / maxOf(1, selectedFiles.size) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                scope.launch {
                    isUploading = true
                    try {
                        for ((i, file) in selectedFiles.withIndex()) {
                            withContext(Dispatchers.IO) {
                                ApiClient.uploadFile("files/upload", File(file.uri.path ?: ""), pDescription, pName)
                            }
                            uploadProgress = i + 1
                        }
                        SnackbarManager.show("上传成功")
                        onSuccess(); onDismiss()
                    } catch (e: Exception) {
                        errorMsg = "上传失败: ${e.message}"
                    } finally {
                        isUploading = false
                    }
                }
            }, enabled = !isUploading) { Text("上传") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}