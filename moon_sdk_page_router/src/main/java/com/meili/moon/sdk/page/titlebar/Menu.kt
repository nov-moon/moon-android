package com.meili.moon.sdk.page.titlebar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meili.moon.sdk.util.app
import kotlinx.android.extensions.LayoutContainer

/**
 * menu的基类
 */
abstract class Menu : LayoutContainer {
    val rootView: ViewGroup by lazy {
        return@lazy LayoutInflater.from(app).inflate(getLayoutResId(), null, false) as ViewGroup
    }

    lateinit var redTips: View

    override val containerView: View? = rootView

    abstract fun getLayoutResId(): Int

    fun setVisibility(visibility: Int) {
        this.rootView.visibility = visibility
    }

    fun setEnabled(enabled: Boolean) {
        this.rootView.isEnabled = enabled
    }

    fun setClickable(clickable: Boolean) {
        this.rootView.isClickable = clickable
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        this.rootView.setOnClickListener(listener)
    }

    fun setVisibleRedTips(visible: Boolean) {
        redTips.visibility = if (visible) View.VISIBLE else View.GONE
    }
}
