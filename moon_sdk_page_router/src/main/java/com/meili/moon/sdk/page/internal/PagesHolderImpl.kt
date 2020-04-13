package com.meili.moon.sdk.page.internal

import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.meili.moon.sdk.page.Page
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.PagesHolder
import com.meili.moon.sdk.page.PagesHolder.IntentRedirector

/**
 * 页面持有对象的实现类
 *
 * Created by imuto on 2018/3/30.
 */
object PagesHolderImpl : PagesHolder {

    private val pageMap = mutableMapOf<String, PageArguments<Page>>()

    /**
     * 注册页面
     * @param pageName 页面名称
     * @param pageClass 页面对应的类
     * @param args 页面入参
     * @param affinity 分组名称
     * @param flags 启动的flag设置，常用flag[android.content.Intent.FLAG_ACTIVITY_NEW_TASK]
     * @param launchMode 启动设置，例如常见的：[android.content.pm.ActivityInfo.LAUNCH_SINGLE_INSTANCE]
     */
    override fun <T : Page> registerPage(pageName: String, pageClass: Class<T>, args: Bundle?, affinity: String?, flags: Int, launchMode: Int) {
        val activityInfo = getDefaultActivityInfo(pageClass, affinity, flags, launchMode)
        registerPage(pageName, pageClass, activityInfo)
        if (args != null) {
            registerIntentRedirector(pageName) {
                it.putExtras(args)
            }
        }
    }

    override fun <Type : Page> registerPage(pageName: String, pageClass: Class<Type>, pageInfo: ActivityInfo) {
        //修正页面名称
        var pageNameVar = pageName
        if (pageNameVar.startsWith("/")) {
            pageNameVar = pageNameVar.substring(1)
        }
        if (pageNameVar.endsWith("/")) {
            pageNameVar = pageNameVar.substring(0, pageNameVar.length - 1)
        }
        if (pageMap.contains(pageNameVar)) {
            val page = pageMap[pageNameVar]!!
            page.pageName = pageNameVar
            page.pageClass = pageClass as Class<Page>
            page.activityInfo = pageInfo
            page.extraData = null
            page.redirector = null
        } else {
            pageMap[pageNameVar] = PageArguments(pageNameVar, pageClass as Class<Page>, pageInfo)
        }
    }

    override fun <Type : Page> findClassByPageName(pageName: String): Class<Type>? {
        return pageMap[pageName]?.pageClass as? Class<Type>
    }

    override fun findInfoByPageName(pageName: String): ActivityInfo? {
        return pageMap[pageName]?.activityInfo
    }

    override fun contain(pageName: String): Boolean {
        return pageMap.contains(pageName)
    }

    override fun registerIntentRedirector(pageName: String, redirector: IntentRedirector) {
        pageMap[pageName]!!.redirector = redirector
    }

    override fun registerIntentRedirector(pageName: String, redirector: (PageIntent) -> Unit) {
        registerIntentRedirector(pageName, object : IntentRedirector {
            override fun redirect(intent: PageIntent) {
                redirector.invoke(intent)
            }
        })
    }

    override fun tryRedirector(intent: PageIntent) {
        val arguments = pageMap[intent.pageName] ?: return
        arguments.redirector?.redirect(intent)
    }

    internal data class PageArguments<Type : Page>(internal var pageName: String,
                                                   internal var pageClass: Class<Type>,
                                                   internal var activityInfo: ActivityInfo,
                                                   internal var extraData: Bundle? = null,
                                                   internal var redirector: IntentRedirector? = null)

    private fun <T : Page> getDefaultActivityInfo(pageClass: Class<T>, affinity: String?, flags: Int, launchMode: Int): ActivityInfo {
        val pageInfo = ActivityInfo()
        pageInfo.applicationInfo = ApplicationInfo()
        pageInfo.taskAffinity = affinity
        pageInfo.packageName = pageClass.`package`?.name
        pageInfo.launchMode = launchMode
        pageInfo.flags = flags
        pageInfo.name = pageClass.name
        return pageInfo
    }
}