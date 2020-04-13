package com.meili.moon.sdk.page.internal.animators

import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.page.Page
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.internal.SdkFragment

/**
 * 边缘touch事件处理器
 * Created by imuto on 2018/4/11.
 */
object EdgeTouchHolder {
    private val screenWidth = CommonSdk.app().resources.displayMetrics.widthPixels

    /**最小的X轴touch拦截值*/
    private const val MIN_X = 100

    /**最小的y轴拦截touch的值*/
    var minHolderTouchY = 0

    /**拦截的触摸点总数*/
    private const val POINTER_COUNT = 1
    private var firstDownX = 0f
    private var lastX = 0f
    private var lastPage: Page? = null
    private var isInterceptTouch = true
    private var canInterceptTouch = false

    /**加速器记录参数*/
    private var mVelocityTracker = VelocityTracker.obtain()
    private var minVelocity = 0
    private var maxVelocity = 0
    private var touchSlop = 0
    private var isScrolling = false

    private var mLastMotionX = 0

    private val clearLambda: () -> Unit = {
        isInterceptTouch = false
        canInterceptTouch = false
        mVelocityTracker.clear()
        lastPage = null
        mLastMotionX = 0
    }

    init {
        //初始化系统标准touch和惯性计算器
        val viewConfig = ViewConfiguration.get(CommonSdk.app())
        minVelocity = viewConfig.scaledMinimumFlingVelocity
        maxVelocity = viewConfig.scaledMaximumFlingVelocity
        touchSlop = viewConfig.scaledTouchSlop
    }

    fun intercept(event: MotionEvent): Boolean {
        val action = event.action

        val isMatchCount = event.pointerCount == POINTER_COUNT

        if (!isMatchCount) return false

        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                lastPage = PageManagerImpl.getTopPage()
                val sdkFragment = lastPage as? SdkFragment
                if (sdkFragment == null) {
                    clearLambda.invoke()
                    return false
                }

                // fragment打开了滑动关闭功能
                val isSlidFinish = sdkFragment.isSlideFinish
                if (!isSlidFinish) {
                    clearLambda.invoke()
                    return false
                }
                val isMatchX = event.x < MIN_X

                //滑动位置大于最小位置，如果fragment指定了y，则必须大于fragment指定的，否则必须大于全局设置的y
                val isMatchY = if (sdkFragment.slideFinishMinY > 0) {
                    event.y > sdkFragment.slideFinishMinY
                } else event.y > minHolderTouchY

                clearLambda.invoke()

                canInterceptTouch = isMatchX && isMatchY

                val x = event.x.toInt()
                mLastMotionX = x
                lastPage = PageManagerImpl.getTopPage()

                mVelocityTracker.addMovement(event)
            }
            MotionEvent.ACTION_MOVE -> {
                if (!canInterceptTouch) return false
                val x = event.x.toInt()
                val xDiff = Math.abs(x - mLastMotionX)

                val sdkFragment = lastPage as? SdkFragment

                if (xDiff > touchSlop && sdkFragment?.onPreFinish() != true) {
                    mVelocityTracker.addMovement(event)
                    isInterceptTouch = true

                    mVelocityTracker.addMovement(event)
                }
            }
            MotionEvent.ACTION_UP -> {
                clearLambda.invoke()
            }
        }

        return isInterceptTouch
    }

    fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        var x = event.x
        if (x < 0) {
            x = 0F
        }

        val pageAnimators = lastPage?.pageAnimators as? PageRatioAnimators
        if (pageAnimators == null) {
            clearLambda.invoke()
            return false
        }

        mVelocityTracker.addMovement(event)

        when (action) {
            MotionEvent.ACTION_DOWN -> {
                firstDownX = x
                lastX = x
            }
            MotionEvent.ACTION_MOVE -> {
                var distance = x - firstDownX
                if (!isScrolling && Math.abs(distance) > touchSlop) {
                    if (distance > 0) {
                        distance -= touchSlop
                    } else {
                        distance += touchSlop
                    }
                    isScrolling = true
                }
                if (isScrolling) {
                    pageAnimators.doTransit(Math.abs(distance / screenWidth), x > lastX)
                    lastX = x
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                var xVelocity = 0.0F
                if (action == MotionEvent.ACTION_UP) {
                    mVelocityTracker.computeCurrentVelocity(1000, maxVelocity.toFloat())
                    xVelocity = mVelocityTracker.xVelocity
                    if (xVelocity < minVelocity) {
                        xVelocity = 0.0F
                    }
                }
                clearLambda.invoke()
                val distance = x - firstDownX
                pageAnimators.doComplete(Math.abs(distance / screenWidth), xVelocity)
                firstDownX = 0f
                lastX = 0f
                lastPage = null
                isScrolling = false
            }
        }

        return true
    }
}