package com.humoyun.mytvlauncher

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BackgroundManager
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.util.Timer
import java.util.TimerTask

class MainFragment : VerticalGridSupportFragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var mAdapter: ArrayObjectAdapter

    private lateinit var backgroundManager: BackgroundManager
    private var defaultBackground: Drawable? = null
    private lateinit var mMetrics: DisplayMetrics
    private var backgroundTimer: Timer? = null
    private val BACKGROUND_UPDATE_DELAY = 300L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prepareBackgroundManager()
        setupUI()
        setupEventListeners()

        viewModel.loadInstalledApps()
        viewModel.appsList.observe(this) { apps ->
            mAdapter.clear()
            apps.forEach { mAdapter.add(it) }
        }
    }

    private fun prepareBackgroundManager() {
        backgroundManager = BackgroundManager.getInstance(requireActivity())
        backgroundManager.attach(requireActivity().window)

        defaultBackground = ContextCompat.getDrawable(requireContext(), R.color.launcher_background)
        backgroundManager.drawable = defaultBackground

        mMetrics = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(mMetrics)
    }

    private fun setupUI() {
        title = "Humoyun TV Launcher's Home"

        val gridPresenter = VerticalGridPresenter(FocusHighlight.ZOOM_FACTOR_MEDIUM)
        gridPresenter.numberOfColumns = 4
        gridPresenter.shadowEnabled = true
        setGridPresenter(gridPresenter)

        mAdapter = ArrayObjectAdapter(AppPresenter())
        adapter = mAdapter
    }

    private fun setupEventListeners() {
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is AppInfo) {
                try {
                    val intent = requireContext().packageManager.getLaunchIntentForPackage(item.packageName)
                    if (intent != null) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        setOnItemViewSelectedListener { _, item, _, _ ->
            if (item is AppInfo) {
                startBackgroundTimer(item)
            }
        }
    }

    private fun startBackgroundTimer(item: AppInfo) {
        backgroundTimer?.cancel()
        backgroundTimer = Timer()
        backgroundTimer?.schedule(object : TimerTask() {
            override fun run() {
                Handler(Looper.getMainLooper()).post {
                    updateBackground(item.packageName)
                }
            }
        }, BACKGROUND_UPDATE_DELAY)
    }

    private fun updateBackground(packageName: String) {
        Glide.with(this)
            .asDrawable()
            .load(requireContext().packageManager.getApplicationIcon(packageName))
            .centerCrop()
            .into(object : CustomTarget<Drawable>() {
                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    backgroundManager.drawable = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Можно вернуть дефолтный фон при очистке
                }
            })
    }

    override fun onResume() {
        super.onResume()
        if (::backgroundManager.isInitialized) {
            backgroundManager.drawable = backgroundManager.drawable
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        backgroundTimer?.cancel()
        if (::backgroundManager.isInitialized) {
            backgroundManager.release()
        }
    }
}