package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.common.ProgressLambda
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.IResponse
import com.meili.moon.sdk.http.IResponseParser
import com.meili.moon.sdk.http.exception.HttpException
import com.meili.moon.sdk.util.isNetworkEnable

/**
 * 默认的抽象loader
 * Created by imuto on 17/11/30.
 */
abstract class SdkHttpLoader<out ResultType>(
        val params: IHttpRequestParams,
        override var progressHandler: ProgressLambda = null) : IResourceLoader<ResultType> {

    private var isLoading = false

    override fun validate(): Boolean {
        if (!isNetworkEnable()) {
            throw HttpException(msg = "网络不可用")
        }
        return true
    }

    override fun load(): ResultType {
        isLoading = true
        val result: ResultType
        try {
            result = parse(request(params))
        } catch (throwable: Throwable) {
            isLoading = false
            throw throwable
        }
        isLoading = false
        return result
    }

    override fun isLoading(): Boolean = isLoading

    private fun parse(response: IResponse): ResultType {
        params.response.httpState = response.code
        params.response.headers = response.headers
        if (response.code !in 200..299) {
            throw HttpException(response.code, "服务器开小差(${response.code})")
        }
        return parseResult(response)
    }

    /**发起请求*/
    internal abstract fun request(params: IHttpRequestParams): IResponse

    /**解析结果*/
    internal abstract fun parseResult(rsp: IResponse): ResultType

    override fun hasCancelled() = !isLoading

    /** 业务端的数据解析对象 */
    override var parser: IResponseParser? = null

    /** 业务端的数据接收对象 */
    override var response: IHttpResponse? = null
}