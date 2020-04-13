package com.meili.moon.sdk

import android.app.Application
import com.meili.moon.sdk.db.IDB
import com.meili.moon.sdk.db.ITable
import com.meili.moon.sdk.event.Events
import com.meili.moon.sdk.event.EventsImpl
import com.meili.moon.sdk.http.IHttp
import com.meili.moon.sdk.json.JsonHelperImpl
import com.meili.moon.sdk.mock.MockManager
import com.meili.moon.sdk.permission.Permission
import com.meili.moon.sdk.task.DefTaskExecutor
import com.meili.moon.sdk.task.TaskPool
import com.meili.moon.sdk.track.Tracker
import java.io.File


/**
 * 配置sdk基础模块的特性
 * Created by imuto on 17/11/23.
 */
object CommonSdk {

    /**获取通用上下文环境对象*/
    @JvmStatic
    fun environment(): Environment = ComponentsInstaller.getEnv()

    /**当前context对象*/
    @JvmStatic
    fun app(): Application = environment().app()

    /**task执行器*/
    @JvmStatic
    fun task(): ITaskExecutor = DefTaskExecutor

    /**获取通用线程池管理器对象*/
    @JvmStatic
    fun executor(): IExecutorPool = TaskPool

    @Permission(android.Manifest.permission.ACCESS_CHECKIN_PROPERTIES)
    /** 获取通用的json解析器  */
    @JvmStatic
    fun json(): IJsonHelper = JsonHelperImpl

    /** 数据库实例  */
    @JvmStatic
    fun db(dbConfig: IDB.Config? = null): ITable = ComponentsInstaller.mDbManager.get().getDBInstance(dbConfig).table

    /**获取通用网络请求对象*/
    fun http(): IHttp = ComponentsInstaller.mHttp.get()

    /**获取通用文件缓存对象，如果不能确认是否有缓存库，请先调用[isCache]方法进行判断*/
    @JvmStatic
    fun fileCache(cacheDir: File? = null) = ComponentsInstaller.mCache.get()!!.getFileCache(cacheDir)

    /**获取通用缓存对象，如果不能确认是否有缓存库，请先调用[isCache]方法进行判断*/
    @JvmStatic
    fun cache() = ComponentsInstaller.mCache.get()!!.getCache()

    /**获取通用mock数据对象*/
    @JvmStatic
    fun mocker(): MockManager = ComponentsInstaller.mMocker.get()!!

    /**获取消息订阅对象*/
    @JvmStatic
    fun event(): Events = EventsImpl

    /**获取权限申请对象*/
    @JvmStatic
    fun permission() = ComponentsInstaller.mPermission.get()!!

    /**数据埋点对象*/
    @JvmStatic
    fun tracker() = Tracker

    /**是否已经安装数据库组件*/
    @JvmStatic
    fun isDb() = ComponentsInstaller.mDbManager.get() != null

    /**是否已经安装网络组件*/
    @JvmStatic
    fun isHttp() = ComponentsInstaller.mHttp.get() != null

    /**是否已经安装缓存组件*/
    @JvmStatic
    fun isCache() = ComponentsInstaller.mCache.get() != null

    /**是否已经安装mock数据组件*/
    @JvmStatic
    fun isMocker() = ComponentsInstaller.mMocker.get() != null && ComponentsInstaller.mMocker.get().validate()

    /**是否已经安装权限申请组件*/
    @JvmStatic
    fun isPermission() = ComponentsInstaller.mPermission.get() != null

    /**是否已经安装埋点组件*/
    @JvmStatic
    fun isTracker() = ComponentsInstaller.mTracker.get() != null
}