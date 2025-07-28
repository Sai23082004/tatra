package com.mahiya.safegas.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit

object TokenManager {
    private const val PREF_NAME = "SafeGas"
    private const val KEY_ACCESS_TOKEN = "auth_token"  // Match LoginScreen key
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_USER_EMAIL = "user_email"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveTokens(context: Context, accessToken: String, refreshToken: String) {
        getPrefs(context).edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
        Log.d("TokenManager", "Tokens saved - Access: ${accessToken.take(20)}...")
    }

    fun getAccessToken(context: Context): String? {
        val token = getPrefs(context).getString(KEY_ACCESS_TOKEN, null)
        Log.d("TokenManager", "Retrieved token: ${token?.take(20) ?: "null"}...")
        return token
    }

    fun saveUserEmail(context: Context, email: String) {
        getPrefs(context).edit().apply {
            putString(KEY_USER_EMAIL, email)
            apply()
        }
    }

    fun getUserEmail(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_EMAIL, null)
    }

    fun clearTokens(context: Context) {
        getPrefs(context).edit { clear() }
        Log.d("TokenManager", "All tokens cleared")
    }

    fun isLoggedIn(context: Context): Boolean {
        val token = getAccessToken(context)
        val isLoggedIn = !token.isNullOrEmpty()
        Log.d("TokenManager", "Is logged in: $isLoggedIn")
        return isLoggedIn
    }
}
