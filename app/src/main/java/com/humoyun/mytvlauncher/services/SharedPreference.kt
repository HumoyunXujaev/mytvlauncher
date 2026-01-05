package com.humoyun.mytvlauncher.services

import android.content.Context
import android.content.SharedPreferences

object SharedPreference {
    private const val PREF_NAME = "app_prefs"
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    var isLauncherByUser: Boolean
        get() = prefs.getBoolean("is_launcher_by_user", false)
        set(value) = prefs.edit().putBoolean("is_launcher_by_user", value).apply()

    var isLauncherActive: Boolean
        get() = prefs.getBoolean("is_launcher_active", true) // По умолчанию включен
        set(value) = prefs.edit().putBoolean("is_launcher_active", value).apply()
}