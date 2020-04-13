package com.meili.moon.sdk.page

import android.app.Activity
import android.content.Intent
import com.meili.moon.sdk.common.IDestroable

/**
 * Created by imuto on 2018/4/9.
 */
interface BasePage : IDestroable {

    /**持有本页面的activity对象*/
    var pageActivity: Activity

    /**
     * 当前页面的pageIntent对象
     *
     * {@hide}
     */
    var pageIntent: PageIntent

    /**是否在顶部*/
    fun isFront(): Boolean

    /**当前page是否正在结束*/
    fun isFinishing(): Boolean

    /**
     * 结束当前页面
     *
     * [isForce] 是否强制结束，如果是fragment，则不会执行onPreFinish()逻辑
     */
    fun finish(isForce: Boolean = false)

    fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent)
}