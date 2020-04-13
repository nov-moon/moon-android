package com.meili.moon.sdk.http.common

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.cache.CacheModel
import com.meili.moon.sdk.cache.DiskCache
import com.meili.moon.sdk.cache.HttpCacheModel
import com.meili.moon.sdk.common.CacheLambda
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.loader.IResourceLoader
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.task.AbsTask
import com.meili.moon.sdk.util.isEmpty
import okhttp3.Response

/**
 * 在这里实现的原因是不想让缓存处理散落代码各处.
 *
 * 当前的cache实现，只是硬性cache处理。不会根据服务器是否允许cache而进行cache（需要服务器配置，暂时没有资源）。
 * 而是根据客户端配置，直接做cache处理
 *
 * Created by imuto on 2018/3/27.
 */
object CacheHandlerImpl : CacheHandler {

    /**需要子实现类进行重写*/
    override val cacheKey: String = ""

    private val cache: DiskCache<CacheModel, String>?

    init {
        cache = try {
            CommonSdk.cache()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun cacheable(): Boolean {
        return cache != null
    }

    override fun tryInitCacheModel(resp: Response, httpResp: IHttpResponse?) {
        // 处理缓存内容
        if (httpResp !is HttpCacheModel) {
            return
        }

        try {
            val httpCacheModel = httpResp as HttpCacheModel
            httpCacheModel.ETag = resp.header("ETag")
            httpCacheModel.lastModify = resp.headers()?.getDate("Last-Modified")?.time ?: 0
            httpCacheModel.expires = resp.headers()?.getDate("Expires")?.time ?: 0
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    override fun <T> tryUseCache(params: IRequestParams.IHttpRequestParams, cacheCallback: CacheLambda, loader: IResourceLoader<*>?, task: AbsTask<T>) {
        if (!cached(params, loader)) {
            return
        }

        val cacheBody = cache?.getValue((params as IRequestParams.ICacheable).cacheKey)
                ?: return

        try {
            if (cacheCallback != null && !cacheCallback.invoke(cacheBody)) {
                return
            }

            val response = params.response
            response.response = cacheBody
            val result = loader!!.parser!!.parse(response, response.itemKClass)
            if (result != null) {
                if (response.isListResult) {
                    task.setResult(result as T)
                } else {
                    task.setResult(result[0] as T)
                }
            }

            task.getProxy()?.onSuccess(task.getResult()!!)
            task.getProxy()?.onFinished(true)
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    override fun saveCache(params: IRequestParams.IHttpRequestParams, loader: IResourceLoader<*>?) {
        if (!cached(params, loader)) {
            return
        }

        val response = loader?.response ?: return

        if (!response.isSuccess()) return

        try {
            val key = (params as IRequestParams.ICacheable).cacheKey
            //如果是缓存model，则直接做缓存操作
            if (response is CacheModel) {
                cache?.put(key, response)
            } else {
                // 如果不是缓存对象，则进行string类型缓存
                val responseStr = response.response
                if (!isEmpty(responseStr)) {
                    cache?.put(key, responseStr!!)
                }
            }
        } catch (e: Exception) {
            LogUtil.e(e)
        }
    }

    override fun isSame2Cache(params: IRequestParams.IHttpRequestParams): Boolean {
        if (params !is IRequestParams.ICacheable || !params.cacheable()) {
            return false
        }

        val cacheBody = cache?.getValue(params.cacheKey)

        return !isEmpty(cacheBody) && cacheBody == params.response.response
    }

    private fun cached(params: IRequestParams.IHttpRequestParams, loader: IResourceLoader<*>?): Boolean {
        //是Cacheable的子类，并且可以缓存，并且loader的parser不为null，并且loader的response不为null
        return cache != null && params is IRequestParams.ICacheable
                && params.cacheable()
                && loader?.parser != null
                && loader.response != null
    }
}