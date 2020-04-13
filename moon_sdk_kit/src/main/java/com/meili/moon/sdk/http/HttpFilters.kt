package com.meili.moon.sdk.http

/**
 *
 * Created by imuto on 2019/1/9.
 */
interface IResponseInterceptor {
    fun intercept(resp: IResponse)
}

interface IRequestFilter {
    fun filter(param: IRequestParams, isBeforeRequest: Boolean)
}