package com.meili.moon.sdk.cache.lru

import com.meili.moon.sdk.cache.lru.model.DefCacheModel
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.TAG

/**
 *
 * Created by imuto on 18/3/15.
 */
class ApiLruCache internal constructor(cacheTypeKey: String) : BaseLruDbCache<String>(cacheTypeKey) {

    init {
        LogUtil.d(TAG, "创建了api缓存对象，缓存对象：${this}，cacheTypeKey：$cacheTypeKey")
    }


    override fun put(key: String, value: String) {
        put(key, DefCacheModel(value))
    }

    override fun onPut(model: LruDiskCacheModel) {
    }

    override fun getValue(key: String): String? {
        return get(key)?.cacheBody
    }

    override fun availableCache(model: LruDiskCacheModel): Boolean {
        return model.expires == 0L || model.expires * 1000 > System.currentTimeMillis()
    }

    override fun onClearItem(model: LruDiskCacheModel) {
    }

    override fun onTrim2Size() {
    }
}
