package com.meili.moon.sdk.cache

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.cache.lru.ApiLruCache
import com.meili.moon.sdk.cache.lru.FileLruCache
import com.meili.moon.sdk.cache.lru.LruDiskCacheModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * cache的管理类
 * Created by imuto on 2018/3/21.
 */
object MoonCacheImpl : MoonCache, IComponent {

    private val File_CACHE_MAP = ConcurrentHashMap<String, DiskCache<LruDiskCacheModel, File>>(4)
    private val API_CACHE_MAP = ConcurrentHashMap<String, DiskCache<LruDiskCacheModel, String>>(4)

    private const val DEF_API_CACHE_KEY = "api_cache_"
    private lateinit var DEF_TEMP_DIR: File

    override fun getFileCache(cacheDir: File?): DiskCache<CacheModel, File> {

        val dir = cacheDir ?: DEF_TEMP_DIR

        val cachePath = dir.absolutePath
        var cache: DiskCache<LruDiskCacheModel, File>? = null
        synchronized(File_CACHE_MAP) {
            cache = File_CACHE_MAP[cachePath] ?: FileLruCache(dir)

            if (!File_CACHE_MAP.contains(cachePath)) {
                File_CACHE_MAP[cachePath] = cache!!
            }
        }

        return cache!!
    }

    override fun getCache(cacheKey: String?): DiskCache<CacheModel, String> {
        val ck = cacheKey ?: DEF_API_CACHE_KEY

        var cache: DiskCache<LruDiskCacheModel, String>? = null
        synchronized(API_CACHE_MAP) {
            cache = API_CACHE_MAP[ck] ?: ApiLruCache(ck)

            if (!API_CACHE_MAP.contains(ck)) {
                API_CACHE_MAP[ck] = cache!!
            }
        }

        return cache!!
    }

    override fun init(env: Environment) {
        DEF_TEMP_DIR = File(CommonSdk.environment().appCacheDir(), "defCache")
        ComponentsInstaller.installCache(this, env)
    }
}