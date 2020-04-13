package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.IParamsBuilder
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.annotation.HttpRequest
import com.meili.moon.sdk.http.common.CacheHandler
import com.meili.moon.sdk.http.common.CacheHandlerImpl
import com.meili.moon.sdk.http.util.parseKV
import com.meili.moon.sdk.util.isEmpty
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.reflect.full.createInstance

/**
 * 实现的网络请求基类
 * Created by imuto on 17/11/29.
 */
abstract class SdkHttpRequestParams
@JvmOverloads constructor(private var url: String = "",
                          private var builder: IParamsBuilder<IHttpRequestParams>? = null)
    : IHttpRequestParams, CacheHandler by CacheHandlerImpl {

    /**保存header*/
    private val header = HashMap<String, String>()
    /**保存param*/
    private val params = HashMap<String, Any?>()
    /**保存param*/
    private val outParams = HashMap<String, Any?>()
    /**保存header*/
    private val outParamsStream = HashMap<String, Any>()
    /**保存path param*/
    private val mPathParams = ArrayList<Any>()
    /**保存header*/
    private val paramsStream = HashMap<String, Any>()
    /**设置请求特性的注解成员*/
    private var httpRequest: HttpRequest? = null
    /**构建的url*/
    private var buildUri: String? = null
    /**默认的请求方式*/
    override var method: HttpMethod = HttpMethod.GET

    override val cacheKey: String
        get() = buildUri ?: "noKey"

    companion object {
        /**参数特性：入参为null的部分，将上传给服务器*/
        const val PARAM_FEATURE_WITH_NULL = 0b01
        /**参数特性：入参为null的部分，将转换为默认值，并且上传给服务器*/
        const val PARAM_FEATURE_NULL_2_EMPTY = 0b10
    }

    override fun init() {
        header.clear()
        params.clear()
        paramsStream.clear()
        //初始化response

        if (httpRequest == null && this::class != SdkHttpRequestParams::class) {
            httpRequest = javaClass.getAnnotation(HttpRequest::class.java)
        }

        if (isEmpty(url) && httpRequest == null) {
            throw IllegalStateException("uri is empty && @HttpRequest == null")
        }

        //初始化参数
        parseKV(this, javaClass, { name, value ->
            internalAddParam(name, value)
        }, { name, value ->
            addPathParam(value)
        })

        //如果httpRequest的注解builder不为默认builder，则使用注解builder，否则使用默认builder
        if (httpRequest != null && builder == null) {
            val builderClazz = httpRequest!!.builder.takeIf { !it.java.isInterface }
            try {
                if (builderClazz != SdkHttpParamsBuilder::class)
                    builder = builderClazz?.createInstance()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        builder = builder ?: getDefaultParamBuilder()
        builder?.buildParams(this)
        builder?.buildSign(this)
        buildUri = builder?.buildUri(this, httpRequest, url)
    }

    override fun getHeader(): MutableMap<String, String> = header

    override fun addHeader(key: String, value: String) {
        header[key] = value
    }

    override fun getUrl(): String? = buildUri

    override fun setUrl(url: String) {
        this.url = url
    }

    override fun getParams(): MutableMap<String, Any?> {
        return params.toMutableMap().apply {
            putAll(paramsStream)
            putAll(outParams)
            putAll(outParamsStream)
        }
    }

    override fun getPathParams(): MutableList<Any> {
        return mPathParams.toMutableList()
    }

    override fun addParam(key: String, value: Any) {
        var v: Any? = filterParam(key, value)
        if (v == null) {
            v = when {
                isConvertNull2EmptyParam() -> ""
                isUseNullParam() -> "null"
                else -> return
            }
        }
        if (v is File || v is ByteArray) {
            outParamsStream[key] = v
        } else {
            outParams[key] = v
        }
    }

    private fun internalAddParam(key: String, value: Any?) {
        var v: Any? = filterParam(key, value)
        if (v == null) {
            v = when {
                isConvertNull2EmptyParam() -> ""
                isUseNullParam() -> "null"
                else -> return
            }
        }
        if (v is File || v is ByteArray) {
            paramsStream[key] = v
        } else {
            params[key] = v
        }
    }

    override fun addPathParam(value: Any) {
        mPathParams.add(value)
    }

    override fun containParam(key: String): Boolean {
        if (isEmpty(key)) return false

        return params.containsKey(key) || paramsStream.containsKey(key)
    }

    /**获取入参的特性，请参见[PARAM_FEATURE_WITH_NULL]、[PARAM_FEATURE_NULL_2_EMPTY]等*/
    open fun getParamFeatures(): Int {
        return 0
    }

    /**获取默认的builder*/
    abstract fun getDefaultParamBuilder(): IParamsBuilder<IHttpRequestParams>

    /**过滤并处理param*/
    protected open fun filterParam(key: String, value: Any?): Any? {
        if (value is Date) {
            return value.time
        }
        return value
    }

    override fun toString(): String {
        return "SdkHttpRequestParams(" +
                "url='$url', \n" +
                "buildUri=$buildUri, \n" +
                "method=$method, \n" +
                "header=$header, \n" +
                "params=$params, \n" +
                "paramsStream=$paramsStream \n)"
    }

    override fun cacheable(): Boolean {
        return false
    }

    /**是否使用null参数*/
    private fun isUseNullParam(): Boolean {
        return getParamFeatures() and PARAM_FEATURE_WITH_NULL != 0
    }

    /**是否将null值的参数转换为空值*/
    private fun isConvertNull2EmptyParam(): Boolean {
        return getParamFeatures() and PARAM_FEATURE_NULL_2_EMPTY != 0
    }
}