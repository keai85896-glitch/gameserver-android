package com.tools.gameserver.core.constants

/**
 * 应用全局常量 — 集中管理所有魔法字符串和数值
 *
 * 禁止在业务代码中直接硬编码，一律引用此文件常量
 */
object AppConstants {

    // ==================== 网络 ====================

    /** 后端 API 基础 URL */
    const val API_BASE_URL = "http://d78.bt1.886966.xyz"

    /** OkHttp 连接超时 (秒) */
    const val CONNECT_TIMEOUT_SECONDS = 15L

    /** OkHttp 读取超时 (秒) */
    const val READ_TIMEOUT_SECONDS = 30L

    /** OkHttp 写入超时 (秒) */
    const val WRITE_TIMEOUT_SECONDS = 30L

    // ==================== 存储 ====================

    /** SharedPreferences 文件名 */
    const val PREFS_NAME = "game_server_toolbox_prefs"

    /** 加密存储文件名 (敏感数据) */
    const val SECURE_PREFS_NAME = "game_server_toolbox_secure"

    /** 已保存协议的 Storage Key */
    const val KEY_PROTOCOLS = "protocols"

    /** 已保存物品的 Storage Key */
    const val KEY_ITEMS = "items"

    // ==================== 密码字段 ====================

    /** 需要脱敏/加密的 Body 参数 Key 列表 */
    val SENSITIVE_PARAM_KEYS = setOf("pwd", "password", "passwd", "token", "secret")

    // ==================== Toast ====================

    /** Toast 短显示时长 (毫秒) */
    const val TOAST_SHORT_MS = 2000L

    /** Toast 长显示时长 (毫秒) */
    const val TOAST_LONG_MS = 3500L

    // ==================== UI ====================

    /** 底部导航栏高度 (dp) */
    const val BOTTOM_NAV_HEIGHT = 64

    /** GlassCard 圆角 (dp) */
    const val CARD_CORNER_RADIUS = 16

    /** GlassCard 透明度 (0f - 1f) */
    const val CARD_ALPHA = 0.7f

    /** Neon 发光效果模糊半径 (dp) */
    const val NEON_BLUR_RADIUS = 12

    // ==================== 社区 Mock ====================

    /** 社区页面初始加载页码 */
    const val COMMUNITY_INITIAL_PAGE = 1

    /** 社区页面每页条数 */
    const val COMMUNITY_PAGE_SIZE = 20

    // ==================== QQ 群 ====================

    /** 官方 QQ 群链接 */
    const val QQ_GROUP_URL = "mqqapi://card/show_pslcard?src_type=internal&version=1&card_type=group&uin=552940784"

    /** 官方 QQ 群号 */
    const val QQ_GROUP_NUM = "552940784"

    /** 悬浮窗位置偏好 Key */
    const val FLOATING_POSITIONS = "floating_positions"

    // ==================== 版本 ====================

    /** 应用版本号 (与 build.gradle.kts versionName 同步) */
    const val APP_VERSION = "1.0.0"
}