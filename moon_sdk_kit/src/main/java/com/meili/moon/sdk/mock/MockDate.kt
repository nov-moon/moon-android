package com.meili.moon.sdk.mock

/**
 * 适用类型：date，否则无效
 *
 * 注解属性的取值范围
 *
 * 使用指定的值作为mock的返回结果，优先级最高
 * 如果没有指定，则使用当前时间
 *
 * @param [value] 确定的返回结果
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockDate(val value: Long = 0L)

