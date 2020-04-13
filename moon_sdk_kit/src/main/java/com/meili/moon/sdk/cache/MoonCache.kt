package com.meili.moon.sdk.cache

import java.io.File

/**
 * 标准的缓存管理类
 * Created by imuto on 2018/3/21.
 */
interface MoonCache {
    /**获取文件缓存实例*/
    fun getFileCache(cacheDir: File? = null): DiskCache<CacheModel, File>

    /**获取普通缓存实例，保存内容为String*/
    fun getCache(cacheKey: String? = null): DiskCache<CacheModel, String>
}