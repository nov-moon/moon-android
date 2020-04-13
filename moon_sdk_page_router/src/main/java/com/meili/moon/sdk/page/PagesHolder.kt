package com.meili.moon.sdk.page

import android.content.pm.ActivityInfo
import android.os.Bundle

/**
 * 页面持有者，提供页面关系存储，检查，获取
 * Created by imuto on 2018/3/30.
 */
interface PagesHolder {

    /**
     * 注册一个页面
     * @param pageName 页面名称
     * @param pageClass 页面class
     * @param pageInfo 页面info信息
     */
    fun <Type : Page> registerPage(pageName: String, pageClass: Class<Type>, pageInfo: ActivityInfo)

    /**
     * 注册页面
     * @param pageName 页面名称
     * @param pageClass 页面对应的类
     * @param args 页面入参
     * @param affinity 分组名称
     * @param flags 启动的flag设置，常用flag[android.content.Intent.FLAG_ACTIVITY_NEW_TASK]
     * @param launchMode 启动设置，例如常见的：[android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE]
     */
    fun <T : Page> registerPage(pageName: String, pageClass: Class<T>, args: Bundle? = null,
                                affinity: String? = null, flags: Int = 0, launchMode: Int = 0)

    /**
     * 通过pageName获取ClassName
     */
    fun <Type : Page> findClassByPageName(pageName: String): Class<Type>?

    /**通过PageName获取activityInfo*/
    fun findInfoByPageName(pageName: String): ActivityInfo?

    /**是否包含指定pageName的页面*/
    fun contain(pageName: String): Boolean

    /**注册intent的跳转器*/
    fun registerIntentRedirector(pageName: String, redirector: IntentRedirector)

    /**注册intent的跳转器*/
    fun registerIntentRedirector(pageName: String, redirector: (PageIntent) -> Unit)

    /**尝试使用跳转器对intent进行处理*/
    fun tryRedirector(intent: PageIntent)

    /**页面跳转器*/
    interface IntentRedirector {
        /**对intent做处理，后续页面跳转逻辑使用处理后的intent*/
        fun redirect(intent: PageIntent)
    }
}