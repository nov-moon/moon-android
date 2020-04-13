package com.meili.moon.sdk.db

/**
 * groupBy字段，当前不支持聚合函数，后面会做优化
 * Created by imuto on 18/1/25.
 */
interface IGroupBy {
    fun having(init: IWhere.() -> Unit)
    fun build(): String
}