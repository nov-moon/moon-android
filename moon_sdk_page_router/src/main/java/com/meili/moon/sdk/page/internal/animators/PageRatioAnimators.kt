package com.meili.moon.sdk.page.internal.animators

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewConfiguration
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.page.PageAnimators
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.internal.SdkFragment
import com.meili.moon.sdk.page.internal.utils.isDirty

private const val DURATION = 400

/**
 * 页面比例动画管理器
 * Created by imuto on 2018/4/11.
 */
class PageRatioAnimators(private val fragment: SdkFragment) : PageAnimators {

    private var transitRatio = 0.0F
    private var minFinishPageVelocity = 2000

    init {
        val viewConfig = ViewConfiguration.get(CommonSdk.app())
        var minVelocity = viewConfig.scaledMinimumFlingVelocity * 20
        if (minVelocity > viewConfig.scaledMaximumFlingVelocity) {
            minVelocity = viewConfig.scaledMaximumFlingVelocity / 2
        }
        minFinishPageVelocity = minVelocity
    }

    private val prePage: SdkFragment?
        get() {
            return if (fragment == PageManagerImpl.getTopPage()) {
                fragment.getPrePage(false) as? SdkFragment
            } else {
                null
            }
        }

    /**
     * 设置当前过渡动画已完成比例
     *
     * [ratio] 取值范围为0-1的float值
     * [isClose] 是否在执行关闭动作
     */
    fun doTransit(ratio: Float, isClose: Boolean) {

        if (!fragment.isSlideFinish || fragment.isFinishing()) return

        val prePage = this.prePage ?: return
        val prePageView = prePage.view ?: return

        transitRatio = ratio
        prePage.interactive = false
        fragment.interactive = false

        if (isClose) {
            fragment.onPageAnimOut(ratio, true)
        } else {
            // 1 - 手指所在屏幕比例 = 动画已完成比例，也就是起始比例位置
            fragment.onPageAnimIn(1 - ratio, false)
        }

        if (prePageView.visibility != View.VISIBLE) {
            prePageView.visibility = View.VISIBLE
        }

        if (isClose) {
            prePage.onPageAnimIn(ratio, true)
        } else {
            // 1 - 手指所在屏幕比例 = 动画已完成比例，也就是起始比例位置
            prePage.onPageAnimOut(1 - ratio, false)
        }

        fragment.interactive = true
        prePage.interactive = true
    }

    /**做动画结束逻辑*/
    fun doComplete(ratio: Float, velocity: Float) {

        if (!fragment.isSlideFinish || fragment.isFinishing()) return

        val prePage = this.prePage ?: return

        val pageRatioAnimators = prePage.pageAnimators as? PageRatioAnimators
                ?: return

        transitRatio = ratio

        pageRatioAnimators.setLastRatio(transitRatio)

        if (transitRatio > 0.4F || velocity > minFinishPageVelocity) {
            fragment.finish()
            if (fragment.containerId != prePage.containerId) {
                val prePageAnimator = pageRatioAnimators.getPageAnimator(true, true, 0)
                prePageAnimator.setTarget(prePage.view)
                prePageAnimator.start()
            }
            return
        }

        // 当前页面执行进入动画，用1 - 当前手指所在的屏幕比例 = 已完成动画比例。进入动画从已完成比例开始执行
        transitRatio = 1 - ratio
        pageRatioAnimators.setLastRatio(1 - ratio)

        val pageAnimator = getPageAnimator(false, true, 0)
        pageAnimator.setTarget(fragment.view)
        pageAnimator.start()

        val prePageAnimator = pageRatioAnimators.getPageAnimator(false, false, 0)
        prePageAnimator.setTarget(prePage.view)
        prePageAnimator.start()
    }

    private fun setLastRatio(ratio: Float) {
        transitRatio = ratio
    }

    override fun getPageAnimator(isPopBack: Boolean, enter: Boolean, nextAnim: Int): Animator {

//        LogUtil.e("getPageAnimator fragment => $fragment")

        val animator = getAnimator {
            val animatedValue = it.animatedValue as Float
            if (enter) {
//                if (!fragment.isResumed) return@getAnimator
                val view = fragment.view
                /**这里之所以这么设置，是因为在动画之前会闪屏*/
                if (!isPopBack && animatedValue == 0.0F) {
                    if (view != null && view.visibility != View.INVISIBLE) {
                        view.visibility = View.INVISIBLE
                    }
                } else {
                    if (view != null && view.visibility != View.VISIBLE) {
                        view.visibility = View.VISIBLE
                    }
                }
                fragment.onPageAnimIn(animatedValue, isPopBack)
            } else {
                // 处理dirty情况下的动画错误
                if (fragment.isDirty && !enter && isPopBack) {
                    val mView = fragment.mView ?: return@getAnimator
                    mView.translationX = 1.0F * mView.width
                } else {
                    fragment.onPageAnimOut(animatedValue, isPopBack)
                }
            }
        }

        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                transitRatio = 0F
                if (!enter) {
                    val view = fragment.view
                    if (view != null && view.visibility != View.INVISIBLE) {
                        view.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {

            }
        })

//        val preFragment = prePage
//        //这种情况是发生在两个不同container过渡的情况下，上面container动画开始执行了，但是下面的container动画没有被吊起。我们手动吊起一下
//        if (preFragment != null && preFragment.containerId != fragment.containerId) {
//            preFragment.pageAnimators?.getPageAnimator(isPopBack, !enter, nextAnim)?.start()
//        }

        return animator
    }

    private fun getAnimator(listener: (ValueAnimator) -> Unit): Animator {
        var duration = DURATION * (1 - transitRatio)
        if (duration < 0) {
            duration = 0F
        }

        val animator = ValueAnimator.ofFloat(transitRatio, 1.0F)
        animator.duration = duration.toLong()
        animator.addUpdateListener(listener)

        return animator
    }

}