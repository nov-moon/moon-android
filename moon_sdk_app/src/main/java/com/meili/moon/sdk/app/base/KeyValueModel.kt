package com.meili.moon.sdk.app.base

import com.meili.moon.sdk.http.common.BaseModel

/**
 * key value 格式的数据标准model
 * Created by imuto on 2018/7/9.
 */
data class KeyValueModel(
        val name: String,
        val value: String,
        var isSelected: Boolean = false,
        var tag: Any? = null,
        val obj: String ?= null
) : BaseModel(), IKeyValueModel {

    override var isKeyValueSelected: Boolean = false

    override fun getKeyValueName(): String = name

    override fun getKeyValueId(): String = value

    override fun getKeyValueTag(): Any? = tag

    override fun getKeyValueObj(): String? = obj
}

/**KeyValueModel的基本结构*/
interface IKeyValueModel {
    var isKeyValueSelected: Boolean

    /**获取用来展示的名称*/
    fun getKeyValueName(): String

    /**获取用来标记的Id*/
    fun getKeyValueId(): String = ""

    fun getKeyValueTag(): Any? = null

    fun getKeyValueObj(): String? = null
}