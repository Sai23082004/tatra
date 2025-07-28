package com.mahiya.safegas.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefsManager {
    private const val PREF_NAME = "SafeGasPrefs"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_AUTH_TOKEN = "auth_token"

    private lateinit var sharedPrefs: SharedPreferences

    fun init(context: Context) {
        sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun isLoggedIn(): Boolean {
        return sharedPrefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun saveAuthToken(token: String) {
        sharedPrefs.edit { putString(KEY_AUTH_TOKEN, token) }
    }

    fun getAuthToken(): String {
        return sharedPrefs.getString(KEY_AUTH_TOKEN, "") ?: ""
    }

    fun saveUserData(email: String, name: String, phone: String = "") {
        sharedPrefs.edit {
            putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_PHONE, phone)
                .putBoolean(KEY_IS_LOGGED_IN, true)
        }
    }

    fun clearUserData() {
        sharedPrefs.edit().clear().apply()
    }

    fun getUserEmail(): String {
        return sharedPrefs.getString(KEY_USER_EMAIL, "user@example.com") ?: "user@example.com"
    }

    fun getUserName(): String {
        return sharedPrefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun getUserPhone(): String {
        return sharedPrefs.getString(KEY_USER_PHONE, "+1 234-567-8900") ?: "+1 234-567-8900"
    }
}
