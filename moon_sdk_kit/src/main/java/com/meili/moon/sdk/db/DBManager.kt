package com.meili.moon.sdk.db

/**
 * 数据库管理对象
 * Created by imuto on 2018/4/28.
 */
interface DBManager {
    /**是否可打印日志*/
    var logable: Boolean

    /**获取指定配置的db对象，如果入参为null，则使用默认配置*/
    fun getDBInstance(dbConfig: IDB.Config? = null): IDB

    /**设置默认数据库配置*/
    fun setDefaultDBConfig(dbConfig: IDB.Config)
}