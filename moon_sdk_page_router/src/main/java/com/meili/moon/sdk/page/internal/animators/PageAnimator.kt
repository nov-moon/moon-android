package com.meili.moon.sdk.page.internal.animators

import android.view.View
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.page.internal.widget.RainbowPageRootView

const val PAGE_ANIMATOR_ALPHA = 0.8F

/**
 * 使用比例作为入参的可动画对象
 *
 * 可能是View，也可能是其他对象。主要用来规范可动画类型为使用比例
 */
interface RatioAnimatable {
    /**
     * 当页面执行进入动画时的动画进度回调。
     *
     * 进入动画的触发分两种情况：
     * 1. 新页面打开，新页面放到栈顶，新页面执行进入动画
     * 2. 栈顶页面关闭，栈顶下面的页面执行进入动画
     *
     * @param value 动画进度，0-1之间的float值
     * @param isPopBack 当前动画是否是由于popBack操作触发的
     */
    fun onPageAnimIn(value: Float, isPopBack: Boolean)

    /**
     * 当页面执行退出动画时的动画进度回调。
     *
     * 退出动画的触发分两种情况：
     * 1. 新页面打开，新页面放到top位置，老页面执行退出动画
     * 2. top页面关闭，top页面执行退出动画
     *
     * @param value 动画进度，0-1之间的float值
     * @param isPopBack 当前动画是否是由于popBack操作触发的
     */
    fun onPageAnimOut(value: Float, isPopBack: Boolean)

}

/**
 * Created by imuto on 2019-06-11.
 */
interface PageAnimator : RatioAnimatable {

    /**设置页面动画是否可用*/
    fun setPageAnimatorEnable(enable: Boolean)

    /**页面动画是否可用*/
    fun isPageAnimatorEnable(): Boolean
}


abstract class AbsPageLeftRightAnimator : PageAnimator {

    private var enable = true

    private var animViews = mutableListOf<View>()

    fun addAnimView(view: View) {
        animViews.add(view)
    }

    final override fun onPageAnimIn(value: Float, isPopBack: Boolean) {
        if (!enable) return

        animViews.forEach { view ->

            val width = getViewWidth(view)

            val curr = 1 - value
            if (isPopBack) {
                view.translationX = -(curr * (width / 3))
            } else {
                view.translationX = (1 - value) * width
            }

            if (view is RainbowPageRootView) {
                view.onPageAnimIn(value, isPopBack)
            }

            onPageAnimInInternal(curr, isPopBack, width)
        }
    }

    abstract fun onPageAnimInInternal(value: Float, isPopBack: Boolean, width: Int)


    final override fun onPageAnimOut(value: Float, isPopBack: Boolean) {
        if (!enable) return

        animViews.forEach { view ->

            val width = getViewWidth(view)

            if (isPopBack) {
                view.translationX = value * width
            } else {
                view.translationX = -(value * (width / 3))
            }

            if (view is RainbowPageRootView) {
                view.onPageAnimOut(value, isPopBack)
            }

            onPageAnimOutInternal(value, isPopBack, width)
        }

    }

    abstract fun onPageAnimOutInternal(value: Float, isPopBack: Boolean, width: Int)


    private fun getViewWidth(view: View): Int {
        val screenWidth = CommonSdk.app().resources.displayMetrics.widthPixels
        return if (view.width > screenWidth) {
            screenWidth
        } else view.width
    }

    override fun setPageAnimatorEnable(enable: Boolean) {
        this.enable = enable
    }

    override fun isPageAnimatorEnable(): Boolean = enable
}


open class PageLeftRightAnimator : AbsPageLeftRightAnimator() {

    protected var mAlphaView: View? = null

    override fun onPageAnimInInternal(value: Float, isPopBack: Boolean, width: Int) {
        if (mAlphaView?.visibility != View.VISIBLE && isPopBack) {
            mAlphaView?.visibility = View.VISIBLE
            mAlphaView?.bringToFront()
        }

        if (isPopBack) {
            mAlphaView?.alpha = value * PAGE_ANIMATOR_ALPHA
        } else {
            mAlphaView?.alpha = 0.01F
        }

        if (value <= 0F) {
            mAlphaView?.visibility = View.INVISIBLE
        }
    }

    override fun onPageAnimOutInternal(value: Float, isPopBack: Boolean, width: Int) {
        if (mAlphaView?.visibility != View.VISIBLE && !isPopBack) {
            mAlphaView?.visibility = View.VISIBLE
            mAlphaView?.bringToFront()
        }
        if (isPopBack) {
            mAlphaView?.alpha = 0.01F
        } else {
            mAlphaView?.alpha = value * PAGE_ANIMATOR_ALPHA
        }
    }
}

class PageAnimatorWrapper : PageAnimator {

    lateinit var realInstance: PageAnimator
    /**
     * 当页面执行进入动画时的动画进度回调。
     *
     * 进入动画的触发分两种情况：
     * 1. 新页面打开，新页面放到栈顶，新页面执行进入动画
     * 2. 栈顶页面关闭，栈顶下面的页面执行进入动画
     *
     * @param value 动画进度，0-1之间的float值
     * @param isPopBack 当前动画是否是由于popBack操作触发的
     */
    override fun onPageAnimIn(value: Float, isPopBack: Boolean) {
        realInstance.onPageAnimIn(value, isPopBack)
    }

    /**
     * 当页面执行退出动画时的动画进度回调。
     *
     * 退出动画的触发分两种情况：
     * 1. 新页面打开，新页面放到top位置，老页面执行退出动画
     * 2. top页面关闭，top页面执行退出动画
     *
     * @param value 动画进度，0-1之间的float值
     * @param isPopBack 当前动画是否是由于popBack操作触发的
     */
    override fun onPageAnimOut(value: Float, isPopBack: Boolean) {
        realInstance.onPageAnimOut(value, isPopBack)
    }

    /**设置页面动画是否可用*/
    override fun setPageAnimatorEnable(enable: Boolean) = realInstance.setPageAnimatorEnable(enable)

    /**页面动画是否可用*/
    override fun isPageAnimatorEnable(): Boolean = realInstance.isPageAnimatorEnable()

}

