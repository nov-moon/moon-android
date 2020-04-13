package com.meili.moon.sdk.base.util

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View

/**
 * Created by imuto on 2018/12/4.
 */

/**循环模式*/
enum class LoopMode {
    /**不循环*/
    NONE,
    /**循环：头->尾  头->尾*/
    RESTART,
    /**循环：头->尾->头-尾*/
    REVERSE
}

/**
 * 做横向动画，如果view不再显示，则动画自动取消
 *
 * @param [toX] 横向位移的目标位置，相对于view的left
 * @param [duration] 动画持续时间，单位：毫秒，默认300毫秒
 * @param [loopMode] 动画循环类型，默认不循环
 *
 * @see [LoopMode]
 */
fun View?.animTransX(toX: Float, duration: Long = 300, loopMode: LoopMode = LoopMode.NONE): ValueAnimator? {
    return this?.animTransInner("translationX", toX, translationX, duration, loopMode)
}


/**
 * 做纵向动画，如果view不再显示，则动画自动取消
 *
 * @param [toY] 纵向位移的目标位置，相对于view的top
 * @param [duration] 动画持续时间，单位：毫秒，默认300毫秒
 * @param [loopMode] 动画循环类型，默认不循环
 *
 * @see [LoopMode]
 */
fun View?.animTransY(toY: Float, duration: Long = 300, loopMode: LoopMode = LoopMode.NONE): ValueAnimator? {
    return this?.animTransInner("translationY", toY, translationY, duration, loopMode)
}

private fun View.animTransInner(property: String, toValue: Float, oldValue: Float, duration: Long, loopMode: LoopMode): ValueAnimator? {
    val animator = ObjectAnimator.ofFloat(this, property, oldValue, toValue)
    when (loopMode) {
        LoopMode.RESTART -> {
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.RESTART
        }
        LoopMode.REVERSE -> {
            animator.repeatCount = ValueAnimator.INFINITE
            animator.repeatMode = ValueAnimator.REVERSE
        }
        else -> {
        }
    }

    animator.duration = duration

    var startTime = System.currentTimeMillis()

    animator.addUpdateListener {
        it.currentPlayTime
        if (System.currentTimeMillis() - startTime > 500) {
            startTime = System.currentTimeMillis()
            if (!hasShown()) {
                it.cancel()
            }
        }
    }

    animator.start()

    return animator
}