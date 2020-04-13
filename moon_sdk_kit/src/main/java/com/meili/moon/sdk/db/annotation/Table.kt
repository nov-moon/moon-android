package com.meili.moon.sdk.db.annotation

/**
 * 表的注解
 * Created by imuto on 17/12/27.
 */
@Target(AnnotationTarget.CLASS)
annotation class Table(
        /**table名称，默认使用类名*/
        val value: String = "",
        /**创建完表后执行的操作*/
        val onCreated: String = "",
        /**设置table中的property是否必须有注解才认为是数据库字段*/
        val propertyWithAnnotation: Boolean = false
)