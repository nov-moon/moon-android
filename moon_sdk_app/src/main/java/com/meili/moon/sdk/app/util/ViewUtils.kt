package com.meili.moon.sdk.app.util

import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.statusBarHeight

/**
 * Created by imuto on 2018/7/7.
 */

val titleBarHeight: Int
    get() = Sdk.app().resources
            .getDimensionPixelOffset(R.dimen.moon_sdk_app_title_bar_height) + statusBarHeight


fun View.fadeOut(time: Long) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeOut.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}


fun View.fadeIn(time: Long) {
    val fadeOut = AlphaAnimation(0f, 1f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeIn.visibility = View.VISIBLE
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}


fun ViewGroup.fadeOut(time: Long) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeOut.visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}


fun ViewGroup.fadeIn(time: Long) {
    val fadeOut = AlphaAnimation(0f, 1f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeIn.visibility = View.VISIBLE
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}