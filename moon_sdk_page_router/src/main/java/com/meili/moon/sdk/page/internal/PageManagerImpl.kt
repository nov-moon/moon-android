@file:Suppress("UNCHECKED_CAST")

package com.meili.moon.sdk.page.internal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.internal.utils.*


/**
 * Created by imuto on 2018/4/2.
 */
object PageManagerImpl : PageManager {

    private var mPageConfig: RainbowConfig = RainbowConfig()
    private var isMainConfig = false

    /**页面拦截器*/
    private var mInterceptors = mutableListOf<InterceptorWrapper>()

    init {
        //注册activity生命周期监听
        CommonSdk.environment().app().registerActivityLifecycleCallbacks(PageStackManager.INSTANCE)

        registerProcessor(PageNameProcessor)
        registerProcessor(AppUriProcessor)
        registerProcessor(H5UriProcessor)
    }

    override fun getTopContainer(): PagesContainer? {
        return PageStackManager.INSTANCE.topContainer
    }

    override fun getTopActivity(): Activity {
        return PageStackManager.INSTANCE.topActivity
    }

    override fun getTopPage(): Page? {
        return PageStackManager.INSTANCE.topFragment as? Page
    }

    override fun finishAll() {
        PageStackManager.INSTANCE.finishAll()
    }

    override fun finishAffinity(pageIntent: PageIntent) {
        PageStackManager.INSTANCE.finishAffinity(pageIntent)
    }

    override fun finish(step: Int, page: Page) {
        innerFinish(step, page)
    }

    override fun <T : Any> gotoPage(intent: PageIntent, canSameWithPre: Boolean?, destroyable: Any?,
                                    pageCallback: OnPageResultCallback<in T>?) {
        gotoPageInternal(intent, canSameWithPre, destroyable, pageCallback)
    }

    override fun gotoPage(intent: PageIntent, canSameWithPre: Boolean?) {
        gotoPageInternal<Any>(intent, canSameWithPre, null, null)
    }

    private fun <T : Any> gotoPageInternal(intent: PageIntent, canSameWithPre: Boolean?,
                                           destroyable: Any? = null, pageCallback: OnPageResultCallback<in T>? = null,
                                           interceptor: PageInterceptor? = null) {
        val continueCallback: ContinueCallbackInner = { gotoPageInternal(intent, canSameWithPre, destroyable, pageCallback, it) }
        val intercept = intent.intercept(mInterceptors, interceptor, continueCallback)
        if (intercept) {
            return
        }
        processor(intent, canSameWithPre, destroyable, pageCallback)
    }

    override fun gotoActivity(intent: Intent) {
        gotoActivityInternal(intent)
    }

    override fun gotoActivity(intent: Intent, destroyable: IDestroable?, activityCallback: OnPageResultCallback<Intent>?) {
        gotoActivityInternal(intent, destroyable, activityCallback)
    }

    private fun gotoActivityInternal(intent: Intent, interceptor: PageInterceptor? = null) {
        if (intent.intercept(mInterceptors, interceptor) { gotoActivityInternal(intent, it) }) {
            return
        }
        PageStackManager.INSTANCE.topActivity.startActivity(intent)
    }

    private fun gotoActivityInternal(intent: Intent, destroyable: IDestroable?, activityCallback: OnPageResultCallback<Intent>?, interceptor: PageInterceptor? = null) {
        if (intent.intercept(mInterceptors, interceptor) { gotoActivityInternal(intent, destroyable, activityCallback, it) }) {
            return
        }
        if (activityCallback == null) {
            PageStackManager.INSTANCE.topActivity.startActivity(intent)
        } else {
            val requestCode = PageCallbackHolder.registerActivityCallback(activityCallback, intent.receiveCancelResult, destroyable)
            PageStackManager.INSTANCE.topActivity.startActivityForResult(intent, requestCode)
        }
    }

    override fun getPagesHolder(): PagesHolder = PagesHolderImpl

    /**
     * 配置页面跳转相关内容，如果有多个配置，则最后一个配置生效.
     * 如果设置[isMainConfig] = true，则不区分先后顺序，此config生效，默认为false
     */
    override fun config(config: RainbowConfig, isMainConfig: Boolean) {
        if (!this.isMainConfig) {
            mPageConfig = config
            this.isMainConfig = isMainConfig
        }
    }

    /**
     * 获取当前的pageConfig
     */
    override fun getConfig(): RainbowConfig {
        return mPageConfig
    }

    /**
     * 添加拦截器
     */
    override fun registerInterceptor(interceptor: PageInterceptor) {
        addInterceptor(mInterceptors, interceptor)
    }

    override fun registerInterceptor(pageName: String, interceptor: PageInterceptor) {
        addInterceptor(mInterceptors, interceptor, pageName)
    }

    override fun unregisterInterceptor(interceptor: PageInterceptor) {
        val findInterceptor = mInterceptors.find { it.interceptor == interceptor }
                ?: return
        mInterceptors.remove(findInterceptor)
    }

    /**
     * 注册页面处理器，用来处理页面跳转操作
     */
    override fun registerProcessor(processor: PageProcessor) {
        addProcessor(processor)
    }

    /**
     * 解注页面处理器
     */
    override fun unregisterProcessor(processor: PageProcessor) {
        removeProcessor(processor)
    }

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
        getPagesHolder().registerPage(pageName, pageClass, args, affinity, flags, launchMode)
    }

}
