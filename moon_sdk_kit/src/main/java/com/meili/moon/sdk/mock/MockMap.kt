package com.meili.moon.sdk.mock

/**
 * 适用类型：map，否则无效
 *
 * 注解属性的取值范围
 *
 * 指定map的mock长度
 *
 * 当前还不支持其他形式的数据定制注解，如果有需求可以找naite.zhou一块讨论一下
 *
 * @param [size] 指定返回的map的长度
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockMap(val size: Int)