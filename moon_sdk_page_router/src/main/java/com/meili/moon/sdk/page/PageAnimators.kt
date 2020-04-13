package com.meili.moon.sdk.page

import android.animation.Animator

/**
 * 页面跳转动画管理器
 *
 * Created by imuto on 2018/4/11.
 */
interface PageAnimators {

    fun getPageAnimator(isPopBack: Boolean, enter: Boolean, nextAnim: Int): Animator

}