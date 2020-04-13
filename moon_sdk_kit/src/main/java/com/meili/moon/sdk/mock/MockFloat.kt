package com.meili.moon.sdk.mock

/**
 * 适用类型：float，否则无效
 *
 * 注解属性的取值范围
 *
 * 使用指定的值作为mock的返回结果，优先级最高
 *
 * @see [MockFloatEnum]
 * @see [MockFloatRange]
 *
 * @param [value] 确定的返回结果
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockFloat(val value: Float)

/**
 * 适用类型：float，否则无效
 *
 * 注解属性的取值范围
 *
 * 如果[min]>[max] 则无效，无效的话会进行随机生成
 *
 * 优先级最低
 *
 * @see [MockFloat]
 * @see [MockFloatEnum]
 *
 * @param [min] 取值范围，最小值，包含此值
 * @param [max] 取值范围，最大值，包含此值，
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockFloatRange(val min: Float, val max: Float)

/**
 * 适用类型：float，否则无效
 *
 * 注解属性的取值列表
 *
 * mock结果将在取值列表中寻找，第二优先级
 *
 * @see [MockFloat]
 * @see [MockFloatRange]
 *
 * @param [enum] 确定的枚举列表，默认为空
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockFloatEnum(vararg val enum: Float)

