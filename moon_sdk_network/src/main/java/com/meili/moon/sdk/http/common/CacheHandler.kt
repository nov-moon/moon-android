package com.meili.moon.sdk.http.common

import com.meili.moon.sdk.common.CacheLambda
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.loader.IResourceLoader
import com.meili.moon.sdk.task.AbsTask
import okhttp3.Response

/**
 * 缓存处理接口
 * Created by imuto on 2018/3/27.
 */
interface CacheHandler: IRequestParams.ICacheable {
    /**尝试使用cache*/
    fun <T> tryUseCache(params: IRequestParams.IHttpRequestParams, cacheCallback: CacheLambda, loader: IResourceLoader<*>?, task: AbsTask<T>)

    /**尝试保存cache*/
    fun saveCache(params: IRequestParams.IHttpRequestParams, loader: IResourceLoader<*>?)

    /**尝试保存cacheModel内容,[httpResp]之所以不是CacheModel类型，是因为，这里想把判断和可能的错误在方法体里处理，不给外部太多负担*/
    fun tryInitCacheModel(resp: Response, httpResp: IHttpResponse?)

    /**是否当前数据和缓存数据相同*/
    fun isSame2Cache(params: IRequestParams.IHttpRequestParams): Boolean
}