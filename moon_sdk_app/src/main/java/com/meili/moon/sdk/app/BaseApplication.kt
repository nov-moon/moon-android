package com.meili.moon.sdk.app

import android.app.Application
import com.meili.moon.sdk.app.base.page.util.TranslucentStatusBarUtils
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.Utils
import com.meili.moon.sdk.base.util.ViewUtil
import com.meili.moon.sdk.page.internal.animators.EdgeTouchHolder

/**
 * Created by imuto on 2018/5/17.
 */
open class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (!Utils.isMainProcess(this)) {
            return
        }
        Sdk.init(this)
//        设置滑动返回功能开始监听的Y值
        var titleBarHeight = resources?.getDimensionPixelOffset(R.dimen.moon_sdk_app_title_bar_height)?:44
        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            titleBarHeight += ViewUtil.getStatusBarHeight()
        }
        EdgeTouchHolder.minHolderTouchY = titleBarHeight
//        if (VersionUtils.isDebug()) {
//            ZXingLibrary.initDisplayOpinion(this);
//        }
    }
}