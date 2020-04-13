package com.meili.moon.sdk.mock

/**
 * 适用类型：List，Array 否则无效
 * 注解属性的取值范围
 *
 * 指定list的mock长度
 *
 * 可以在此mock上使用其他mock注解进行list内的item注解。例如再添加一个@MockString("a")则生成的list中的所有item，都为a
 *
 * @param [size] 指定返回的list的长度
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockList(val size: Int)
