package com.meili.moon.sdk.db.annotation

/**
 * 数据库的列属性注解
 * Created by imuto on 17/12/27.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class Column(
        /**列名称，默认使用字段名称*/
        val value: String = "",
        /**列属性，默认没有属性*/
        val property: String = "",
        /**当前字段是否是id，默认不是id*/
        val isId: Boolean = false,
        /**当前字段是否自增长，默认自增长*/
        val autoGen: Boolean = false,
        /**是否忽略当前属性*/
        val ignore: Boolean = false
)