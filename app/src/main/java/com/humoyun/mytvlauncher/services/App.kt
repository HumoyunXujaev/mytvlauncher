package com.humoyun.mytvlauncher

import android.app.Application
import com.humoyun.mytvlauncher.services.SharedPreference

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPreference.init(this)
        if (!SharedPreference.isLauncherByUser) {
            SharedPreference.isLauncherByUser = true
        }
    }
}