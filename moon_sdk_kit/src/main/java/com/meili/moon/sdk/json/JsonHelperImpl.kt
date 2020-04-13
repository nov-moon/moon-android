package com.meili.moon.sdk.json

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.serializer.SerializerFeature
import com.meili.moon.sdk.util.isEmpty
import java.math.BigDecimal

/**
 * jsonHelper的实现类
 * Created by imuto on 17/11/23.
 */
object JsonHelperImpl : com.meili.moon.sdk.IJsonHelper {

    private val JSON_FILTER = mutableListOf<Class<*>>()

    init {
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(Int::class.java)
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(Long::class.java)
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(Double::class.java)
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(Float::class.java)
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(BigDecimal::class.java)
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.add(String::class.java)
    }

    override fun toJson(any: Any?): String = JSON.toJSONString(any, SerializerFeature.WriteMapNullValue)

    override fun <T> toObject(json: String?, clazz: Class<T>?): T? = when {
        clazz == null || clazz is JSONObject ->
            JSON.parseObject(json) as T
        com.meili.moon.sdk.json.JsonHelperImpl.JSON_FILTER.contains(clazz) ->
            com.meili.moon.sdk.json.JsonHelperImpl.parseForFilter(json, clazz)
        else ->
            JSON.parseObject(json, clazz)
    }

    override fun toJsonArray(json: String?): JSONArray? = JSON.parseArray(json)

    override fun isJsonArray(json: String?): Boolean {
        if (isEmpty(json)) {
            return false
        }
        return try {
            val jsonArray = com.meili.moon.sdk.json.JsonHelperImpl.toJsonArray(json)
            jsonArray != null && jsonArray.size > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun parseObject(json: String?): JSONObject {
        return JSON.parseObject(json)
    }


    override fun <T> toList(json: String?, clazz: Class<T>?): MutableList<T> = JSON.parseArray(json, clazz)

    private fun <T> parseForFilter(json: String?, clazz: Class<T>): T? = when {
        json == null ->
            null
        Int::class.java.isAssignableFrom(clazz) ->
            json.toInt() as T
        Long::class.java.isAssignableFrom(clazz) ->
            json.toLong() as T
        Double::class.java.isAssignableFrom(clazz) ->
            json.toDouble() as T
        Float::class.java.isAssignableFrom(clazz) ->
            json.toFloat() as T
        String::class.java.isAssignableFrom(clazz) ->
            json as T
        BigDecimal::class.java.isAssignableFrom(clazz) ->
            BigDecimal.valueOf(json.toDouble()) as T
        else ->
            null
    }
}