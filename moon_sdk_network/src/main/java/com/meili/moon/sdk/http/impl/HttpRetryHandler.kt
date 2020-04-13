package com.meili.moon.sdk.http.impl

import com.alibaba.fastjson.JSONException
import com.meili.moon.sdk.exception.CancelledException
import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.IRetryHandler
import com.meili.moon.sdk.http.exception.HttpException
import java.io.FileNotFoundException
import java.net.*

/**
 * 默认的Http的重试控制器
 * Created by imuto on 17/11/28.
 */
class HttpRetryHandler : IRetryHandler<IHttpRequestParams> {
    /**最大尝试次数*/
    private var maxTimes = 2
    /**当前尝试次数*/
    private var currTimes = 0

    private companion object {
        //不在重试的黑名单
        val BLACK_LIST = listOf(
                HttpException::class,
                CancelledException::class,
                MalformedURLException::class,
                URISyntaxException::class,
                NoRouteToHostException::class,
                PortUnreachableException::class,
                ProtocolException::class,
                NullPointerException::class,
                FileNotFoundException::class,
                JSONException::class,
                SocketTimeoutException::class,
                UnknownHostException::class,
                IllegalArgumentException::class
        )
    }

    override fun retry(requestInstance: IHttpRequestParams, throwable: Throwable): Boolean {
        val retry = currTimes < maxTimes
                && HttpMethod.permitsRetry(requestInstance.method)
                && !BLACK_LIST.contains(throwable::class)
        currTimes++
        return retry
    }


    override fun getMaxTimes(): Int = maxTimes
    override fun getTimes(): Int = currTimes
}