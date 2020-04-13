package com.meili.moon.mock.initializer

import com.meili.moon.sdk.mock.MockDate
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.toT
import java.util.*

/**
 * Created by imuto on 2018/12/22.
 */
internal object DateInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        val clazz = info.clazz ?: return false
        if (clazz == Long::class) {
            val valueAnnotation = info.annotations.findAnnotation<MockDate>()
            return valueAnnotation != null
        }
        return Date::class.java == clazz || Calendar::class.java == clazz
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val clazz = info.clazz ?: return null
        val defValue = System.currentTimeMillis()
        if (clazz == Long::class) {
            val valueAnnotation = info.annotations.findAnnotation<MockDate>()
            return (valueAnnotation?.value ?: defValue).toT()
        }

        //从1973年3月3号-2050年9月3号
        val resultValue = PrimitiveInitializer.randomLong(100000000000, 2545807933000) ?: defValue

        val result: Any? = when (clazz) {
            Date::class -> {
                Date(resultValue)
            }
            Calendar::class -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = resultValue
                calendar
            }
            else -> null
        }

        return result.toT()
    }

}