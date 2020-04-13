package com.meili.moon.sdk.http

import com.meili.moon.sdk.http.annotation.HttpRequest
import javax.net.ssl.SSLSocketFactory

/**
 * params的构建标准接口
 * Created by imuto on 17/11/28.
 */
interface IParamsBuilder<in Param : IRequestParams> {
    /**
     * 根据参数，构建请求的uri
     */
    fun buildUri(param: Param, httpRequest: HttpRequest?, path: String? = null): String?

    /**获取sslSocketFactory,默认返回null*/
    fun getSSLSocketFactory(): SSLSocketFactory? = null

    /**构建参数，添加通用头信息等*/
    fun buildParams(params: Param)

    /**构建签名信息，默认不做任何处理*/
    fun buildSign(param: Param) = Unit

    /**获取用户UA，默认为null*/
    fun getUserAgent(): String? = null

    /**获取当前构建的headers*/
    fun getHeaders(): MutableMap<String, String>
}