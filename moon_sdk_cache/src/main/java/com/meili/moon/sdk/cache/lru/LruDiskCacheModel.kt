package com.meili.moon.sdk.cache.lru

import com.meili.moon.sdk.cache.HttpCacheModel
import com.meili.moon.sdk.db.annotation.Column
import com.meili.moon.sdk.db.annotation.Table
import java.io.File

/**
 * 硬盘数据缓存对象
 * Created by imuto on 18/3/13.
 */
@Table
class LruDiskCacheModel : HttpCacheModel {

    constructor(cacheKey: String, cacheBody: String) {
        this.cacheKey = cacheKey
        this.cacheBody = cacheBody
    }

    constructor()

    @Column(isId = true)
    lateinit var cacheKey: String


    override var cacheBody: String = ""

    var cacheTypeKey: String = ""


    /**
     * 到期时间，以秒为单位
     * 生成方式：
     *  1. 有max-age属性时，[expires] = [System.currentTimeMillis] / 1000 + max-age
     *  2. 没有max-age，有expires属性时，[expires] = expires
     */
    override var expires: Long = 0

    /**etag记录*/
    override var ETag: String? = null

    /**最后修改时间，源于服务器*/
    override var lastModify: Long = 0

    /**碰撞次数，碰撞次数越高代表当前文件有效性越高*/
    var hitTimes = 0

    /**最后访问时间*/
    var lastAccess: Long = 0

    /**是否是文件缓存*/
    var isFileCache: Boolean = false

    @Column(ignore = true)
    var file: File? = null

    override fun toString(): String {
        return "LruDiskCacheModel(cacheBody='$cacheBody', " +
                "cacheTypeKey='$cacheTypeKey', " +
                "cacheKey='$cacheKey', " +
                "expires=$expires, " +
                "ETag=$ETag, " +
                "lastModify=$lastModify, " +
                "hitTimes=$hitTimes, " +
                "lastAccess=$lastAccess, " +
                "isFileCache=$isFileCache, " +
                "file=$file)"
    }

}