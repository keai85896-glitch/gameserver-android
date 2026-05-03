package com.tools.gameserver.data.repository

import android.util.Log
import com.tools.gameserver.data.model.Announcement
import com.tools.gameserver.data.service.api.ApiClient
import com.tools.gameserver.data.service.api.ApiResult
import org.json.JSONObject

/**
 * 公告加载结果
 */
data class AnnouncementLoadResult(
    val announcements: List<Announcement>,
    val alwaysShow: Boolean,
    val allowEnterMain: Boolean,
    val blockTitle: String,
    val blockMessage: String
)

/**
 * 公告数据仓库 — 从后端 API 加载公告
 */
object AnnouncementRepository {

    private const val TAG = "AnnouncementRepo"

    suspend fun loadAnnouncements(): Result<AnnouncementLoadResult> {
        return try {
            when (val result = ApiClient.getAnnouncements()) {
                is ApiResult.Success -> {
                    val json = JSONObject(result.body)
                    val success = json.optBoolean("success", true)
                    if (!success) {
                        return Result.failure(Exception(json.optString("error", "加载公告失败")))
                    }
                    val data = json.optJSONObject("data") ?: json
                    val items = data.optJSONArray("announcements") ?: data.optJSONArray("items")
                    val announcements = mutableListOf<Announcement>()
                    if (items != null) {
                        for (i in 0 until items.length()) {
                            val item = items.getJSONObject(i)
                            announcements.add(
                                Announcement(
                                    id = item.optInt("id", 0),
                                    title = item.optString("title", ""),
                                    content = item.optString("content", ""),
                                    type = item.optString("type", "info"),
                                    createdAt = item.optString("created_at", "")
                                )
                            )
                        }
                    }
                    val alwaysShow = data.optBoolean("always_show", false)
                    val allowEnterMain = data.optBoolean("allow_enter_main", true)
                    val blockTitle = data.optString("block_title", "")
                    val blockMessage = data.optString("block_message", "")
                    Log.d(TAG, "Loaded ${announcements.size} announcements, alwaysShow=$alwaysShow, allowEnterMain=$allowEnterMain")
                    Result.success(AnnouncementLoadResult(announcements, alwaysShow, allowEnterMain, blockTitle, blockMessage))
                }
                is ApiResult.Error -> Result.failure(Exception(result.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class VersionCheckResult(
    val needUpdate: Boolean,
    val forceUpdate: Boolean,
    val latestVersion: String,
    val currentVersion: String,
    val downloadUrl: String,
    val updateDescription: String,
    val updateEnabled: Boolean
)