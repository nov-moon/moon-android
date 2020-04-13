package com.meili.moon.sdk.page.internal

import android.os.Bundle
import com.meili.moon.sdk.page.IPageDefine
import com.meili.moon.sdk.page.Page

/**
 * 页面注册器
 * Created by imuto on 2018/4/8.
 */
abstract class SdkPageDefine : IPageDefine {

    override fun <T : Page> registerPage(pageName: String, pageClass: Class<T>, args: Bundle?, affinity: String?, flags: Int, launchMode: Int) {
        PageManagerImpl.registerPage(pageName, pageClass, args, affinity, flags, launchMode)
    }
}