package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.http.IRequestParams

/**
 * 默认的paramBuilder
 * Created by imuto on 17/11/29.
 */
class DefHttpParamsBuilder : SdkHttpParamsBuilder() {
    override fun buildParams(params: IRequestParams.IHttpRequestParams) = Unit

    override fun getDefaultHost() = ""
}