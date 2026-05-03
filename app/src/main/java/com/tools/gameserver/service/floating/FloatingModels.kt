package com.tools.gameserver.service.floating

import com.tools.gameserver.data.model.ParsedData
import com.tools.gameserver.presentation.features.workspace.model.LocalGameEntry

/**
 * 悬浮窗页面状态
 */
internal sealed class FloatingPage {
    object GameList : FloatingPage()
    data class GameDetail(val entry: LocalGameEntry) : FloatingPage()
    data class ProtocolResult(
        val entry: LocalGameEntry,
        val parsedData: ParsedData,
        val rawHeaders: String,
        val rawBody: String,
        val allItems: List<Pair<String, String>>,
        val itemFileName: String
    ) : FloatingPage()
}
