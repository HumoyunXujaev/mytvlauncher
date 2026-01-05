package com.humoyun.mytvlauncher

import android.graphics.Color
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.ImageCardView
import androidx.leanback.widget.Presenter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

class AppPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val cardView = ImageCardView(parent.context).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setBackgroundColor(Color.TRANSPARENT)
            setMainImageDimensions(320, 180)
        }
        return ViewHolder(cardView)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val app = item as AppInfo
        val cardView = viewHolder.view as ImageCardView

        cardView.titleText = app.label
        cardView.contentText = app.packageName

        Glide.with(cardView.context)
            .load(cardView.context.packageManager.getApplicationIcon(app.packageName))
            .transition(DrawableTransitionOptions.withCrossFade()) // Плавное появление
            .error(android.R.drawable.sym_def_app_icon)
            .into(cardView.mainImageView)

        updateCardBackgroundColor(cardView, false)
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val cardView = viewHolder.view as ImageCardView
        Glide.with(cardView.context).clear(cardView.mainImageView)
        cardView.mainImage = null
    }

    private fun updateCardBackgroundColor(view: ImageCardView, selected: Boolean) {
        val colorRes = if (selected) R.color.card_background_selected else R.color.card_background_default
        val color = ContextCompat.getColor(view.context, colorRes)
        view.setInfoAreaBackgroundColor(color)
    }
}