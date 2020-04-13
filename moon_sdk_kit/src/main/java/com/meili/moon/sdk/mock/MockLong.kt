package com.meili.moon.sdk.mock

/**
 * 适用类型：long，否则无效
 *
 * 注解属性的取值范围
 *
 * 使用指定的值作为mock的返回结果，优先级最高
 *
 * @see [MockLongRange]
 * @see [MockLongEnum]
 *
 * @param [value] 确定的返回结果
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockLong(val value: Long)

/**
 * 适用类型：long，否则无效
 *
 * 注解属性的取值范围
 *
 * 如果[min]>[max] 则无效，无效的话会进行随机生成
 *
 * 优先级最低
 *
 * @see [MockLong]
 * @see [MockLongEnum]
 *
 * @param [min] 取值范围，最小值，包含此值
 * @param [max] 取值范围，最大值，包含此值，
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockLongRange(val min: Long, val max: Long = Long.MAX_VALUE)

/**
 * 适用类型：long，否则无效
 *
 * 注解属性的取值列表
 *
 * mock结果将在取值列表中寻找，第二优先级
 *
 * @see [MockLong]
 * @see [MockLongRange]
 *
 * @param [enum] 确定的枚举列表，默认为空
 *
 * Created by imuto on 2018/12/22.
 */
@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY)
annotation class MockLongEnum(vararg val enum: Long)

