package com.meili.moon.sdk.http

import java.net.Proxy
import javax.net.ssl.SSLSocketFactory

/**
 * 请求的
 * Created by imuto on 17/11/28.
 */
interface IRequestParams {
    companion object {
        /**默认的连接超时时间*/
        const val CONNECT_TIME_OUT = 1000 * 60L
        const val CHARSET = "UTF-8"
    }


//    abstract fun getRequestBody(): RequestBody
//
//    abstract fun convert2ProgressRequest(handler: ProgressHandler)

    /**http的基本功能封装*/
    interface IHttpRequestParams : IRequestParams {

        /**请求结果*/
        var response: IHttpResponse
        /**网络请求的方法*/
        var method: HttpMethod

        /**获取header,默认为空header*/
        fun getHeader(): MutableMap<String, String>

        /**添加header*/
        fun addHeader(key: String, value: String)

        /**获取url*/
        fun getUrl(): String?

        /**设置url*/
        fun setUrl(url: String)

        /**获取连接超时，带有默认设置*/
        fun getConnectTimeOut() = CONNECT_TIME_OUT

        /**获取读取超时，带有默认设置*/
        fun getReadTimeOut() = CONNECT_TIME_OUT

        /**获取写入超时，带有默认设置*/
        fun getWriteTimeOut() = CONNECT_TIME_OUT

        /**是否在请求过程中启用cookie*/
        fun isUseCookie(): Boolean = true

        /**获取编码信息，带有默认编码*/
        fun getCharset() = CHARSET

        /**是否使用json格式上传数据*/
        fun isUseJsonFormat() = false

        /**是否使用解析器*/
        fun addParser(parser: IResponseParser)
    }

    /**http的高级特性*/
    interface IHttpAdvanceFeatures {
        /**获取代理*/
        fun getProxy(): Proxy? = null

        /**是否使用gzip*/
        fun isGzip() = false

        /**获取重试控制器*/
        fun getRetryHandler(): IRetryHandler<IHttpRequestParams>? = null

        /**获取请求记录器*/
        fun getTracker(): IRequestTracker? = null

        /**获取https的处理类*/
        fun getSSLSocketFactory(): SSLSocketFactory? = null

        /**获取返回数据集的拦截处理器，可以拦截到最原始的response信息*/
        fun getResponseInterceptor(): IResponseInterceptor? = null

        /**request的过滤器，在发起请求和请求结束，都会被调用*/
        fun getRequestFilter(): IRequestFilter? = null
    }

    /**数据模拟的特性定制*/
    interface MockFeatures {
        /**获取使用模拟数据的策略*/
        fun getUseMockDataStrategy(): MockStrategy

        /**Mock的策略*/
        enum class MockStrategy {
            /**关闭mock*/
            OFF,
            /**打开mock：如果没有mock，则使用网络等其他方式尝试加载*/
            ON,
            /**打开mock：并且强制使用mock*/
            ON_FORCE,
            /**制作mock模板，暂时无用*/
            MAKE_TEMPLATE
        }
    }

    /**可用缓存*/
    interface ICacheable {
        /**是否可用缓存*/
        fun cacheable(): Boolean

        /**缓存key*/
        val cacheKey: String
    }

    /**下载特性定制*/
    interface IDownloadFeatures {
        /**获取进度刷新间隔*/
        fun getProgressSpacingTime(): Long = 300L

        /**文件保存路径*/
        var downloadFileSavePath: String

        /**是否使用断点续传*/
        fun isAutoResume(): Boolean = true

        /**是否根据头信息自动命名文件*/
        fun isAutoRename(): Boolean = false
    }

    /**获取请求入参*/
    fun getParams(): MutableMap<String, Any?>

    /**获取pathParam请求入参*/
    fun getPathParams(): MutableMap<String, Any>

    /**添加请求参数*/
    fun addParam(key: String, value: Any)

    /**添加pathParam请求参数*/
    fun addPathParam(key: String, value: Any)

    /**添加pathParam请求参数*/
    fun addPathParam(value: Any)

    fun containParam(key: String): Boolean

    fun addExtraConvert(convert: IResponseExtraConvert)

    fun getExtraConvert(): IResponseExtraConvert?

    /**初始话params*/
    fun init()
}