package com.meili.moon.sdk.http

/**
 * 标准请求方法枚举
 *
 * Created by imuto on 16/5/23.
 */
enum class HttpMethod private constructor(private val value: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    PATCH("PATCH"),
    HEAD("HEAD"),
    MOVE("MOVE"),
    COPY("COPY"),
    DELETE("DELETE"),
    OPTIONS("OPTIONS"),
    TRACE("TRACE"),
    CONNECT("CONNECT");

    override fun toString(): String {
        return this.value
    }

    companion object {

        /**是否支持重试*/
        fun permitsRetry(method: HttpMethod): Boolean {
            return method == GET
        }

        /**是否支持缓存*/
        fun permitsCache(method: HttpMethod): Boolean {
            return method == GET || method == POST
        }

        /**是否支持requestBody*/
        fun permitsRequestBody(method: HttpMethod): Boolean {
            return (method == POST
                    || method == PUT
                    || method == PATCH
                    || method == DELETE)
        }
    }
}
