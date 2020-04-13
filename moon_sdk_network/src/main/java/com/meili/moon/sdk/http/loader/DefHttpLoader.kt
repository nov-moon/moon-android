package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.http.*
import com.meili.moon.sdk.http.annotation.HttpResponse
import com.meili.moon.sdk.http.annotation.HttpResponseParser
import com.meili.moon.sdk.http.common.CacheHandler
import com.meili.moon.sdk.http.exception.HttpException
import com.meili.moon.sdk.http.util.OkHttpClientDelegate
import com.meili.moon.sdk.util.isEmpty
import okhttp3.Call
import okhttp3.Request
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

/**
 * 默认的http请求及解析器
 * Created by imuto on 17/11/30.
 */
open class DefHttpLoader<out ResultType>(
        //请求入参
        params: IRequestParams.IHttpRequestParams)
    : SdkHttpLoader<ResultType>(params) {

    private var mCall: Call? = null
    private var httpRequest: Request? = null

    private val resultClass = params.response.itemKClass.kotlin

    override var response: IHttpResponse? = null
        get() {
            //从param中获取response
            var httpResponse = params.response
            //如果resultClass是response类型的，则使用resultClass作为response接收器
            if (IHttpResponse::class.isSuperclassOf(resultClass)) {
                httpResponse = resultClass.createInstance() as IHttpResponse
                params.response = httpResponse
                return httpResponse
            }

            val respAnnotation = resultClass.findAnnotation<HttpResponse>() ?: return httpResponse

            httpResponse = respAnnotation.value.createInstance()
            params.response = httpResponse
            return httpResponse
        }

    override var parser: IResponseParser? = null
        get() {
            //获取注解，优先使用resultClass的注解，如果没有则使用httpResponse上的注解
            var annotation: HttpResponseParser? = resultClass.findAnnotation()
            if (annotation == null) {
                annotation = response!!.javaClass.getAnnotation(HttpResponseParser::class.java)
            }

            var parser = annotation?.value?.createInstance()
            if (parser == null) {
                parser = response!!.parser
            }
            return parser
        }

    override fun request(params: IRequestParams.IHttpRequestParams): IResponse {
        val clientInit: (Call, Request) -> Unit = { client, request ->
            mCall = client
            httpRequest = request
        }
        val resp = when (params.method) {
            HttpMethod.GET -> OkHttpClientDelegate.get(params, clientCallback = clientInit)
            HttpMethod.POST -> OkHttpClientDelegate.post(params, clientCallback = clientInit)
            else -> throw BaseException(msg = "don't support request method type : " + params.method)
        }

        // 初始化缓存对象
        if (params is CacheHandler && params.cacheable()) {
            params.tryInitCacheModel(resp, response)
        }

        val okHttpResponse = OkHttpResponse(resp)

        if (params is IRequestParams.IHttpAdvanceFeatures) {
            params.getResponseInterceptor()?.intercept(okHttpResponse)
        }

        return okHttpResponse
    }

    override fun parseResult(rsp: IResponse): ResultType {
        this.response!!.response = rsp.bodyByString
        val result: List<*>? = try {
            parser!!.parse(this.response!!, resultClass.java)
        } catch (ex: Exception) {
            if (ex is BaseException) {
                throw ex
            } else {
                throw HttpException(msg = "数据解析错误", cause = ex)
            }
        }

        var firstItem: Any? = null

        if (!isEmpty(result)) {
            firstItem = result!![0]
        }

        @Suppress("UNCHECKED_CAST")
        return if (!params.response.isListResult) {
            val resultType = firstItem as? ResultType
            if (resultType == null) {
                when {
                    resultClass.java.isAssignableFrom(JvmType.Object::class.java) -> {
                        Any() as ResultType
                    }
                    resultClass.java.isAssignableFrom(String::class.java) -> {
                        "" as ResultType
                    }
                    else -> {
                        CommonSdk.json().toObject("{}", resultClass.java) as ResultType
                    }
                }
            } else {
                resultType
            }
        } else {
            result as ResultType
        }
    }

    override fun cancel(immediately: Boolean) {
        if (!hasCancelled() && immediately) {
            mCall?.cancel()
        }
    }

}