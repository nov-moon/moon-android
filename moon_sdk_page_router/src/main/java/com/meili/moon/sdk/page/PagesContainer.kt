package com.meili.moon.sdk.page

import android.view.ViewGroup

/**
 * 页面容器
 * Created by imuto on 2018/3/30.
 */
interface PagesContainer : BasePage {

    /**持有本页面的containerId*/
    var containerId: Int

    var container: ViewGroup

    fun onStartPage(intent: PageIntent): Boolean

    fun onGotoPage(intent: PageIntent): Boolean

    fun contain(page: Page): Boolean
}