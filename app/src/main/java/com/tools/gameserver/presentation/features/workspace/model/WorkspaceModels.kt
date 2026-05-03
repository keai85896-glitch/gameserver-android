package com.tools.gameserver.presentation.features.workspace.model

import java.io.File

/**
 * 本地游戏目录条目
 */
data class LocalGameEntry(
    val dir: File,
    val author: String,
    val gameName: String,
    val protocolFiles: List<File>,
    val itemFiles: List<File>,
    val lastModified: Long
)

/**
 * 工作台页面路由 — sealed class 管理页面状态
 */
sealed class WorkspacePage {
    /** 游戏列表页 */
    object GameList : WorkspacePage()

    /** 游戏详情页（显示协议文件和物品文件） */
    data class GameDetail(val entry: LocalGameEntry) : WorkspacePage()

    /** 物品文件详情页（搜索 + 分页 + 勾选） */
    data class ItemFile(
        val file: File,
        val items: List<Pair<String, String>>,
        val ownerGameEntry: LocalGameEntry
    ) : WorkspacePage()

    /** 协议解析结果页 */
    object ProtocolResult : WorkspacePage()
}

/**
 * 弹窗类型
 */
sealed class WorkspaceDialog {
    object None : WorkspaceDialog()
    data class SaveItems(val snapshot: List<Pair<String, String>>) : WorkspaceDialog()
    data class DeleteFile(val file: File) : WorkspaceDialog()
    data class DeleteGame(val entry: LocalGameEntry) : WorkspaceDialog()
    object CreateProtocol : WorkspaceDialog()
    object StoragePermission : WorkspaceDialog()
    data class AddItemFile(val entry: LocalGameEntry) : WorkspaceDialog()
    data class UploadProtocol(val entry: LocalGameEntry) : WorkspaceDialog()
    object Help : WorkspaceDialog()
    data class GameLongPressMenu(val entry: LocalGameEntry) : WorkspaceDialog()
}
