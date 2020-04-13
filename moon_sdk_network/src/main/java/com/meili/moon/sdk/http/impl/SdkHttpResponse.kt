package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.cache.HttpCacheModel
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.util.ParameterizedTypeUtil
import com.meili.moon.sdk.util.isArrayType
import com.meili.moon.sdk.util.isEmpty
import java.lang.reflect.ParameterizedType
import java.lang.reflect.WildcardType
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KType

/**
 * response的基类
 * Created by imuto on 17/12/5.
 */
abstract class SdkHttpResponse : IHttpResponse, HttpCacheModel {

    override var response: String? = ""
    override var data: String? = null
    override var state: Int = -1
    override var httpState: Int = 0
    override var message: String = ""

    // cache相关属性
    override var ETag: String? = null
    override var lastModify: Long = 0
    override var expires: Long = 0
    override var cacheBody: String = ""
        get() = response ?: ""

    override var headers: Map<String, String> = emptyMap()

    var originalThrowable: Throwable? = null

    var hasSuccess = true

    override fun handleParseData(response: String?) = false

    override fun handleError(throwable: Throwable): BaseException {
        hasSuccess = false
        originalThrowable = throwable
        if (isEmpty(message)) {
            message = when (throwable) {
                is UnknownHostException -> "服务器连接失败"
                is TimeoutException -> "网络连接超时"
                is ConnectException -> "网络连接错误"
                else -> {
                    return throwable as? BaseException
                            ?: BaseException(msg = throwable.message, cause = throwable)
                }
            }
        }
        return BaseException(msg = message, cause = throwable)
    }

    override var isListResult: Boolean = false
    override var itemKClass: Class<*> = String::class.java

    /**当前只解析两种类型，一种是lambda表达式的泛型，另外一种是callback上绑定的泛型*/
    override fun parseItemParamType(paramEntity: Any?) {
        if (paramEntity == null) {
            return
        }
        itemKClass = if (paramEntity is KType) {
            var clazz = (paramEntity.classifier as KClass<*>).java
            if (clazz.isArrayType()) {
                clazz = (paramEntity.arguments[0].type!!.classifier as KClass<*>).java
                isListResult = true
            }
            clazz
        } else if (paramEntity is KFunction<*>) {
            val paramType = paramEntity.parameters[0].type
            var clazz = (paramType.classifier as KClass<*>).java
            if (clazz.isArrayType()) {
                clazz = (paramType.arguments[0].type!!.classifier as KClass<*>).java
                isListResult = true
            }
            clazz
        } else if (paramEntity is ParameterizedType) {
            if (paramEntity.rawType.isArrayType()) {
                isListResult = true
                val arg0 = paramEntity.actualTypeArguments[0]
                if (arg0 is WildcardType) {
                    if (!isEmpty(arg0.upperBounds)) {
                        arg0.upperBounds[0] as Class<*>
                    } else {
                        arg0.lowerBounds[0] as Class<*>
                    }
                } else {
                    paramEntity.actualTypeArguments[0] as Class<*>
                }
            } else {
                val directParameterizedType = paramEntity.getActualTypeArguments()[0]
                if (directParameterizedType is ParameterizedType) {
                    if (directParameterizedType.rawType.isArrayType()) {
                        isListResult = true
                        directParameterizedType.actualTypeArguments[0] as Class<*>
                    } else {
                        directParameterizedType.rawType as Class<*>
                    }
                } else {
                    directParameterizedType as Class<*>
                }
            }
        } else {
            val directParameterizedType = ParameterizedTypeUtil.getDirectParameterizedType(paramEntity::class.java)
            if (directParameterizedType is ParameterizedType) {
                if (directParameterizedType.rawType.isArrayType()) {
                    isListResult = true
                    directParameterizedType.actualTypeArguments[0] as Class<*>
                } else {
                    directParameterizedType.rawType as Class<*>
                }
            } else {
                directParameterizedType as Class<*>
            }
        }
    }

    override fun isSuccess() = hasSuccess
}