package com.meili.moon.sdk.cache

/**
 * 做Lru算法的缓存策略
 * 主要提供增删改查的能力限制
 *
 * Created by imuto on 18/3/8.
 */

/**缓存最大5000条*/
const val MAX_COUNT = 5000
/**缓存最大500M*/
const val MAX_FILE_SIZE = 100 * 1024 * 1024L

interface DiskCache<out ValueType : CacheModel, CacheType> {

    /**配置全局缓存，此配置只有第一次配置有效*/
    fun configGlobal(config: Config)

    /**添加缓存内容*/
    fun put(key: String, cacheModel: CacheModel)

    /**添加缓存内容*/
    fun put(key: String, value: CacheType)

    /**获取缓存对象*/
    fun get(key: String): ValueType?

    /**获取缓存内容*/
    fun getValue(key: String): CacheType?

    /**清空当前类型的缓存，例如文件缓存根据缓存目录分类*/
    fun clear(): Boolean

    /**清空所有缓存*/
    fun clearAll(): Boolean

    /**删除指定key的缓存，如果成功则返回true，否则返回false*/
    fun remove(key: String): Boolean

    /**缓存的相关配置*/
    interface Config {
        /**缓存最大条数*/
        var cacheMaxCount: Int
        /**文件缓存的最大磁盘空间，单位：字节*/
        var cacheMaxFileSize: Long
    }
}