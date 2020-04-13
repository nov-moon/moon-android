package com.meili.moon.sdk.cache.lru.model

import com.meili.moon.sdk.cache.CacheModel

/**
 * 默认的cacheModel实现类
 * Created by imuto on 2018/3/22.
 */
data class DefCacheModel(override var cacheBody: String) : CacheModel