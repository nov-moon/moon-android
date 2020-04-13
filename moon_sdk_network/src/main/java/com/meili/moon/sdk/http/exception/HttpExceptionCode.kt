package com.meili.moon.sdk.http.exception

import com.meili.moon.sdk.http.exception.HttpExceptionCode.CONNECTION_ERROR_NO_ROUTE_TO_HOST
import com.meili.moon.sdk.http.exception.HttpExceptionCode.CONNECTION_ERROR_PORT_UNREACHABLE
import com.meili.moon.sdk.http.exception.HttpExceptionCode.CONNECTION_ERROR_SOCKET_TIMEOUT
import com.meili.moon.sdk.http.exception.HttpExceptionCode.CONNECTION_ERROR_UNKNOWN_HOST
import com.meili.moon.sdk.http.exception.HttpExceptionCode.OTHER_ERROR_FILE_NOT_FOUND
import com.meili.moon.sdk.http.exception.HttpExceptionCode.OTHER_ERROR_ILLEGAL_ARGUMENT
import com.meili.moon.sdk.http.exception.HttpExceptionCode.OTHER_ERROR_JSON
import com.meili.moon.sdk.http.exception.HttpExceptionCode.OTHER_ERROR_NULL_POINTER
import com.meili.moon.sdk.http.exception.HttpExceptionCode.URL_ERROR_MALFORMED_URL
import com.meili.moon.sdk.http.exception.HttpExceptionCode.URL_ERROR_PROTOCOL
import com.meili.moon.sdk.http.exception.HttpExceptionCode.URL_ERROR_URI_SYNTAX
import org.json.JSONException
import java.io.FileNotFoundException
import java.net.*
import kotlin.reflect.KClass

/**
 * Created by imuto on 2018/12/5.
 */
object HttpExceptionCode {
    /**链接错误：NoRouteToHostException*/
    val CONNECTION_ERROR_NO_ROUTE_TO_HOST = 1001
    /**链接错误：PortUnreachableException*/
    val CONNECTION_ERROR_PORT_UNREACHABLE = 1002
    /**链接错误：SocketTimeoutException*/
    val CONNECTION_ERROR_SOCKET_TIMEOUT = 1003
    /**链接错误：UnknownHostException*/
    val CONNECTION_ERROR_UNKNOWN_HOST = 1004
    /**URL错误：MalformedURLException*/
    val URL_ERROR_MALFORMED_URL = 2001
    /**URL错误：URISyntaxException*/
    val URL_ERROR_URI_SYNTAX = 2002
    /**URL错误：ProtocolException*/
    val URL_ERROR_PROTOCOL = 2002
    /**MOCK数据错误：ProtocolException*/
    val MOCK_ERROR = 3002
    /**未知错误：IllegalArgumentException*/
    val OTHER_ERROR_ILLEGAL_ARGUMENT = 9001
    /**未知错误：NullPointerException*/
    val OTHER_ERROR_NULL_POINTER = 9002
    /**未知错误：FileNotFoundException*/
    val OTHER_ERROR_FILE_NOT_FOUND = 9003
    /**未知错误：JSONException*/
    val OTHER_ERROR_JSON = 9004
}

const val DEF_COMMON_ERROR_TIP = "请求失败，请稍后重试"
const val DEF_COMMON_ERROR_CODE = 9901

internal val EXCEPTION_PROVIDER_MAP: Map<KClass<*>, Pair<Int, String>> by lazy {
    val map = mutableMapOf<KClass<*>, Pair<Int, String>>()
    map[NoRouteToHostException::class] = Pair(CONNECTION_ERROR_NO_ROUTE_TO_HOST, DEF_COMMON_ERROR_TIP)
    map[PortUnreachableException::class] = Pair(CONNECTION_ERROR_PORT_UNREACHABLE, DEF_COMMON_ERROR_TIP)
    map[SocketTimeoutException::class] = Pair(CONNECTION_ERROR_SOCKET_TIMEOUT, DEF_COMMON_ERROR_TIP)
    map[UnknownHostException::class] = Pair(CONNECTION_ERROR_UNKNOWN_HOST, DEF_COMMON_ERROR_TIP)
    map[MalformedURLException::class] = Pair(URL_ERROR_MALFORMED_URL, DEF_COMMON_ERROR_TIP)
    map[URISyntaxException::class] = Pair(URL_ERROR_URI_SYNTAX, DEF_COMMON_ERROR_TIP)
    map[ProtocolException::class] = Pair(URL_ERROR_PROTOCOL, DEF_COMMON_ERROR_TIP)
    map[IllegalArgumentException::class] = Pair(OTHER_ERROR_ILLEGAL_ARGUMENT, DEF_COMMON_ERROR_TIP)
    map[NullPointerException::class] = Pair(OTHER_ERROR_NULL_POINTER, DEF_COMMON_ERROR_TIP)
    map[FileNotFoundException::class] = Pair(OTHER_ERROR_FILE_NOT_FOUND, DEF_COMMON_ERROR_TIP)
    map[JSONException::class] = Pair(OTHER_ERROR_JSON, DEF_COMMON_ERROR_TIP)
    return@lazy map
}