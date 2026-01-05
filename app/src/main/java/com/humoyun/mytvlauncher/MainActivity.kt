package com.humoyun.mytvlauncher

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import com.humoyun.mytvlauncher.services.DefaultLauncherHelper
import com.humoyun.mytvlauncher.services.MyAccessibilityService
import com.humoyun.mytvlauncher.services.SharedPreference

class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var btnToggleLauncher: Button

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnToggleLauncher = findViewById(R.id.btnToggleLauncher)

        SharedPreference.init(this)

        btnToggleLauncher.setOnClickListener {
            if (SharedPreference.isLauncherActive) {
                disableLauncherFlow()
            } else {
                enableLauncherFlow()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (SharedPreference.isLauncherActive) {
                } else {
                    finish()
                }
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        updateButtonUI()

        if (SharedPreference.isLauncherActive) {
            checkPermissionsAndRedirectIfNeeded()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun updateButtonUI() {
        val isServiceRunning = isAccessibilityServiceEnabled(MyAccessibilityService::class.java)

        if (!isServiceRunning && SharedPreference.isLauncherActive) {
            SharedPreference.isLauncherActive = false
        }

        if (SharedPreference.isLauncherActive) {
            btnToggleLauncher.text = "Отключить лаунчер"
            btnToggleLauncher.setBackgroundColor(getColor(android.R.color.holo_red_dark))
        } else {
            btnToggleLauncher.text = "Включить лаунчер"
            btnToggleLauncher.setBackgroundColor(getColor(android.R.color.holo_green_dark))
        }
    }

    private fun checkPermissionsAndRedirectIfNeeded() {
        val helper = DefaultLauncherHelper(this)
        val isDefault = helper.isDefaultLauncher()
        val isAccessibility = isAccessibilityServiceEnabled(MyAccessibilityService::class.java)

        if (!isDefault && !isAccessibility) {
            if (!Utils.isMyLauncherDefault(this)) {
                startActivity(Intent(this, SetupActivity::class.java))
                finish()
            }
        }
        SharedPreference.isLauncherByUser = true
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun disableLauncherFlow() {
        SharedPreference.isLauncherActive = false

        Toast.makeText(this, "Пожалуйста, выключите MyTvLauncher в настройках", Toast.LENGTH_LONG).show()

        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Не удалось открыть настройки", Toast.LENGTH_SHORT).show()
        }

        updateButtonUI()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun enableLauncherFlow() {
        if (isAccessibilityServiceEnabled(MyAccessibilityService::class.java)) {
            SharedPreference.isLauncherActive = true
            SharedPreference.isLauncherByUser = true
            Toast.makeText(this, "Лаунчер активирован", Toast.LENGTH_SHORT).show()
            updateButtonUI()
        } else {
            SharedPreference.isLauncherActive = true // Предварительно ставим true
            SharedPreference.isLauncherByUser = true

            Toast.makeText(this, "Включите MyTvLauncher в списке", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            } catch (e: Exception) {
                startActivity(Intent(this, SetupActivity::class.java))
            }
        }
    }

    private fun isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>): Boolean {
        val expectedComponent = ComponentName(this, serviceClass).flattenToString()
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
    }
}