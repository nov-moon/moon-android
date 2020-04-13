package com.meili.moon.sdk.util

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation

/**
 * 动画相关工具
 * Created by imuto on 2019-08-14.
 */

fun View.fadeOut(time: Long, onEnd: (() -> Unit)? = null) {
    val fadeOut = AlphaAnimation(1f, 0f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeOut.visibility = View.GONE
            onEnd?.invoke()
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}

fun View.fadeIn(time: Long, onEnd: (() -> Unit)? = null) {
    val fadeOut = AlphaAnimation(0f, 1f)
    fadeOut.interpolator = AccelerateInterpolator()
    fadeOut.duration = time

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation) {
            this@fadeIn.visibility = View.VISIBLE
            onEnd?.invoke()
        }

        override fun onAnimationRepeat(animation: Animation) {}
        override fun onAnimationStart(animation: Animation) {}
    })

    this.startAnimation(fadeOut)
}