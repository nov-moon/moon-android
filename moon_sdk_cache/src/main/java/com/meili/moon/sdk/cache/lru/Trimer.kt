package com.meili.moon.sdk.cache.lru

/**
 * Created by imuto on 2018/3/20.
 */
interface Trimer {
    fun trimUnavailable()
    fun trimLru()
}