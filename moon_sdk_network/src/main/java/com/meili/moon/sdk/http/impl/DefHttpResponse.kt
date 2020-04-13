package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IResponseParser


/**
 * 默认的response
 * Created by imuto on 17/12/5.
 */
class DefHttpResponse(override var requestParams: IRequestParams.IHttpRequestParams) : SdkHttpResponse() {
    override var httpState: Int = 0
    override var parser: IResponseParser = DefHttpRespParser()
}