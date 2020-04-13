package com.meili.moon.sdk.page.internal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.text.TextUtils
import android.util.TypedValue
import android.view.KeyEvent
import com.meili.moon.sdk.page.Page
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.resultCode

/**
 * Sdk层的Activity，属于page路由的container部分
 *
 * pagesContainer继承此类，并在Manifest中注册action，注册方式：
 *
 * <intent-filter>
 *      <action android:name="action.com.meili.moon.sdk.page.router.container"/>
 *      <category android:name="android.intent.category.DEFAULT"/>
 * </intent-filter>
 *
 * 这个action的来源为：[com.meili.moon.sdk.page.ACTION_PAGES_CONTAINER]
 *
 * Created by imuto on 2018/4/9.
 */
abstract class SdkActivity : FragmentActivity(), PageFragmentContainer {

    override var pageActivity: Activity
        get() = this
        set(value) {}

    /**记录当前页面是否在top*/
    private var isFront = false
    /**记录当前页面是否正在结束*/
    private var mFinishing = false
    /**记录当前页面的保存状态*/
    private var saveStated = false

    override var hasDestroyed: Boolean = false
        get() = field && isFinishing

    override var pageIntent: PageIntent = PageIntent()

    /**容器ID*/
    abstract override var containerId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        pageIntent = PageIntent(this.intent)
        super.onCreate(savedInstanceState)
    }

    override fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent) {
        onActivityResult(requestCode, resultCode, intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (PageCallbackHolder.match(requestCode)) {
            val result = intent ?: Intent()
            result.resultCode = resultCode
            PageCallbackHolder.callback(requestCode, result)
            return
        }

        val topFragment = getTopFragment()
        val resultIntent = intent ?: Intent()
        if (topFragment?.onPageResultHolder?.invoke(requestCode, resultCode, intent) == true) {
            return
        }
        topFragment?.onPageResult(requestCode, resultCode, resultIntent)
    }

    override fun isFront(): Boolean {
        return isFront && !mFinishing
    }

    override fun onResume() {
        super.onResume()
        isFront = true
        saveStated = false
    }

    override fun onPause() {
        isFront = false
        super.onPause()
    }

    override fun finish() {
        mFinishing = true
        super.finish()
        addExitTransition()
    }

    /**
     * 结束当前页面
     *
     * [isForce] 是否强制结束，如果是fragment，则不会执行onPreFinish()逻辑
     */
    override fun finish(isForce: Boolean) {
        finish()
    }

    override fun onDestroy() {
        mFinishing = true
        hasDestroyed = true
        super.onDestroy()
    }

    override fun isFinishing(): Boolean {
        return mFinishing || super.isFinishing()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        saveStated = true
        super.onSaveInstanceState(outState)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (saveStated) return true
        if (event == null) return super.onKeyUp(keyCode, event)

        if (event.isTracking && !event.isCanceled) {
            val topFragment = PageManagerImpl.getTopPage()
            if (topFragment is SdkFragment) {
                if (topFragment.onKeyUp(keyCode, event)) {
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun hasSavedState(): Boolean = saveStated

    override fun contain(page: Page): Boolean {
        val fragment = page as? Fragment ?: return false

        val fragments = supportFragmentManager.fragments

        return fragments != null && fragments.contains(fragment)
    }

    protected fun getTopFragment(): SdkFragment? {
        val manager = supportFragmentManager
        val backStackEntryAt = manager.getBackStackEntryAt(manager.backStackEntryCount - 1)
        val fragments = manager.fragments
        if (backStackEntryAt != null && !TextUtils.isEmpty(backStackEntryAt.name)) {
            for (i in fragments.indices.reversed()) {
                val fragment = fragments[i]
                if (fragment != null && !TextUtils.isEmpty(fragment.tag) && fragment.tag!!.startsWith(backStackEntryAt.name)) {
                    return fragment as SdkFragment
                }
            }
        }
        for (i in fragments.indices.reversed()) {
            val fragment = fragments[i]
            if (fragment !is SdkFragment || fragment.isChildPage) {
                continue
            }
            return fragment
        }
        return null
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        addOpenTransition()
    }

    private fun addOpenTransition() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowAnimationStyle, typedValue, true)
        val attr = intArrayOf(android.R.attr.activityOpenEnterAnimation,
                android.R.attr.activityOpenExitAnimation)

        val array = theme.obtainStyledAttributes(typedValue.resourceId, attr)
        val enterAnim = array.getResourceId(array.getIndex(0), R.anim.rainbow_open_in)
        val outAnim = array.getResourceId(array.getIndex(1), R.anim.rainbow_open_out)
        array.recycle()
//        overridePendingTransition(enterAnim, outAnim)
    }

    private fun addExitTransition() {
        val typedValue = TypedValue()
        theme.resolveAttribute(android.R.attr.windowAnimationStyle, typedValue, true)
        val attr = intArrayOf(android.R.attr.activityCloseEnterAnimation,
                android.R.attr.activityCloseExitAnimation)

        val array = theme.obtainStyledAttributes(typedValue.resourceId, attr)
        val enterAnim = array.getResourceId(array.getIndex(0), R.anim.rainbow_close_in)
        val outAnim = array.getResourceId(array.getIndex(1), R.anim.rainbow_close_out)
        array.recycle()
//        overridePendingTransition(enterAnim, outAnim)
    }
}