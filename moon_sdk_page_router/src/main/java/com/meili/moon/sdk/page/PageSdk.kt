@file:Suppress("UNCHECKED_CAST")

package com.meili.moon.sdk.page

import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.util.getWrapFiles
import com.meili.moon.sdk.util.throwOnDebug
import com.meili.processor.IPageConfig
import com.meili.processor.PageConfigModel

/**
 * Created by imuto on 2019-08-29.
 */
object PageSdk : IComponent {
    override fun init(env: Environment) {
        val configPackage = Package.getPackage(IPageConfig.PACKAGE_NAME)
        val wrapClass = configPackage.getWrapFiles().log()
        wrapClass.forEach {
            val forName = Class.forName(it)
            if (IPageConfig::class.java.isAssignableFrom(forName)) {
                val getPages = forName.getDeclaredMethod("getPages")
                val list = getPages.invoke(null) as List<PageConfigModel>
                registerPageConfig(list)
            }
        }
    }

    /**
     * 将注解收集到的页面配置信息，添加到Rainbow中
     */
    fun registerPageConfig(configs: List<PageConfigModel>) {
        configs.forEach {
            val forName = Class.forName(it.className)
            if (!Page::class.java.isAssignableFrom(forName)) {
                throwOnDebug("PageName注解使用错误，页面注解只能注册在页面上，错误类：${forName.canonicalName}")
            }
            page().registerPage(it.pageName, forName as Class<Page>, affinity = it.affinityId)
            if (it.interceptors != null) {
                it.interceptors.forEach { interceptor ->

                    val interceptorClass = Class.forName(interceptor)
                    if (!PageInterceptor::class.java.isAssignableFrom(interceptorClass)) {
                        throwOnDebug("PageName注解使用错误，指定的interceptor只能是PageInterceptor的子类，错误类：${forName.canonicalName}")
                    }

                    page().registerInterceptor(it.pageName, interceptorClass.newInstance() as PageInterceptor)
                }
            }
        }
    }

    fun page(): PageManager = PageManagerImpl

}