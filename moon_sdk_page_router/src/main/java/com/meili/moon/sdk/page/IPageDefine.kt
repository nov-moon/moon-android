package com.meili.moon.sdk.page

import android.os.Bundle

/**
 * 页面注册接口
 * Created by imuto on 2018/3/30.
 */
interface IPageDefine {

    /**初始化pages，将注册的page添加到holder中*/
    fun loadPages()

    /**
     * 注册页面
     * @param pageName 页面名称
     * @param pageClass 页面对应的类
     * @param args 页面入参
     * @param affinity 分组名称
     * @param flags 启动的flag设置，常用flag[android.content.Intent.FLAG_ACTIVITY_NEW_TASK]
     * @param launchMode 启动设置，例如常见的：[android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE]
     */
    fun <T : Page> registerPage(pageName: String, pageClass: Class<T>, args: Bundle? = null, affinity: String? = null, flags: Int = 0, launchMode: Int = 0)
}