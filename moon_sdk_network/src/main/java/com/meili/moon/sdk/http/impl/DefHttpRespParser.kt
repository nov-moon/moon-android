package com.meili.moon.sdk.http.impl

import com.alibaba.fastjson.JSONObject
import com.meili.moon.sdk.http.IHttpResponse

/**
 * 默认的response解析器
 * Created by imuto on 17/12/5.
 */
class DefHttpRespParser : SdkHttpRespParser() {
    override fun parseCommonData(json: JSONObject, response: IHttpResponse) {
        response.state = json.getInteger("status")!!
        response.message = json.getString("msg")
        response.data = json.getString("data")
    }
}