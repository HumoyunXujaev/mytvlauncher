package com.humoyun.mytvlauncher

import android.accessibilityservice.AccessibilityService
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.humoyun.mytvlauncher.services.DefaultLauncherHelper
import com.humoyun.mytvlauncher.services.MyAccessibilityService
import com.humoyun.mytvlauncher.services.SharedPreference

class SetupActivity : FragmentActivity() {

    private lateinit var defaultLauncherHelper: DefaultLauncherHelper

    private var accessibilityLaunchListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkStatusAndFinish()
    }

    private var roleLaunchListener = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        checkStatusAndFinish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)

        defaultLauncherHelper = DefaultLauncherHelper(this)

        val btnSetDefault = findViewById<Button>(R.id.btnSetDefault)
        val btnOpenSettings = findViewById<Button>(R.id.btnOpenSettings)

        btnSetDefault.setOnClickListener {
            if (defaultLauncherHelper.canRequestDefaultLauncher()) {
                val intent = defaultLauncherHelper.requestDefaultLauncherIntent()
                if (intent != null) {
                    roleLaunchListener.launch(intent)
                }
            } else {
                openSystemHomeSettings()
            }
        }

        btnOpenSettings.setText("Включить Accessibility Service")
        btnOpenSettings.setOnClickListener {
            openAccessibilitySettings()
        }
    }

    override fun onResume() {
        super.onResume()
        checkStatusAndFinish()
    }

    private fun isAccessibilityServiceEnabled(serviceClass: Class<out AccessibilityService>): Boolean {
        val expectedComponent = ComponentName(this, serviceClass).flattenToString()
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.split(":").any { it.equals(expectedComponent, ignoreCase = true) }
    }

    private fun checkStatusAndFinish() {
        val isDefault = defaultLauncherHelper.isDefaultLauncher()
        val isAccessibilityEnabled = isAccessibilityServiceEnabled(MyAccessibilityService::class.java)

        if (isAccessibilityEnabled || isDefault) {
            SharedPreference.isLauncherByUser = true
            SharedPreference.isLauncherActive = true

            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun openSystemHomeSettings() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
            } catch (e2: Exception) {
                try {
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                } catch (e3: Exception) {
                    Toast.makeText(this, "Настройки недоступны", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openAccessibilitySettings() {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            accessibilityLaunchListener.launch(intent)
            Toast.makeText(this, "Найдите MyTvLauncher и включите его", Toast.LENGTH_LONG).show()
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Настройки Accessibility не найдены", Toast.LENGTH_SHORT).show()
        }
    }
}