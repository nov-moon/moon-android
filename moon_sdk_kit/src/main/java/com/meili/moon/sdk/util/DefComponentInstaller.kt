package com.meili.moon.sdk.util

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.cache.MoonCache
import com.meili.moon.sdk.db.DBManager
import com.meili.moon.sdk.http.IHttp
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.TAG
import com.meili.moon.sdk.mock.MockManager

/**
 * 默认的组件安装器
 *
 * 此安装器会在编译期由moon-kit插件修改代码，请不熟悉的同学不要修改此类，否则可能导致不能使用。
 * 编译期代码插桩请参考moon-kit插件(moon_tools项目)
 *
 * Created by imuto on 2018/3/21.
 */
object DefComponentInstaller {

    /**
     * 子组件列表，在编译期进行数据添加
     */
    private val componentList = mutableListOf<IComponent>()
    private var hasInstalled = false

    @Synchronized
    fun install() {
        if (hasInstalled) return
        hasInstalled = true
        installComponents()
    }

    /**
     * 初始化所有子组件。会在编译期修改本方法
     */
    private fun installComponents() {
        val app = CommonSdk.app()
        if (app == null) {
            throwOnDebug("CommonSdk中的application未初始化")
            return
        }
        componentList.forEach {
            it.init(app)
        }
    }

    private fun installDb() {
        try {
            val dbClass = Class.forName("com.meili.moon.sdk.db.MoonDB")
            val instanceField = dbClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null) as DBManager
            ComponentsInstaller.installDb(instance)
            LogUtil.d(TAG, "init DB success！！")
        } catch (e: Exception) {
            LogUtil.d(TAG, "init DB error！！")
        }
    }

    private fun installCache() {
        try {
            val cacheClass = Class.forName("com.meili.moon.sdk.cache.MoonCacheImpl")
            val instanceField = cacheClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null) as MoonCache
            ComponentsInstaller.installCache(instance)
            LogUtil.d(TAG, "init CACHE success！！")
        } catch (e: Exception) {
            LogUtil.d(TAG, "init CACHE error！！")
        }
    }

    private fun installHttp() {
        try {
            val cacheClass = Class.forName("com.meili.moon.sdk.http.impl.HttpImpl")
            val instanceField = cacheClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null) as IHttp
            ComponentsInstaller.installHttp(instance)
            LogUtil.d(TAG, "init HTTP success！！")
        } catch (e: Exception) {
            LogUtil.d(TAG, "init HTTP error！！")
        }
    }

    private fun installMocker() {
        try {
            val cacheClass = Class.forName("com.meili.moon.mock.MockerManagerImpl")
            val instanceField = cacheClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null) as MockManager
            ComponentsInstaller.installMocker(instance)
            LogUtil.d(TAG, "init Mock success！！")
        } catch (e: Exception) {
            LogUtil.d(TAG, "init Mock error！！")
        }
    }

    private fun installPermission() {
        try {
            val cacheClass = Class.forName("com.meili.sdk.permission.MoonPermissionImpl")
            val instanceField = cacheClass.getDeclaredField("INSTANCE")
            val instance = instanceField.get(null) as IComponent
            instance.init(CommonSdk.environment())
            LogUtil.d(TAG, "init MoonPermission success！！")
        } catch (e: Exception) {
            LogUtil.d(TAG, "init MoonPermission error！！")
        }
    }
}