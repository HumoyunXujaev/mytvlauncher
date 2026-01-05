package com.humoyun.mytvlauncher

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _appsList = MutableLiveData<List<AppInfo>>()
    val appsList: LiveData<List<AppInfo>> = _appsList

    fun loadInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager

            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)

            val allApps = pm.queryIntentActivities(intent, 0)

            val apps = allApps.mapNotNull { resolveInfo ->
                val pkgName = resolveInfo.activityInfo.packageName
                if (pkgName == getApplication<Application>().packageName) {
                    null
                } else {
                    AppInfo(
                        label = resolveInfo.loadLabel(pm).toString(),
                        packageName = pkgName
                    )
                }
            }.sortedBy { it.label } // Сортировка А-Я

            _appsList.postValue(apps)
        }
    }
}