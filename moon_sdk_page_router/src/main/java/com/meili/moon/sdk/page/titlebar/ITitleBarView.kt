package com.meili.moon.sdk.page.titlebar

import android.graphics.drawable.Drawable
import android.support.annotation.StyleRes
import com.meili.moon.sdk.page.internal.animators.PageAnimator

/**
 * titleBar的标准接口
 *
 *
 * 主要提供设置title、backText、添加menu
 * Created by imuto on 16/5/20.
 */
interface ITitleBarView {

    var titleBarVisibility: Int

    fun setTitle(text: CharSequence)

    fun setTitle(resId: Int)

    fun setCloseMenuVisible(visible: Int)

    fun setTitleTextColor(color: Int)

    fun setBackIcon(resId: Int)

    fun setBackIcon(drawable: Drawable)

    fun setBackIconVisible(visible: Int)

    fun setBackText(resId: Int)

    fun setBackText(text: CharSequence)

    fun setBackTextColor(resId: Int)

    fun setTitleBarBackgroundColor(color: Int)

    fun setTitleBarBackgroundDrawable(resId: Int)

    fun addMenu(menu: Menu)

    fun removeMenu(menu: Menu)

    /** 设置是否支持无网络连接的View  */
    fun setSupportNoNetworkStyle(isSupport: Boolean)

    /** 设置样式  */
    fun setStyle(@StyleRes style: Int)

    interface ImplView : ITitleBarView, PageAnimator {

        /** 获取页面动画的标识: PAGE_ANIM_FLAG_ALPHA(0)，透明度渐变动画。PAGE_ANIM_FLAG_SLID(1)，右划动画。NO_PAGE_ANIM_FLAG(100)，无动画  */
        /** 设置页面动画的标识: 0，透明度渐变动画。1，右划动画。100，无动画  */
        var pageAnimatorFlag: Int

        /**获取titleBar的高度 */
        fun getTitleBarHeight(): Int

        fun setBackClickListener(lis: ()->Unit)

        fun setCloseMenuClickListener(lis: ()->Unit)

        fun setOnTitleDoubleClickListener(lis: ()->Unit)

        companion object {

            /** 没有动画  */
            val PAGE_ANIM_FLAG_NONE = 100
            /** 带有alpha的动画  */
            val PAGE_ANIM_FLAG_ALPHA = 0
            /** 左右拖拽的动画  */
            val PAGE_ANIM_FLAG_SLID = 1
        }
    }
}
