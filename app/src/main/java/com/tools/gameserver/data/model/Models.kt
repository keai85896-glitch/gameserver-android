package com.tools.gameserver.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 协议数据模型 — 存储用户保存的完整协议信息
 *
 * @param id 唯一标识 (时间戳生成)
 * @param name 用户自定义名称
 * @param rawHeaders 原始请求头文本
 * @param rawBody 原始请求体文本
 * @param createdAt 创建时间 (epoch millis)
 * @param updatedAt 更新时间 (epoch millis)
 */
@Serializable
data class Protocol(
    val id: String,
    val name: String,
    val rawHeaders: String,
    val rawBody: String,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * 解析后的结构化数据 — 协议解析引擎的输出
 *
 * @param method HTTP 方法 (GET/POST/PUT/DELETE/PATCH)
 * @param url 完整请求 URL (scheme://host/path)
 * @param headers 解析后的 Headers Map
 * @param bodyParams 解析后的 Body 参数 Map (urlencoded 时使用)
 * @param rawBody 原始 Body 文本 (json/raw 时使用)
 * @param bodyType Body 类型枚举
 */
@Serializable
data class ParsedData(
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val bodyParams: Map<String, String> = emptyMap(),
    val rawBody: String = "",
    val bodyType: BodyType = BodyType.NONE
)

/**
 * HTTP Body ç±»åæä¸¾
 */
@Serializable
enum class BodyType {
    @SerialName("none") NONE,
    @SerialName("urlencoded") URL_ENCODED,
    @SerialName("json") JSON,
    @SerialName("raw") RAW,
    @SerialName("multipart") MULTIPART
}

/**
 * 代理请求响应模型
 *
 * @param status HTTP 状态码
 * @param statusText HTTP 状态描述
 * @param headers 响应头
 * @param body 响应体文本
 * @param route 请求路由 (固定为 "direct")
 * @param timestamp 响应时间 (epoch millis)
 * @param durationMs 请求耗时 (毫秒)
 */
@Serializable
data class ProxyResponse(
    val status: Int,
    val statusText: String,
    val headers: Map<String, String>,
    val body: String,
    val route: String,
    val timestamp: Long,
    val durationMs: Long
)

/**
 * 社区协议模型
 *
 * @param id 唯一标识
 * @param name 协议名称
 * @param author 作者
 * @param description 描述
 * @param rawHeaders 原始请求头
 * @param rawBody 原始请求体
 * @param downloads 下载次数
 * @param rating 评分 (0.0 - 5.0)
 * @param tags 标签列表
 */
@Serializable
data class CommunityProtocol(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val rawHeaders: String,
    val rawBody: String,
    val downloads: Int,
    val rating: Float,
    val tags: List<String>,
    val method: String = "",
    val url: String = ""
)

/**
 * ç¤¾åºç©åæ¨¡å
 *
 * @param id å¯ä¸æ è¯
 * @param name ç©ååç§°
 * @param description æè¿°
 * @param code ç©ååæ¢ç /å½ä»¤
 * @param author ä½è
 * @param downloads ä¸è½½æ¬¡æ°
 */
@Serializable
data class CommunityItem(
    val id: String,
    val name: String,
    val description: String,
    val code: String,
    val author: String,
    val downloads: Int
)

/**
 * ç¤¾åºæä»¶æ¨¡åï¼å¯¹åºåç«¯ files è¡¨ï¼
 *
 * @param id å¯ä¸æ è¯
 * @param name æä»¶æ¾ç¤ºåç§°
 * @param description æè¿°
 * @param filePath æå¡ç«¯å­å¨è·¯å¾
 * @param fileSize æä»¶å¤§å°ï¼å­èï¼
 * @param fileType MIME ç±»å
 * @param author ä¸ä¼ èç¨æ·å
 * @param downloads ä¸è½½æ¬¡æ°
 * @param tags éå·åéæ ç­¾
 */
@Serializable
data class CommunityFile(
    val id: String,
    val name: String,
    val description: String,
    val filePath: String,
    val fileSize: Long,
    val fileType: String,
    val author: String,
    val downloads: Int,
    val tags: String
)

/**
 * ç¤¾åºå¸å­æ¨¡å â åå®¢é£æ ¼ï¼ä¸ä¸ªå¸å­ = ä¸ä¸ªåè®® + å³èæä»¶ æ ä¸ä¸ªç¬ç«æä»¶
 *
 * @param id å¸å­ IDï¼å¤åæ ¼å¼ï¼p_123=åè®®, f_456=æä»¶ï¼
 * @param name å¸å­æ é¢
 * @param author ä½è
 * @param description æè¿°
 * @param method HTTP æ¹æ³
 * @param url ç®æ  URL
 * @param rawHeaders åå§è¯·æ±å¤´ï¼ä»åè®®å¸å­è¯¦æé¡µæ¾ç¤ºï¼
 * @param rawBody åå§è¯·æ±ä½ï¼ä»åè®®å¸å­è¯¦æé¡µæ¾ç¤ºï¼
 * @param tags æ ç­¾åè¡¨
 * @param downloads ä¸è½½æ¬¡æ°
 * @param rating è¯å
 * @param fileCount å³èæä»¶æ°é
 * @param files å³èæä»¶åè¡¨
 * @param postType 帖子类型：protocol=协议帖, file=文件帖
 */
@Serializable
data class CommunityPost(
    val id: String,
    val name: String,
    val author: String,
    val description: String,
    val method: String = "",
    val url: String = "",
    val rawHeaders: String = "",
    val rawBody: String = "",
    val tags: List<String> = emptyList(),
    val downloads: Int = 0,
    val rating: Float = 0f,
    val fileCount: Int = 0,
    val files: List<CommunityFile> = emptyList(),
    val postType: String = "protocol"
)

/**
 * 请求发送状态封装
 */
sealed class SendStatus {
    /** 空闲状态 */
    data object Idle : SendStatus()
    /** 加载中 */
    data class Loading(val message: String = "加载中") : SendStatus()
    /** 成功 */
    data class Success(val message: String = "发送成功") : SendStatus()
    /** 失败 */
    data class Error(val message: String = "发送失败") : SendStatus()
}

/**
 * 公告数据模型 — 从服务器获取的公告信息
 *
 * @param id 公告 ID
 * @param title 标题
 * @param content 内容
 * @param type 类型 (info/warning/important)
 * @param createdAt 创建时间
 */
data class Announcement(
    val id: Int,
    val title: String,
    val content: String,
    val type: String,
    val createdAt: String
)