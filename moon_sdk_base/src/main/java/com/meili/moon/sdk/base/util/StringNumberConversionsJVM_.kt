@file:kotlin.jvm.JvmName("StringsNumberUtils")

package com.meili.moon.sdk.base.util

/**
 * Created by imuto on 2018/11/26.
 */

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toBoolean(defValue: Boolean? = null): Boolean? = _filterValue(this, defValue, java.lang.Boolean::parseBoolean)

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toByte(defValue: Byte? = null): Byte? = _filterValue(this, defValue, java.lang.Byte::parseByte)

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toShort(defValue: Short? = null): Short? = _filterValue(this, defValue, java.lang.Short::parseShort)

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toLong(defValue: Long? = null): Long? = _filterValue(this, defValue, java.lang.Long::parseLong)

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toFloat(defValue: Float? = null): Float? = _filterValue(this, defValue, java.lang.Float::parseFloat)

/**将String转换为对应类型的值，如果失败，则使用[defValue]作为默认值返回*/
inline fun String?.toDouble(defValue: Double? = null): Double? = _filterValue(this, defValue, java.lang.Double::parseDouble)

inline fun <T> _filterValue(str: String?, def: T?, parse: (String) -> T): T? {
    str ?: return def
    return try {
        parse(str)
    } catch (e: Throwable) {  // overflow
        def
    }
}