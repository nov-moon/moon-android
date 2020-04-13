package com.meili.moon.sdk.page.internal.widget

import android.content.Context
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.internal.animators.AbsPageLeftRightAnimator
import com.meili.moon.sdk.page.internal.animators.PageAnimator
import com.meili.moon.sdk.page.internal.animators.PageLeftRightAnimator
import com.meili.moon.sdk.page.internal.animators.RatioAnimatable
import com.meili.moon.sdk.util.MATCH_PARENT
import com.meili.moon.sdk.util.childrenForeach
import com.meili.moon.sdk.util.getDrawable

/**
 * Created by imuto on 2019-09-10.
 */
class RainbowPageRootView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), PageAnimator {

    private var mShadowView: View? = null
    private var mMainAnimateView: View? = null
    private var mCurrentRatio = 0F

    private var _animationEnable = false

    /**
     * 页面左侧的阴影是否可用，默认可用
     */
    var pageLeftShadowEnable = true


    /**在页面过渡的真正执行对象*/
    private var pageTransitionAnimator: AbsPageLeftRightAnimator = PageLeftRightAnimator()

    /**
     * 设置页面左边的阴影
     */
    fun setPageLeftShadowDrawableResource(@DrawableRes drawableId: Int) {
        val drawable = getDrawable(drawableId)
//        mShadowLeft = drawable
        if (mShadowView != null) {
            removeView(mShadowView)
        }

        mShadowView = View(context)
        mShadowView?.background = drawable
        val shadowHeight = mMainAnimateView?.measuredHeight ?: MATCH_PARENT
        val layoutParams = LayoutParams(drawable!!.intrinsicWidth, shadowHeight)
        layoutParams.addRule(ALIGN_PARENT_LEFT)
        layoutParams.leftMargin = -drawable.intrinsicWidth
        addView(mShadowView, layoutParams)
    }


    /**
     * 设置主的动画View
     *
     * 当动画执行时，会遍历子view是否为[RatioAnimatable]类型，如果是此类型则执行子View的动画。
     * 但是MainAnimateView是一个例外，不管他是不是上述类型，都会执行动画
     */
    fun setMainAnimateView(view: View, layoutParams: LayoutParams? = null) {
        mMainAnimateView = view
        addView(view, layoutParams)

        post {
            val mShadowViewVal = mShadowView
            if (mShadowViewVal != null) {
                if (view.id == View.NO_ID) {
                    R.id.moonRainbowContentView
                    view.id = R.id.moonRainbowContentView
                }

                (mShadowViewVal.layoutParams as LayoutParams).addRule(ALIGN_TOP, view.id)
                mShadowViewVal.requestLayout()
            }
        }
    }

    /**
     * 添加view，如果当前添加的View是RatioAnimatable类型，则不管理其动画效果，否则其管理动画
     */
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (child != null && child !is RatioAnimatable) {
            pageTransitionAnimator.addAnimView(child)
        }
        super.addView(child, index, params)
    }

    /**
     * 获取主动画View
     */
    fun getMainAnimateView() = mMainAnimateView

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
        mCurrentRatio = if (isPopBack) {
            -1F
        } else {
            1 - value
        }
        childrenForeach {
            if (it == mMainAnimateView) {
                pageTransitionAnimator.onPageAnimIn(value, isPopBack)
            } else if (it is RatioAnimatable) {
                it.onPageAnimIn(value, isPopBack)
            }
        }

        if (pageLeftShadowEnable && mShadowView != null) {
            mShadowView?.translationX = width * mCurrentRatio
        }
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
        mCurrentRatio = if (!isPopBack) {
            -1F
        } else {
            value
        }

        childrenForeach {
            if (it == mMainAnimateView) {
                pageTransitionAnimator.onPageAnimOut(value, isPopBack)
            } else if (it is RatioAnimatable) {
                it.onPageAnimOut(value, isPopBack)
            }
        }

        if (pageLeftShadowEnable && mShadowView != null) {
            mShadowView?.translationX = width * mCurrentRatio
        }
    }

    /**设置页面动画是否可用*/
    override fun setPageAnimatorEnable(enable: Boolean) {
        _animationEnable = enable
    }

    /**页面动画是否可用*/
    override fun isPageAnimatorEnable(): Boolean {
        return _animationEnable
    }

    override fun setVisibility(visibility: Int) {
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        super.setVisibility(visibility)
        descendantFocusability = ViewGroup.FOCUS_BEFORE_DESCENDANTS
    }

}