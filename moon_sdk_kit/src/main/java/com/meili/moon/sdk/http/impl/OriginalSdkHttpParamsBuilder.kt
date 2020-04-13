package com.meili.moon.sdk.http.impl

import android.os.Build
import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.IParamsBuilder
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.annotation.HttpRequest
import com.meili.moon.sdk.util.DeviceUtil
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.startWith
import java.net.URLEncoder
import java.util.*

/**
 * builder的抽象实现，实现了主要功能
 * Created by imuto on 17/11/28.
 */
abstract class OriginalSdkHttpParamsBuilder : IParamsBuilder<IHttpRequestParams> {
    override fun buildUri(param: IHttpRequestParams, httpRequest: HttpRequest?, path: String?): String? {
        var pathVar = path
        if (httpRequest == null) {
            return pathVar
        }
        val url = StringBuilder()

        if (isEmpty(pathVar)) {
            pathVar = httpRequest.value
        }

        if (!param.getPathParams().isEmpty()) {
            pathVar = pathVar ?: "/"
            if (!pathVar.endsWith("/")) {
                pathVar = "$pathVar/"
            }
            val sb = StringBuilder()
            param.getPathParams().forEach {
                if (it !is CharSequence || !it.isEmpty()) {
                    sb.append(it)
                    sb.append("/")
                }
            }
            if (!sb.isEmpty())
                sb.deleteCharAt(sb.length - 1)

            pathVar = "$pathVar$sb"
        }

        if (isEmpty(pathVar)) {
            return pathVar
        }

        //初始化host,如果入参pathVar本身就是一个完整url，则直接拼接pathVar，不做host处理
        if (isHttpUrl(pathVar!!)) {
            url.append(pathVar)
        } else {
            var host = httpRequest.host

            if (isEmpty(host)) {
                host = getDefaultHost()
            }

            url.append(host)

            if (!host.endsWith("/") && !pathVar.startsWith("/")) {
                url.append("/")
            } else if (host.endsWith("/") && pathVar.startsWith("/")) {
                pathVar = pathVar.substring(1)
            }
            url.append(pathVar)
        }

        if (!isEmpty(httpRequest.version)) {
            if (!pathVar.contains("?") && !pathVar.endsWith("/")) {
                url.append("/")
            }
            url.append(httpRequest.version)
        }

        val mapParam = param.getParams()

        if (param.method === HttpMethod.GET && !isEmpty(mapParam)) {
            if (!pathVar.contains("?")) {
                url.append("?")
            } else if (!pathVar.endsWith("&")) {
                url.append("&")
            }
            for (entry in mapParam.entries) {
                url.append(entry.key)
                url.append("=")
                url.append(entry.value.toString())
                url.append("&")
            }
        }

        if (url[url.length - 1] == '&') {
            url.deleteCharAt(url.length - 1)
        }
        if (url[url.length - 1] == '?') {
            url.deleteCharAt(url.length - 1)
        }
        return url.toString()
    }

    override fun buildParams(params: IHttpRequestParams) {
        getHeaders().forEach {
            params.addHeader(it.key, it.value)
        }
    }

    override fun getHeaders(): MutableMap<String, String> {
        val headers = mutableMapOf<String, String>()
        headers.apply {
            //当前的UA
            put("User-Agent", getUserAgent() ?: "")
            //当前设备系统类型
            put("X-platform", "Android")
            //当前设备系统版本号，比如5.1
            put("X-os", Build.VERSION.RELEASE ?: "")
            //手机型号，比如m3note
            put("X-product", Build.PRODUCT ?: "")
            //手机厂家，比如Meizu
            put("X-manufacture", Build.MANUFACTURER ?: "")
            //设备ID，当前格式为IMEI+$+MAC，以后有可能改变
            put("X-di", DeviceUtil.getDI() ?: "")
            try {
                //当前网络类型，比如wifi："MLJR"
                put("X-network", URLEncoder.encode(DeviceUtil.getCurrentNetType() + "", "UTF-8"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
            put("X-traceId", UUID.randomUUID().toString())
            //当前设备的mac地址
            put("X-macid", DeviceUtil.getMacAddress() ?: "")
            //当前设备的imei
            put("X-imei", DeviceUtil.getIMEI() ?: "")
        }
        headers.putAll(addExtraHeader())
        return headers
    }


    abstract fun getDefaultHost(): String

    open fun addExtraHeader(): MutableMap<String, String>{
        return mutableMapOf()
    }

    private fun isHttpUrl(url: String) = url.startWith("http://", "https://")
}