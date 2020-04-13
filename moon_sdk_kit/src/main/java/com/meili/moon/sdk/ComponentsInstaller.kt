package com.meili.moon.sdk

import android.app.Application
import com.meili.moon.sdk.cache.MoonCache
import com.meili.moon.sdk.common.DefaultEnvironment
import com.meili.moon.sdk.db.DBManager
import com.meili.moon.sdk.http.IHttp
import com.meili.moon.sdk.mock.MockManager
import com.meili.moon.sdk.permission.MoonPermission
import com.meili.moon.sdk.track.TrackerProvider
import com.meili.moon.sdk.util.DefComponentInstaller
import com.meili.moon.sdk.util.setOnceNoError
import java.util.concurrent.atomic.AtomicReference

/**
 * Kit提供的通用能力安装器，在使用通用能力之前，需要先调用本类的方法进行通用能力初始化
 * Created by imuto on 18/3/8.
 */
object ComponentsInstaller {

    /**记录通用的运行环境对象*/
    internal val mEnvironment = AtomicReference<Environment>()

    /**记录通用的数据库访问对象*/
    internal val mDbManager = AtomicReference<DBManager>()

    /**记录通用的文件缓存对象*/
    internal val mCache = AtomicReference<MoonCache>()

    /**记录通用的网络请求对象*/
    internal val mHttp = AtomicReference<IHttp>()

    internal val mMocker = AtomicReference<MockManager>()

    internal val mPermission = AtomicReference<MoonPermission>()

    internal val mTracker = AtomicReference<TrackerProvider>()

    fun getEnv(): Environment {
        if (mEnvironment.get() == null) {
            try {
                val renderActionClass = Class.forName("com.android.layoutlib.bridge.impl.RenderAction")
                val method = renderActionClass.getDeclaredMethod("getCurrentContext")
                val context = method.invoke(null)
                installEnvironment(context as Application)
            } catch (ignored: Throwable) {
                ignored.printStackTrace()
            }
        }
        return mEnvironment.get()
    }

    /**
     * 初始化运行环境
     */
    @JvmStatic
    fun installEnvironment(app: Application) {
        installEnvironment(DefaultEnvironment(app))
    }

    /**
     * 初始化运行环境，作为组件，如果调用此方法，如果之前全局env已经初始化，则你的env不会设置到全局
     */
    @JvmStatic
    @JvmOverloads
    fun installEnvironment(env: Environment, autoInstall: Boolean = true) {
        installEnvInternal(env, true, autoInstall)
    }

    internal fun installEnvInternal(env: Environment, forceInit: Boolean = false, autoInstall: Boolean = true) {
        if (forceInit) {
            mEnvironment.set(env)
        } else {
            mEnvironment.setOnceNoError(env)
        }
        if (autoInstall) {
            DefComponentInstaller.install()
        }
    }

    /**
     * 初始化数据库对象，需要调用方自己实现基于[com.meili.moon.sdk.db]包定义的标准
     *
     * 在使用相关api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installDb(db: DBManager, env: Environment? = null) {
        mDbManager.setOnceNoError(db)
        if (env != null) {
            installEnvInternal(env)
        }
    }

    /**
     * 初始化硬盘缓存对象，需要调用方自己实现基于[com.meili.moon.sdk.cache]包定义的标准
     *
     * 在使用api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installCache(cache: MoonCache, env: Environment? = null) {
        mCache.setOnceNoError(cache)
        if (env != null) {
            installEnvInternal(env)
        }
    }

    /**
     * 初始化网络请求对象，需要调用方自己实现基于[com.meili.moon.sdk.http]包定义的标准
     *
     * 在使用相关api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installHttp(instance: IHttp, env: Environment? = null) {
        mHttp.setOnceNoError(instance)
        if (env != null) {
            installEnvInternal(env)
        }
    }

    /**
     * 初始化Mocker对象，需要调用方自己实现基于[com.meili.moon.sdk.mock]包定义的标准
     *
     * 在使用相关api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installMocker(instance: MockManager, env: Environment? = null) {
        mMocker.setOnceNoError(instance)
        if (env != null) {
            installEnvInternal(env)
        }
    }

    /**
     * 初始化动态权限申请对象，需要调用方自己实现基于[com.meili.moon.sdk.permission]包定义的标准
     *
     * 在使用相关api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installPermission(moonPermission: MoonPermission, env: Environment? = null) {
        mPermission.setOnceNoError(moonPermission)
        if (env != null) {
            installEnvInternal(env)
        }
    }

    /**
     * 初始化埋点记录器，需要调用方自己实现基于[com.meili.moon.sdk.track]包定义的标准
     *
     * 在使用相关api之前，必须先调用本方法做初始化
     */
    @JvmStatic
    fun installTrackerProvider(provider: TrackerProvider, env: Environment? = null) {
        mTracker.setOnceNoError(provider)
        if (env != null) {
            installEnvInternal(env)
        }
    }

}