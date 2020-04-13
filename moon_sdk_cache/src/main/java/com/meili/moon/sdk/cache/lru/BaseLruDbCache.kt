package com.meili.moon.sdk.cache.lru

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.cache.*
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.db.IWhere
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.*
import com.meili.sdk.db.sql.ISelector
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

///**缓存文件的后缀名*/
//private const val TEMP_FILE_SUFFIX = ".temp"
///**默认的文件缓存目录*/
//private const val DEF_CACHE_DIR = "meili_temp"
//内存缓存的最大条数
const val MAX_LRU_CACHE = 100

/**
 *
 * Created by imuto on 18/3/13.
 */
abstract class BaseLruDbCache<CacheType>(protected val cacheTypeKey: String) : DiskCache<LruDiskCacheModel, CacheType> {

    protected val db = CommonSdk.db()
    private var maxCount = MAX_COUNT

    private var lruCache = LruCache<String, CacheModel>(MAX_LRU_CACHE)
    /**当前缓存是否可用*/
    private var available = !isEmpty(cacheTypeKey)

    protected companion object Base {

        val EXECUTOR: Executor by lazy { CommonSdk.executor().io() }

        private val defConfig = object : DiskCache.Config {
            override var cacheMaxCount: Int = MAX_COUNT
            override var cacheMaxFileSize: Long = MAX_FILE_SIZE
        }

        var globalConfig: AtomicReference<DiskCache.Config> = AtomicReference(defConfig)

        private val trimCounter = AtomicInteger(0)
    }

    override fun configGlobal(config: DiskCache.Config) {
        try {
            globalConfig.setOnce(defConfig, config)
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    override fun put(key: String, cacheModel: CacheModel) {

        //TODO 考虑使用子线程的方式做实现
        try {
            hasEmpty(cacheModel.cacheBody, key).assertFalse("cacheBody和cacheKey不能为空")
            available()
            lruCache.put(key, cacheModel)
            val model = LruDiskCacheModel(key, cacheModel.cacheBody)
            if (cacheModel is HttpCacheModel) {
                model.ETag = cacheModel.ETag
                model.cacheKey = key
                model.lastAccess = System.currentTimeMillis()
                model.lastModify = cacheModel.lastModify
                model.expires = cacheModel.expires
                model.cacheTypeKey = cacheTypeKey
            }

            val cache = db.get(key, LruDiskCacheModel::class)
            if (cache != null) {
                model.hitTimes = cache.hitTimes + 1
            }

            onPut(model)

            db.save(model)
            lruCache.trimToSize(MAX_LRU_CACHE)
            trim2Size()
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    override fun get(key: String): LruDiskCacheModel? {
        if (isEmpty(key)) return null
        available()
        val value = lruCache.get(key)
        if (value?.cacheBody.isNullOrEmpty()) {
            getSql(key)
        } else {
            EXECUTOR.execute {
                getSql(key)
            }
            return LruDiskCacheModel(key, value.cacheBody)
        }

        return null
    }

    private fun getSql(key: String): LruDiskCacheModel? {
        try {
            val model = db.get(key, LruDiskCacheModel::class) ?: return null
            availableCache(model).assertTrue()
            //更新缓存记录
            EXECUTOR.execute {
                model.hitTimes++
                model.lastAccess = System.currentTimeMillis()
                db.update(LruDiskCacheModel::class,
                        Pair("hitTimes", model.hitTimes),
                        Pair("lastAccess", model.lastAccess)) {
                    and("cacheKey", "=", model.cacheKey)
                }
            }
            return model
        } catch (e: Exception) {
            LogUtil.e(e)
        }
        return null
    }

    override fun clear(): Boolean {
        try {
            available()
            lruCache.clean()
            return clearWhere {
                and("cacheTypeKey", "=", cacheTypeKey)
            }
        } catch (e: Exception) {
            LogUtil.e(e)
        }
        return false
    }

    override fun clearAll(): Boolean {
        try {
            available()
            lruCache.clean()
            return clearWhere()
        } catch (e: Exception) {
            LogUtil.e(e)
        }
        return false
    }

    override fun remove(key: String): Boolean {
        try {
            available()
            lruCache.remove(key)
            val model = db.get(key, LruDiskCacheModel::class) ?: return true
            remove(model)
            return true
        } catch (e: Exception) {
            LogUtil.e(e)
        }
        return false
    }

    protected fun remove(model: LruDiskCacheModel) {
        onClearItem(model)
        db.delete(model)
    }

    private fun clearWhere(where: (IWhere.() -> Unit)? = null): Boolean {
        return try {
            val list = db.get(LruDiskCacheModel::class, where)
            list.forEach(::onClearItem)
            db.delete(LruDiskCacheModel::class, where)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun trim2Size() {

        try {
            available()
        } catch (e: Exception) {
            LogUtil.e(e)
        }

        if (trimCounter.getAndIncrement() > 0) {
            return
        }
        EXECUTOR.execute {
            var missCount = 1
            while (missCount > 0) {
                try {
                    //获取count，并检查
                    val config = globalConfig.get()
                    val overflowLambda: (() -> Int) = {
                        (db.selector(LruDiskCacheModel::class).count() - config.cacheMaxCount).toInt()
                    }

                    if (overflowLambda() > 0) {
                        // 尝试删除无效缓存
                        trimUnavailable()
                        val overflow = overflowLambda()
                        if (overflow > 0) {
                            // 尝试lru方式删除缓存
                            trimLru(overflow)
                        }
                    }

                    // 子类做自己的trim逻辑
                    onTrim2Size()
                } catch (e: Exception) {
                    LogUtil.e(e)
                }
                missCount = trimCounter.addAndGet(-missCount)
            }
        }
    }

    /**验证当前缓存是否可用，如果不可用可以抛出错误，父类调用此方法都会try-catch*/
    protected open fun available() {
        if (!available) {
            throw BaseException(msg = "当前缓存对象已无效")
        }
    }

    protected fun getLruOrderList(count: Int = 0, where: (IWhere.() -> Unit)? = null): List<LruDiskCacheModel> {
        return db.selector(LruDiskCacheModel::class) {
            limit = count
            orderBy(ISelector.OrderBy("lastAccess"))
            orderBy(ISelector.OrderBy("hitTimes"))
            where(where)
        }.findAll()
    }

    /**验证给定缓存是否可用*/
    abstract fun availableCache(model: LruDiskCacheModel): Boolean

    /**当删除Item的时候调用，比如当缓存为文件时，做删除文件操作*/
    abstract fun onClearItem(model: LruDiskCacheModel)

    /**当在计算数据量大小，已经在子线程执行*/
    abstract fun onTrim2Size()

    /**当添加缓存时*/
    abstract fun onPut(model: LruDiskCacheModel)

    private fun trimUnavailable() {
        db.get(LruDiskCacheModel::class) {
            and("expires", "!=", 0)
            and("expires", "<", System.currentTimeMillis() / 1000)
        }.forEach { remove(it) }
    }

    private fun trimLru(overflow: Int) {
        getLruOrderList(overflow).forEach { remove(it) }
    }
}