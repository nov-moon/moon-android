package com.meili.moon.sdk.page.demo

import android.app.Application
import android.content.Intent
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.Priority.Companion.PRIORITY_LOW
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.internal.utils.PageInterceptInfo

/**
 * Application
 */
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ComponentsInstaller.installEnvironment(this)

        // 对Rainbow进行全局设置
        Rainbow.getConfig().apply {
            // 设置app特定的schema，一般用来支持外部链接打开app
            appSchema = "moon"
            // 是否可打开相同页面，默认为true
            canOpenSamePage = true

            h5OpenProcessor = processor@{ uri, intent, canSameWithPre, destroyable, pageCallback ->
                uri.log()
                return@processor true
            }
        }

        PageManagerImpl.registerPage("home", HomeFragment::class.java)
        PageManagerImpl.registerPage("target", InterceptorTargetFragment::class.java)
        PageManagerImpl.registerPage("interceptor", InterceptorFragment::class.java)
        PageManagerImpl.registerPage("pageState", PageStatesFragment::class.java)
        PageManagerImpl.registerPage("otherGroup", OtherGroupFragment::class.java, affinity = "otherGroup")
        PageManagerImpl.registerPage("samePage1", SamePage1Fragment::class.java)
        PageManagerImpl.registerPage("samePage2", SamePage2Fragment::class.java)
        PageManagerImpl.registerPage("samePage3", SamePage3Fragment::class.java)
        PageManagerImpl.registerPage("pageNotification", PageNotificationTestFragment::class.java)

        PageManagerImpl.registerInterceptor(object : PageInterceptor {


            /**
             * 拦截器的优先级，请参照[Priority.PRIORITY_LOW]、[Priority.PRIORITY_NORMAL]、
             * [Priority.PRIORITY_HIGH]、[Priority.PRIORITY_MAX]
             */
            override val priority: Int = PRIORITY_LOW

            var hasIntercept = false
            /**
             * 尝试拦截页面
             *
             * [info]中存放着打开页面的一些原始信息。如果你不拦截页面，直接返回false即可。如果你拦截了页面，则返回true。
             * 如果你拦截了页面并且做完相关操作后，想继续原来的打开操作，可调用[continueCallback]来完成原来的操作。
             *
             */
            override fun intercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean {
                "fragment拦截器".log()

                val intent = info.intent

                if (intent is PageIntent) {
                    if (intent.pageName == "target" && !hasIntercept) {
                        val pageIntent = PageIntent("interceptor")
                        PageManagerImpl.gotoPage<Boolean>(pageIntent) {
                            it.log()
                            if (it) {
                                hasIntercept = true
                                continueCallback()
                            }
                        }
                        return true
                    }
                }

                return false
            }
        })

        PageManagerImpl.registerInterceptor(object : PageInterceptor {

            /**
             * 拦截器的优先级，请参照[Priority.PRIORITY_LOW]、[Priority.PRIORITY_NORMAL]、
             * [Priority.PRIORITY_HIGH]、[Priority.PRIORITY_MAX]
             */
            override val priority: Int = PRIORITY_LOW

            var hasIntercept = false

            /**
             * 尝试拦截页面
             *
             * [info]中存放着打开页面的一些原始信息。如果你不拦截页面，直接返回false即可。如果你拦截了页面，则返回true。
             * 如果你拦截了页面并且做完相关操作后，想继续原来的打开操作，可调用[continueCallback]来完成原来的操作。
             *
             */
            override fun intercept(info: PageInterceptInfo, continueCallback: ContinueCallback): Boolean {

                "Activity拦截器".log()

                val intent = info.intent

                if (intent !is PageIntent) {
                    if (intent.component.className.contains("InterceptorTargetActivity") && !hasIntercept) {
                        hasIntercept = true

                        val pageIntent = Intent(this@DemoApp, InterceptorActivity::class.java)
                        PageManagerImpl.gotoActivity(pageIntent) {
                            if (it.getBooleanExtra("isOk", false)) {
                                continueCallback()
                            }
                        }
                        return true
                    }
                }

                return false
            }

        })
    }
}
