package com.tools.gameserver.core.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * 全局 Snackbar 管理器
 *
 * 用法：
 *   SnackbarManager.show("操作成功")
 *   SnackbarManager.show("删除了", "撤销") { undo() }
 *
 * 在 Composable 中使用 SnackbarHostState.showSnackbar() 响应
 */
object SnackbarManager {
    /** Snackbar Host State，由根组件注入 */
    val snackbarHostState = SnackbarHostState()

    /** 待显示的消息队列 */
    private val _messages = MutableSharedFlow<SnackbarMessage>(extraBufferCapacity = 5)
    val messages = _messages

    fun show(
        message: String,
        actionLabel: String? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        onAction: (() -> Unit)? = null
    ) {
        _messages.tryEmit(SnackbarMessage(message, actionLabel, duration, onAction))
    }
}

data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val onAction: (() -> Unit)? = null
)