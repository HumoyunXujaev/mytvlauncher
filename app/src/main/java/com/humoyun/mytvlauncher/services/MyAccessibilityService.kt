package com.humoyun.mytvlauncher.services

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.humoyun.mytvlauncher.MainActivity

class MyAccessibilityService : AccessibilityService() {

    private val TAG: String = "MyAccessibilityService"
    private val validHomeSources = setOf(
        "com.android.systemui",
        "com.android.tvlauncher",
        "com.google.android.tvlauncher",
        "com.google.android.apps.tv.launcherx",
        "com.sony.dtv.homelauncher",
        "com.amazon.firelauncher",
        "com.google.android.leanbacklauncher"
    )

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Service Connected")
        SharedPreference.init(applicationContext)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!SharedPreference.isLauncherActive) return

        event?.run {
            when (eventType) {
                AccessibilityEvent.TYPE_VIEW_CLICKED -> handleViewClick()
                AccessibilityEvent.TYPE_GESTURE_DETECTION_START -> handleGesture()
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    val packageName = event.packageName?.toString()
                    if (isSystemLauncher(packageName)) {
                        launchOurLauncher()
                    }
                }
                else -> Unit
            }
        }
    }

    override fun onInterrupt() {}

    private fun launchOurLauncher() {
        if (!SharedPreference.isLauncherActive) return
        if (!SharedPreference.isLauncherByUser) return

        try {
            startActivity(homeIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching home via intent", e)
            // Fallback
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                startActivity(launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
    }

    private val homeIntent by lazy {
        Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    private fun AccessibilityEvent.handleViewClick() {
        val source = source ?: return
        val isHomeButton = source.viewIdResourceName?.let { id ->
            id.contains("home", true) || validHomeSources.contains(packageName?.toString())
        } ?: false

        if (isHomeButton) {
            launchOurLauncher()
        }
        source.recycle()
    }

    private fun AccessibilityEvent.handleGesture() {
        if (contentChangeTypes.and(AccessibilityEvent.CONTENT_CHANGE_TYPE_PANE_DISAPPEARED) != 0) {
            Handler(Looper.getMainLooper()).postDelayed({ launchOurLauncher() }, 50)
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!SharedPreference.isLauncherActive) return false

        if (event.keyCode == KeyEvent.KEYCODE_HOME) {
            if (event.action == KeyEvent.ACTION_UP) {
                Handler(Looper.getMainLooper()).postDelayed({ launchOurLauncher() }, 30)
            }
            return true
        }
        return super.onKeyEvent(event)
    }

    private fun isSystemLauncher(pkg: CharSequence?): Boolean {
        return validHomeSources.contains(pkg?.toString())
    }
}