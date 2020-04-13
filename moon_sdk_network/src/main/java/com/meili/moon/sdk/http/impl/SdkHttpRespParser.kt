package com.meili.moon.sdk.http.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.http.IResponseParser
import com.meili.moon.sdk.json.JsonHelperImpl
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.isEmpty

/**
 * response解析器的基类
 * Created by imuto on 17/11/29.
 */
abstract class SdkHttpRespParser : IResponseParser {

    override fun <DataType> parse(response: IHttpResponse, dataType: Class<DataType>): List<DataType>? {
        val result = response.response
        try {
            LogUtil.json(result)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 让对象去自己解析
        val handler = response.handleParseData(result)
        if (handler) {
            return null
        }

        val json = JSON.parseObject(result)

        parseCommonData(json, response)

//        checkCode(response) // 判断系统参数
//        TODO("这里需要实现判断系统参数的逻辑")

        var dataSet = mutableListOf<DataType>()

        if (String::class.java.isAssignableFrom(dataType) || Any::class.java == dataType) {
            dataSet.add(response.data as DataType)
            return dataSet
        }

        var isArray = true
        var dataJson: String? = null
        try {
            val jsonArray = JSON.parseArray(response.data) // 拿出data
            dataJson = jsonArray.toJSONString()
        } catch (e: Exception) {
            isArray = false
            dataJson = response.data
        }
        if (isEmpty(dataJson)) {
            return dataSet
        }

        if (isArray) {
            if (dataType.isArray) {
//                dataSet = JsonHelperImpl.toList(dataJson, dataType.java.componentType as List<DataType>)
                TODO("这里需要实现解析数组的逻辑")
            } else {
                dataSet = JsonHelperImpl.toList(dataJson, dataType)
            }
        } else {
            val data = JsonHelperImpl.toObject(dataJson, dataType)
            if (data != null) {
                dataSet.add(data)
            }
        }
        return dataSet
    }

    abstract fun parseCommonData(json: JSONObject, response: IHttpResponse)
}