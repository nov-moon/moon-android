package com.meili.moon.mock.initializer

import com.meili.moon.sdk.mock.*
import com.meili.moon.sdk.util.isPrimitive
import com.meili.moon.sdk.util.toT
import java.math.BigDecimal
import java.math.BigInteger

/**
 * 基本类型初始化器
 *
 * Created by imuto on 2018/12/22.
 */
internal object PrimitiveInitializer : AbsInitializer() {

    private val blackList = listOf(
            BigInteger::class,
            BigDecimal::class
    )

    override fun isMatch(info: InitializerInfo): Boolean {
        return info.clazz.isPrimitive || blackList.contains(info.clazz)
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val clazz = info.clazz ?: return null
        val annotations = info.annotations ?: return null
        //下面应该是符合规定范围内的随机数展示，而不是固定的值
        val result = when (clazz) {
            Boolean::class -> {
                val booleanAnnotation = annotations.findAnnotation<MockBoolean>()
                booleanAnnotation?.value ?: mRandom.nextBoolean()
            }
            Char::class -> {
                initValue<MockChar, MockCharEnum, MockCharRange, Char>(
                        annotations, 'a', { it.value }, { it?.enum?.toTypedArray() }, {
                    val max = it.max
                    val min = it.min
                    if (max > min) {
                        val count = max - min
                        (min + (mRandom.nextFloat() * count).toInt())
                    } else null
                })
            }
            Byte::class -> {
                initValue<MockByte, MockByteEnum, MockByteRange, Byte>(
                        annotations, 1, { it.value }, { it?.enum?.toTypedArray() }, {
                    val max = it.max
                    val min = it.min
                    if (max > min) {
                        val count = max - min
                        (min + (mRandom.nextFloat() * count)).toByte()
                    } else null
                })
            }
            Short::class -> {
                initValue<MockShort, MockShortEnum, MockShortRange, Short>(
                        annotations, 1, { it.value }, { it?.enum?.toTypedArray() }, {
                    val max = it.max
                    val min = it.min
                    if (max > min) {
                        val count = max - min
                        (min + (mRandom.nextFloat() * count)).toShort()
                    } else null
                })
            }
            Int::class -> {
                initValue<MockInt, MockIntEnum, MockIntRange, Int>(
                        annotations, 1, { it.value }, { it?.enum?.toTypedArray() }, {
                    val max = it.max
                    val min = it.min
                    if (max > min) {
                        val count = max - min
                        min + (mRandom.nextFloat() * count).toInt()
                    } else null
                })
            }
            Float::class -> {
                initValue<MockFloat, MockFloatEnum, MockFloatRange, Float>(
                        annotations, 1.0F, { it.value }, { it?.enum?.toTypedArray() }, {
                    val max = it.max
                    val min = it.min
                    if (max > min) {
                        val count = max - min
                        (min + mRandom.nextFloat() * count)
                    } else null
                })
            }
            Long::class -> {
                initValue<MockLong, MockLongEnum, MockLongRange, Long>(
                        annotations, 1L, { it.value }, { it?.enum?.toTypedArray() }, {
                    randomLong(it.min, it.max)
                })
            }
            Double::class -> {
                initValue<MockDouble, MockDoubleEnum, MockDoubleRange, Double>(
                        annotations, 1.0, { it.value }, { it?.enum?.toTypedArray() }, {
                    randomDouble(it.min, it.max)
                })
            }
            else -> null
        }

        return result.toT()
    }

    private inline fun <reified T : Annotation, reified T2 : Annotation, reified T3 : Annotation, R>
            initValue(annotation: List<Annotation>,
                      defValue: R,
                      valueLambda: (T) -> R,
                      enumLambda: (T2?) -> Array<R>?,
                      rangeLambda: (T3) -> R?): R {
        val result: R
        val valueAnnotation = annotation.findAnnotation<T>()
        result = if (valueAnnotation != null) {
            valueLambda(valueAnnotation)
        } else {
            val enumAnnotation = annotation.findAnnotation<T2>()
            val enum = enumLambda(enumAnnotation)
            if (enum != null && enum.isNotEmpty()) {
                val index = (mRandom.nextFloat() * enum.size).toInt()
                enum[index]
            } else {
                val rangeAnnotation = annotation.findAnnotation<T3>()
                if (rangeAnnotation != null) {
                    rangeLambda(rangeAnnotation) ?: defValue
                } else {
                    defValue
                }
            }
        }

        return result
    }

    internal fun randomLong(min: Long, max: Long): Long? {
        return if (max > min) {
            val count = max - min
            min + (mRandom.nextFloat() * count).toLong()
        } else null
    }

    internal fun randomDouble(min: Double, max: Double): Double? {
        return if (max > min) {
            val count = max - min
            min + (mRandom.nextFloat() * count)
        } else null
    }

}