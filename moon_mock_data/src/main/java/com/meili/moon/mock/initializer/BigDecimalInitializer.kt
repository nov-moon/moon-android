package com.meili.moon.mock.initializer

import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.toT
import java.math.BigDecimal
import java.math.BigInteger

/**
 * Created by imuto on 2018/12/22.
 */
internal object BigDecimalInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        return info.clazz == BigDecimal::class || info.clazz == BigInteger::class
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val result: Any? = when (info.clazz) {
            BigInteger::class -> {
                val randomLong = PrimitiveInitializer.randomLong(100_000_000_000, 9_000_000_000_000)
                BigInteger("$randomLong")
            }
            BigDecimal::class -> {
                val randomDouble = PrimitiveInitializer.randomDouble(0.0, 10_000_000.0) ?: 1.0
                BigDecimal(randomDouble)
            }
            else -> null
        }

        return result.toT()
    }
}