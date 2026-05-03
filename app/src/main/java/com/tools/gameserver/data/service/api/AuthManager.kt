package com.tools.gameserver.data.service.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.tools.gameserver.core.constants.AppConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * 认证管理器 — 全局单例
 *
 * 职责：
 * 1. 管理登录/注册/登出状态
 * 2. 持久化 JWT Token（加密 SharedPreferences）
 * 3. 广播用户信息变更事件
 *
 * @author GameServer Toolbox
 */
object AuthManager {

    private const val TAG = "AuthManager"
    private const val PREFS_NAME = "gs_auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USERNAME = "username"
    private const val KEY_EMAIL = "email"
    private const val KEY_BIO = "bio"
    private const val KEY_AVATAR_URL = "avatar_url"
    private const val KEY_ROLE = "role"
    private const val KEY_UPLOAD_AUTHORIZED = "upload_authorized"

    /** 当前用户信息 — 响应式状态 */
    private val _userState = MutableStateFlow<UserInfo?>(null)
    val userState: StateFlow<UserInfo?> = _userState.asStateFlow()

    /** 是否已登录 — 响应式状态 */
    val isLoggedIn: Boolean get() = _userState.value != null

    private var prefs: SharedPreferences? = null

    /**
     * 初始化 — Application.onCreate 中调用
     *
     * @param context ApplicationContext
     */
    fun init(context: Context) {
        if (prefs != null) return

        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // 恢复上次登录状态
        val token = prefs?.getString(KEY_TOKEN, null)
        if (!token.isNullOrBlank()) {
            val userId = prefs?.getInt(KEY_USER_ID, -1) ?: -1
            if (userId > 0) {
                _userState.value = UserInfo(
                    id = userId,
                    username = prefs?.getString(KEY_USERNAME, "") ?: "",
                    email = prefs?.getString(KEY_EMAIL, "") ?: "",
                    bio = prefs?.getString(KEY_BIO, "") ?: "",
                    avatarUrl = prefs?.getString(KEY_AVATAR_URL, null),
role = prefs?.getString(KEY_ROLE, "user") ?: "user",
                    uploadAuthorized = prefs?.getBoolean(KEY_UPLOAD_AUTHORIZED, false) ?: false
                )
                // 初始化 ApiClient Token
                ApiClient.init { token }
            }
            Log.d(TAG, "Restored login: userId=$userId")
        }
    }

    /**
     * 注册
     */
    suspend fun register(username: String, email: String, password: String): AuthResult {
        val result = ApiClient.post("auth/register", JSONObject().apply {
            put("username", username)
            put("email", email)
            put("password", password)
        }.toString())
        return when (result) {
            is ApiResult.Success -> {
                val json = JSONObject(result.body)
                val token = json.optString("token", "")
                val userJson = json.optJSONObject("user")
                val userInfo = userJson?.let { parseUserInfo(it) }
                if (token.isNotBlank()) saveAuth(token, userInfo)
                Log.d(TAG, "Register success: ${userInfo?.username}")
                AuthResult.Success(userInfo)
            }
            is ApiResult.Error -> AuthResult.Error(result.message)
        }
    }

    /**
     * 登录
     */
    suspend fun login(username: String, password: String): AuthResult {
        val result = ApiClient.post("auth/login", JSONObject().apply {
            put("username", username)
            put("password", password)
        }.toString())
        return when (result) {
            is ApiResult.Success -> {
                val json = JSONObject(result.body)
                val token = json.optString("token", "")
                val userJson = json.optJSONObject("user")
                val userInfo = userJson?.let { parseUserInfo(it) }
                if (token.isNotBlank()) saveAuth(token, userInfo)
                Log.d(TAG, "Login success: ${userInfo?.username}")
                AuthResult.Success(userInfo)
            }
            is ApiResult.Error -> AuthResult.Error(result.message)
        }
    }

    /**
     * 登出
     */
    fun logout() {
        prefs?.edit()?.clear()?.apply()
        _userState.value = null
        ApiClient.init { null }
        Log.d(TAG, "Logged out")
    }

    /**
     * 获取当前 Token
     */
    fun getToken(): String? = prefs?.getString(KEY_TOKEN, null)

    /**
     * 获取当前用户信息
     */
    fun getUserInfo(): UserInfo? = _userState.value

    private fun saveAuth(token: String, userInfo: UserInfo?) {
        prefs?.edit()?.apply {
            putString(KEY_TOKEN, token)
            if (userInfo != null) {
                putInt(KEY_USER_ID, userInfo.id)
                putString(KEY_USERNAME, userInfo.username)
                putString(KEY_EMAIL, userInfo.email)
                putString(KEY_BIO, userInfo.bio ?: "")
                putString(KEY_AVATAR_URL, userInfo.avatarUrl)
                putString(KEY_ROLE, userInfo.role)
                putBoolean(KEY_UPLOAD_AUTHORIZED, userInfo.uploadAuthorized)
            }
            apply()
        }
        _userState.value = userInfo
        ApiClient.init { token }
    }

    private fun parseUserInfo(json: JSONObject): UserInfo {
        return UserInfo(
            id = json.optInt("id", 0),
            username = json.optString("username", ""),
            email = json.optString("email", ""),
            bio = json.optString("bio", ""),
            avatarUrl = json.opt("avatar_url")?.toString(),
            role = json.optString("role", "user"),
            uploadAuthorized = json.optBoolean("upload_authorized", false)
        )
    }
}

/**
 * 用户信息数据类
 */
data class UserInfo(
    val id: Int,
    val username: String,
    val email: String,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val role: String = "user",
    val uploadAuthorized: Boolean = false
)

/**
 * 认证操作结果密封类
 */
sealed class AuthResult {
    data class Success(val user: UserInfo?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}