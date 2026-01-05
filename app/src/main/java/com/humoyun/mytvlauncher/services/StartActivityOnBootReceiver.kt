package com.humoyun.mytvlauncher.services

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.humoyun.mytvlauncher.MainActivity

class StartActivityOnBootReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")

    override fun onReceive(context: Context, intent: Intent?) {
        if (SharedPreference.isLauncherByUser && SharedPreference.isLauncherActive) {
            val myIntent = Intent(context, MainActivity::class.java)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(myIntent)
        }
    }
}