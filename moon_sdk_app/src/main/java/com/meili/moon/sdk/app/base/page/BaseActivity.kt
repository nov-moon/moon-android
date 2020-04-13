package com.meili.moon.sdk.app.base.page

import android.os.Bundle
import com.meili.moon.sdk.app.base.page.util.TranslucentStatusBarUtils
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.internal.SdkActivity

/**
 * activity的基类方法
 * Created by imuto on 2018/4/24.
 */
abstract class BaseActivity : SdkActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStartPage(intent: PageIntent): Boolean {
        return false
    }

    override fun onGotoPage(intent: PageIntent): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        TranslucentStatusBarUtils.tryOpenTranslucentStatusBarStyle(this)
    }

}