@file:JvmName("HttpHelper")

package com.meili.moon.sdk.http.util

import com.meili.moon.sdk.exception.JsonException
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.annotation.HttpParam
import com.meili.moon.sdk.http.annotation.HttpParamModel
import com.meili.moon.sdk.http.impl.SdkHttpRequestParams
import com.meili.moon.sdk.json.JsonHelperImpl
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.isEmpty
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URLConnection
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.kotlinProperty

/**
 * http帮助类
 * Created by imuto on 17/11/30.
 */
private val BOOT_CL = String::class.java.classLoader

/***/
val IRequestParams.paramTypeBlackList: MutableList<Class<*>> by lazy {
    mutableListOf<Class<*>>()
}

/**item的过滤器，如果lambda返回true，则认为过滤掉了，如果为false则认为合法*/
val IRequestParams.paramFilter: MutableList<(IRequestParams, Any, Class<*>, Field?) -> Boolean> by lazy {
    val list = mutableListOf<(IRequestParams, Any, Class<*>, Field?) -> Boolean>()
    //添加黑名单
    list.add({ params, entity, type, field ->
        if (field == null) {
            type.getAnnotation(com.meili.moon.sdk.http.annotation.HttpIgnoreBuildParam::class.java) != null
        } else {
            with(field) {
                isAccessible = true
                Modifier.isTransient(modifiers) ||
                        params.paramTypeBlackList.contains(this::class.java) ||
                        getAnnotation(com.meili.moon.sdk.http.annotation.HttpIgnoreBuildParam::class.java) != null ||
                        name == "serialVersionUID"
            }
        }
    })
    //添加通用过滤
    list.add({ params, entity, type, field ->
        if (field != null) params.paramTypeBlackList.contains(field::class.java) else false
    })
    list
}

/**解析参数*/
fun IRequestParams.parseKV(entity: Any, type: Class<*>, paramCallback: (String, Any?) -> Unit, pathParamCallback: (String, Any) -> Unit) {
    if (type == SdkHttpRequestParams::class.java) {
        return
    } else {
        val cl = type.classLoader
        if (cl == null || cl == BOOT_CL) {
            return
        }
    }

    paramFilter.forEach {
        if (it(this, entity, type, null)) return
    }

    val fields = type.declaredFields
    fields?.forEach {
        it.run {
            paramFilter.forEach {
                if (it(this@parseKV, entity, type, this)) return@run
            }
            isAccessible = true
            try {
                val property = get(entity) ?: null

                val atnParamModel = kotlinProperty?.findAnnotation<HttpParamModel>()

                //如果是ParamModel，则做相应解析
                if (atnParamModel != null && property != null) {
                    parseKV(property, property::class.java, paramCallback, pathParamCallback)
                } else {
                    var httpParamAnt = it.kotlinProperty?.findAnnotation<HttpParam>()
                    if (httpParamAnt == null) {
                        httpParamAnt = it.getAnnotation(HttpParam::class.java)
                    }
                    if (httpParamAnt == null) {
                        paramCallback(name, property)
                    } else {
                        if (httpParamAnt.pathParam && property != null) {
                            pathParamCallback(name, property)
                        } else {
                            paramCallback(name, property)
                        }
                    }
                }
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }
        }
    }

    parseKV(entity, type.superclass, paramCallback, pathParamCallback)
}

/**获取requestBody*/
internal fun getRequestBody(params: IHttpRequestParams): RequestBody? {
    val paramsMap = params.getParams()
    if (isEmpty(paramsMap)) {
        return null
    }
    val streamParam = paramsMap.filter { it.value is File || it.value is ByteArray }
    val normalParam = paramsMap.filter { it.value !is File && it.value !is ByteArray }
    return if (!isEmpty(streamParam) && !isEmpty(normalParam)) {
        getMultiRequestBody(normalParam, streamParam)
    } else if (!isEmpty(streamParam)) {
        getFileRequestBody(streamParam, params.getCharset())
    } else if (!isEmpty(normalParam)) {
        val value: Any? = filterJsonObject(normalParam)
        when {
            value != null -> RequestBody.create(getMediaTypeJson(params.getCharset()), value.toString())
            params.isUseJsonFormat() -> RequestBody.create(getMediaTypeJson(params.getCharset()), JsonHelperImpl.toJson(normalParam))
            else -> {
                val builder = FormBody.Builder()
                normalParam.forEach {
                    builder.add(it.key, getString(it.value))
                }
                builder.build()
            }
        }
    } else null
}

private fun filterJsonObject(param: Map<String, Any?>): Any? {
    if (param.size == 1) {
        param.forEach {
            val value = it.value
            if (value is JSONObject
                    || value is JSONArray
                    || value is com.alibaba.fastjson.JSONArray
                    || value is com.alibaba.fastjson.JSONObject) {
                return value
            }
        }
    }
    return null
}

/**获取multiForm表单*/
private fun getMultiRequestBody(param: Map<String, Any?>, paramFile: Map<String, Any?>): RequestBody? {
    val builder = MultipartBody.Builder().setType(MultipartBody.FORM)

    for ((key, value) in param) {
        val resultValue = getString(value)
        builder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"$key\""),
                RequestBody.create(null, resultValue))
    }

    var fileBody: RequestBody
    for ((name, value) in paramFile) {
        if (value !is File) {
            LogUtil.e("file+参数,的请求,file必须是File类型")
            return null
        }
        if (value.exists() && value.isFile) {
            val fileName = value.name
            fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), value)
            //TODO 根据文件名设置contentType
            builder.addPart(
                    Headers.of("Content-Disposition",
                            "form-data; name=\"$name\"; filename=\"$fileName\""), fileBody)
        }
    }

    return builder.build()
}

private fun getFileRequestBody(paramFile: Map<String, Any?>, charset: String): RequestBody? {
    // TODO: 16/6/1 实现多文件上传
    paramFile.forEach {
        if (it.value is File) {
            return RequestBody.create(getMediaTypeStream(charset), it.value as? File)
        } else if (it.value is ByteArray) {
            return RequestBody.create(getMediaTypeStream(charset), it.value as? ByteArray)
        }
    }
    return null
}

private fun getString(value: Any?): String? {
    value ?: return null
    var resultValue = ""
    if (value.javaClass == String::class.java || value.javaClass.isPrimitive) {
        resultValue = value.toString()
    } else {
        try {
            resultValue = JsonHelperImpl.toJson(value)
        } catch (e: JsonException) {
            e.printStackTrace()
        }

    }
    return resultValue
}

//根据文件名称获取文件媒体类型
private fun guessMimeType(path: String): String {
    val fileNameMap = URLConnection.getFileNameMap()
    var contentTypeFor: String? = fileNameMap.getContentTypeFor(path)
    if (contentTypeFor == null) {
        contentTypeFor = "application/octet-stream"
    }
    return contentTypeFor
}


private fun getMediaTypeJson(charset: String) = MediaType.parse("application/json;charset=" + charset.toLowerCase())

private fun getMediaTypeStream(charset: String) = MediaType.parse("application/octet-stream;charset=" + charset.toLowerCase())