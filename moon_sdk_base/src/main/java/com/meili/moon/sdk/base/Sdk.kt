package com.meili.moon.sdk.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import com.meili.moon.sdk.*
import com.meili.moon.sdk.base.Sdk.init
import com.meili.moon.sdk.base.app.ApplicationImpl
import com.meili.moon.sdk.base.common.AppEnvironment
import com.meili.moon.sdk.base.img.IImageLoader
import com.meili.moon.sdk.base.tracker.SensorsAnalyticsTrackerProvider
import com.meili.moon.sdk.base.util.ActivityHolder
import com.meili.moon.sdk.base.util.Utils
import com.meili.moon.sdk.base.util.VersionUtils
import com.meili.moon.sdk.base.view.ViewInjectorImpl
import com.meili.moon.sdk.common.IApplication
import com.meili.moon.sdk.db.ITable
import com.meili.moon.sdk.event.Events
import com.meili.moon.sdk.http.IHttp
import com.meili.moon.sdk.page.PageManager
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.internal.SdkActivity
import com.meili.moon.sdk.page.internal.SdkFragment
import com.meili.moon.sdk.track.Tracker
import java.io.File

/**
 * 提供SDk的主要功能
 *
 * 使用前，在application的onCreate中调用[init]方法进行初始化
 * Created by imuto on 2018/3/22.
 */
object Sdk {

    /** 只调用一次即可 */
    @JvmStatic
    @MainThread
    fun init(app: Application) {
        if (!Utils.isMainProcess(app)) {
            return
        }

        val env = AppEnvironment(ApplicationImpl(app))
        ComponentsInstaller.installEnvironment(env)

        VersionUtils.syncDebug()

        Ext.mViewInjector = ViewInjectorImpl.INSTANCE
        Ext.registerPageManager(null)

        event().addIgnoreSubscriber(SdkFragment::class.java, SdkActivity::class.java,
                Activity::class.java, Fragment::class.java)

        application()?.registerActivityLifecycleCallbacks(ActivityHolder)

        SensorsAnalyticsTrackerProvider.init()
    }

    /** 获取一个view初始化器  */
    @JvmStatic
    @MainThread
    fun view(): IViewInjector {
        return Ext.mViewInjector!!
    }

    /** 提供一个Context的委托类 */
    @JvmStatic
    fun app(): Context {
        return CommonSdk.app()
    }

    /** 提供一个Application的委托类，实际调用使用注册进来的Application  */
    @JvmStatic
    fun application(): IApplication? {
        return CommonSdk.application()
    }

    /** 提供一个PageManager对象，用来管理页面跳转逻辑 */
    @JvmStatic
    fun page(): PageManager {
        return Ext.mPageManager
    }

    fun isAppFront(): Boolean {
        return ActivityHolder.isFront
    }

    /** 提供一个图片加载对象 */
    @JvmStatic
    fun image(): IImageLoader {
        return IImageLoader.ImageLoaderImpl
    }

    /** 数据库实例  */
    @JvmStatic
    fun db(): ITable {
        return CommonSdk.db()
    }

    /** 获取通用的json解析器  */
    @JvmStatic
    fun json(): IJsonHelper {
        return CommonSdk.json()
    }

    /**获取通用上下文环境对象*/
    @JvmStatic
    fun environment(): Environment = CommonSdk.environment()

    /**task执行器*/
    @JvmStatic
    fun task(): ITaskExecutor = CommonSdk.task()

    /**获取通用线程池管理器对象*/
    @JvmStatic
    fun executor(): IExecutorPool = CommonSdk.executor()

    /**获取通用网络请求对象*/
    @JvmStatic
    fun http(): IHttp = CommonSdk.http()

    /**获取通用文件缓存对象*/
    @JvmStatic
    fun fileCache(cacheDir: File? = null) = CommonSdk.fileCache(cacheDir)

    /**获取通用缓存对象*/
    @JvmStatic
    fun cache() = CommonSdk.cache()

    /** 获取通用的消息订阅管理对象 */
    @JvmStatic
    fun event(): Events {
        return CommonSdk.event()
    }

    /** 获取通用的埋点对象 */
    @JvmStatic
    fun tracker(): Tracker {
        return CommonSdk.tracker()
    }

    /** 注册一个viewInjector  */
    @MainThread
    fun registerViewInjector(viewInjector: IViewInjector) {
        Ext.registerViewInjector(viewInjector)
    }

    internal object Ext {
        var mViewInjector: IViewInjector? = null

        lateinit var mPageManager: PageManager

        fun registerViewInjector(injector: IViewInjector?) {
            mViewInjector = injector ?: ViewInjectorImpl.INSTANCE
        }

        fun registerPageManager(injector: PageManager?) {
            mPageManager = injector ?: PageManagerImpl
        }
    }

}