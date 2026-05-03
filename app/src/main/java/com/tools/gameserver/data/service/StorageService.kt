package com.tools.gameserver.data.service

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.tools.gameserver.core.constants.AppConstants
import com.tools.gameserver.data.model.CommunityItem
import com.tools.gameserver.data.model.CommunityProtocol
import com.tools.gameserver.data.model.Protocol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 数据持久化服务 — 协议 + 物品的本地存储
 *
 * 设计决策：
 * - 普通数据：SharedPreferences（JSON 序列化）
 * - 敏感数据（含密码字段的协议）：EncryptedSharedPreferences (Fix 5.2)
 *
 * 线程安全：
 * - 所有 IO 操作在 Dispatchers.IO 线程池执行
 * - SharedPreferences.apply() 异步提交，不阻塞 UI
 *
 * @param context Application Context（避免 Activity 泄漏）
 * @author GameServer Toolbox
 */
class StorageService private constructor(context: Context) {

    companion object {
        private const val TAG = "StorageService"

        @Volatile
        private var instance: StorageService? = null

        /**
         * 获取全局单例 — 避免 EncryptedSharedPreferences 多实例死锁
         *
         * @param context Application Context
         * @return StorageService 单例
         */
        fun getInstance(context: Context): StorageService {
            return instance ?: synchronized(this) {
                instance ?: StorageService(context.applicationContext).also { instance = it }
            }
        }
    }

    /** JSON 序列化配置 — 宽松模式，兼容未知字段 */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
    }

    /** 普通 SharedPreferences — 非敏感数据 */
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(
            AppConstants.PREFS_NAME,
            Context.MODE_PRIVATE
        )
    }

    /** 加密 SharedPreferences — 含密码字段的协议 (Fix 5.2) */
    private val securePrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            AppConstants.SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // ==================== 协议管理 ====================

    /**
     * 加载所有已保存的协议
     *
     * @return 协议 Map (id → Protocol)
     */
    suspend fun loadProtocols(): Map<String, Protocol> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = prefs.getString(AppConstants.KEY_PROTOCOLS, null)
            if (jsonStr.isNullOrBlank()) return@withContext emptyMap()

            val map = json.decodeFromString<Map<String, Protocol>>(jsonStr)
            map
        } catch (e: Exception) {
            Log.e(TAG, "加载协议失败: ${e.message}")
            emptyMap()
        }
    }

    suspend fun saveProtocols(protocols: Map<String, Protocol>) = withContext(Dispatchers.IO) {
        try {
            prefs.edit().putString(AppConstants.KEY_PROTOCOLS, json.encodeToString(protocols)).apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存协议失败: ${e.message}")
        }
    }

    suspend fun loadItems(): Map<String, List<CommunityItem>> = withContext(Dispatchers.IO) {
        try {
            val jsonStr = prefs.getString(AppConstants.KEY_ITEMS, null)
            if (jsonStr.isNullOrBlank()) return@withContext emptyMap()
            json.decodeFromString<Map<String, List<CommunityItem>>>(jsonStr)
        } catch (e: Exception) {
            Log.e(TAG, "加载物品失败: ${e.message}")
            emptyMap()
        }
    }

    suspend fun saveItems(items: Map<String, List<CommunityItem>>) = withContext(Dispatchers.IO) {
        try {
            prefs.edit().putString(AppConstants.KEY_ITEMS, json.encodeToString(items)).apply()
        } catch (e: Exception) {
            Log.e(TAG, "保存物品失败: ${e.message}")
        }
    }

    suspend fun clearAll() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}