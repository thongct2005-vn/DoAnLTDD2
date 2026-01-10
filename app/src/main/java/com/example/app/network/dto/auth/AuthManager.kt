package com.example.app.network.dto.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object AuthManager {
    private const val PREF_NAME = "auth_pref"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_IS_FIRST_LOGIN = "is_first_login"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    // --- ACCESS TOKEN ---
    fun saveAccessToken(token: String) {
        prefs.edit { putString(KEY_ACCESS_TOKEN, token) }
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    // --- REFRESH TOKEN (Hàm bạn cần thêm) ---
    fun saveRefreshToken(token: String) {
        prefs.edit { putString(KEY_REFRESH_TOKEN, token) }
    }

    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    // --- TRẠNG THÁI KHÁC ---
    fun setFirstLogin(isFirst: Boolean) {
        prefs.edit { putBoolean(KEY_IS_FIRST_LOGIN, isFirst) }
    }

    fun isFirstLogin(): Boolean = prefs.getBoolean(KEY_IS_FIRST_LOGIN, true)

    // --- CLEAR DATA (Dùng khi Logout) ---
    fun clear() {
        prefs.edit {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }
}