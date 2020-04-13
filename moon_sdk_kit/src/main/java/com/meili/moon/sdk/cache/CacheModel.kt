package com.meili.moon.sdk.cache

/**
 * 定义缓存model
 * 1. 缓存id
 * 2. 混存大小
 * 3. 获取缓存内容
 *
 * Created by imuto on 18/3/8.
 */
interface CacheModel {
    /**数据缓存对象*/
    var cacheBody: String
}