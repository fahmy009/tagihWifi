package com.example.tagihinternet.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.tagihinternet.data.NetworkConfig
import com.example.tagihinternet.data.entity.Role
import com.example.tagihinternet.data.entity.User

object PreferenceHelper {
    private const val PREF_NAME = "tagih_internet_prefs"
    private const val KEY_GAS_URL = "gas_url"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_CODE = "user_code"
    private const val KEY_USERNAME = "username"
    private const val KEY_PASSWORD = "password"
    private const val KEY_ROLE = "role"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getGasUrl(context: Context): String {
        return getPrefs(context).getString(KEY_GAS_URL, NetworkConfig.GAS_URL) ?: NetworkConfig.GAS_URL
    }

    fun setGasUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_GAS_URL, url).apply()
    }

    fun saveSession(context: Context, user: User) {
        getPrefs(context).edit().apply {
            putLong(KEY_USER_ID, user.id)
            putString(KEY_USER_CODE, user.userCode)
            putString(KEY_USERNAME, user.username)
            putString(KEY_PASSWORD, user.password)
            putString(KEY_ROLE, user.role.name)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getSession(context: Context): User? {
        val prefs = getPrefs(context)
        val id = prefs.getLong(KEY_USER_ID, -1)
        val code = prefs.getString(KEY_USER_CODE, "") ?: ""
        val username = prefs.getString(KEY_USERNAME, null)
        val password = prefs.getString(KEY_PASSWORD, null)
        val roleStr = prefs.getString(KEY_ROLE, null)

        return if (id != -1L && username != null && password != null && roleStr != null) {
            User(id, code, username, password, Role.valueOf(roleStr))
        } else {
            null
        }
    }

    fun clearSession(context: Context) {
        val biometricEnabled = isBiometricEnabled(context)
        getPrefs(context).edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, false)
            // Jika biometrik tidak aktif, hapus semua kredensial
            if (!biometricEnabled) {
                remove(KEY_USER_ID)
                remove(KEY_USER_CODE)
                remove(KEY_USERNAME)
                remove(KEY_PASSWORD)
                remove(KEY_ROLE)
            }
            apply()
        }
    }

    fun isBiometricEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }
}
