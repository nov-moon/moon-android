package com.meili.moon.sdk.cache

/**
 * http缓存的判断条件
 * Created by imuto on 18/3/14.
 */
interface HttpCacheModel : CacheModel {
    var ETag: String?
    var lastModify: Long
    var expires: Long
}