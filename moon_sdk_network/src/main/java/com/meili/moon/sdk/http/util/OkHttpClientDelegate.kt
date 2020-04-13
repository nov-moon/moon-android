package com.meili.moon.sdk.http.util

import com.meili.moon.sdk.common.ProgressLambda
import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.body.ProgressRequestBody
import com.meili.moon.sdk.http.interceptor.GzipRequestInterceptor
import com.meili.moon.sdk.log.LogUtil
import okhttp3.*
import java.util.concurrent.TimeUnit

internal typealias OkHttpClientCallback = ((Call, Request) -> Unit)?

/**
 * Okhttp网络库的委托调用类
 * Created by imuto on 17/12/5.
 */
internal object OkHttpClientDelegate {
    private val mOkHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
                .connectTimeout(IRequestParams.CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .readTimeout(IRequestParams.CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .writeTimeout(IRequestParams.CONNECT_TIME_OUT, TimeUnit.MILLISECONDS)
                .addInterceptor(GzipRequestInterceptor())
        builder.build()
    }

    /**发起同步的post请求*/
    fun post(params: IHttpRequestParams, progress: ProgressLambda = null, tag: Any? = null, clientCallback: OkHttpClientCallback = null): Response {
        var requestBody = getRequestBody(params)
        if (progress != null && requestBody != null) {
            requestBody = ProgressRequestBody(requestBody, progress)
        }
        val client = getClient(params)
        val request = getRequest(params, requestBody, HttpMethod.POST, tag)
        val newCall = client.newCall(request)
        clientCallback?.invoke(newCall, request)
        return newCall.execute()
    }

    /**发起同步的get请求*/
    fun get(params: IHttpRequestParams, tag: Any? = null, clientCallback: OkHttpClientCallback = null): Response {
        val client = getClient(params)
        val request = getRequest(params, null, HttpMethod.GET, tag)
        val newCall = client.newCall(request)
        clientCallback?.invoke(newCall, request)
        return newCall.execute()
    }

    private fun getRequest(params: IHttpRequestParams, requestBody: RequestBody?, method: HttpMethod, tag: Any?): Request {
        val builder = Request.Builder()

        val url = params.getUrl()
        builder.url(url)
        if (tag != null) {
            builder.tag(tag)
        }
        val header = params.getHeader()
        for ((key, value) in header) {
            try {
                builder.header(key, value)
            } catch (e: Exception) {
                LogUtil.e("error ( key = $key  value = $value)")
            }
        }

        if (params is IRequestParams.IHttpAdvanceFeatures) {
            //是否开启gzip
            builder.header(GzipRequestInterceptor.KEY_GZIP, if (params.isGzip()) {
                GzipRequestInterceptor.VALUE_OPEN
            } else {
                GzipRequestInterceptor.VALUE_CLOSE
            })
        }

        when (method) {
            HttpMethod.POST -> builder.post(fixRequestBody(requestBody))
            HttpMethod.PUT -> builder.put(fixRequestBody(requestBody))
            HttpMethod.DELETE -> builder.delete(requestBody)
            else -> Unit
        }
        return builder.build()
    }

    /**获取要使用的client*/
    private fun getClient(param: IHttpRequestParams): OkHttpClient {
        if (useDefClient(param)) {
            return mOkHttpClient
        }
        val builder = mOkHttpClient.newBuilder()
                .connectTimeout(param.getConnectTimeOut(), TimeUnit.MILLISECONDS)
                .readTimeout(param.getReadTimeOut(), TimeUnit.MILLISECONDS)
                .writeTimeout(param.getWriteTimeOut(), TimeUnit.MILLISECONDS)
        if (param is IRequestParams.IHttpAdvanceFeatures) {
            val sslSocketFactory = param.getSSLSocketFactory()
            if (sslSocketFactory != null) {
                builder.sslSocketFactory(sslSocketFactory)
            }
        }
        return builder.build()
    }

    /**修复requestBody*/
    private fun fixRequestBody(requestBody: RequestBody?) = requestBody ?: FormBody.Builder().build()

    /**是否可以使用默认client*/
    private fun useDefClient(param: IHttpRequestParams): Boolean {
        return param.getConnectTimeOut() == IRequestParams.CONNECT_TIME_OUT
                && param.getReadTimeOut() == IRequestParams.CONNECT_TIME_OUT
                && param.getWriteTimeOut() == IRequestParams.CONNECT_TIME_OUT
                && param.isUseCookie()
    }
}