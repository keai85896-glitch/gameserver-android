package com.tools.gameserver.core.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 主题模式持久化服务 — 基于 DataStore Preferences
 *
 * 线程安全：DataStore 保证原子读写，单例 DataStore 实例
 *
 * @param context Application Context
 */
class ThemeModeService private constructor(context: Context) {

    companion object {
        /** DataStore 文件名 */
        private const val DATASTORE_NAME = "theme_mode_prefs"

        /** 存储键 */
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")

        /** 默认主题模式 */
        val DEFAULT = ThemeMode.SYSTEM

        @Volatile
        private var instance: ThemeModeService? = null

        /**
         * 获取全局单例 — 保证 DataStore 同一文件只有一个活跃实例
         *
         * @param context Application Context
         * @return ThemeModeService 单例
         */
        fun getInstance(context: Context): ThemeModeService {
            return instance ?: synchronized(this) {
                instance ?: ThemeModeService(context.applicationContext).also { instance = it }
            }
        }
    }

    /** DataStore 单例 — 全局唯一，由 Context 区分 */
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DATASTORE_NAME
    )

    private val appContext = context.applicationContext

    /**
     * 观察当前主题模式
     *
     * @return Flow<ThemeMode> 主题模式流，首次默认 SYSTEM
     */
    val themeModeFlow: Flow<ThemeMode> = appContext.dataStore.data.map { prefs ->
        val name = prefs[KEY_THEME_MODE] ?: ThemeMode.SYSTEM.name
        runCatching { ThemeMode.valueOf(name) }.getOrDefault(ThemeMode.SYSTEM)
    }

    /**
     * 设置主题模式
     *
     * @param mode 目标模式
     */
    suspend fun setThemeMode(mode: ThemeMode) {
        appContext.dataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = mode.name
        }
    }
}