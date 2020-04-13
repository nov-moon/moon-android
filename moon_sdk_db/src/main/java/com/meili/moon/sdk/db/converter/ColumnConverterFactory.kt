@file:JvmName("ColumnConverterFactory")

package com.meili.moon.sdk.db.converter

import com.meili.moon.sdk.log.LogUtil
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**获取class对应的converter*/
internal fun Class<*>.getColumnConverter(): ColumnConverter<*> {
    var result: ColumnConverter<*>? = null
    if (MAP_TYPE_CONVERT.containsKey(name)) {
        result = MAP_TYPE_CONVERT[name]
    } else if (ColumnConverter::class.java.isAssignableFrom(this)) {
        try {
            val columnConverter = newInstance() as ColumnConverter<*>
            MAP_TYPE_CONVERT[name] = columnConverter
            result = columnConverter
        } catch (ex: Throwable) {
            LogUtil.e(ex, ex.message)
        }
    }

    if (result == null) {
        throw RuntimeException("Database Column Not Support: " + name +
                ", please impl ColumnConverter or use ColumnConverterFactory#registerColumnConverter(...)")
    }

    return result
}

/**注册convert到db的sdk中*/
fun registerColumnConverter(columnType: Class<*>, columnConverter: ColumnConverter<*>) {
    MAP_TYPE_CONVERT.put(columnType.name, columnConverter)
}

/**当前class是否是有支持的convert*/
internal fun Class<*>.isSupportColumnConverter(): Boolean {
    if (MAP_TYPE_CONVERT.containsKey(name)) {
        return true
    } else if (ColumnConverter::class.java.isAssignableFrom(this)) {
        try {
            val columnConverter = newInstance() as ColumnConverter<*>
            MAP_TYPE_CONVERT[name] = columnConverter
            return true
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }
    return false
}

/**
 * 以实际的type为key，转换器为value的map
 */
private val MAP_TYPE_CONVERT: ConcurrentHashMap<String, ColumnConverter<*>> by lazy {
    val map: ConcurrentHashMap<String, ColumnConverter<*>> = ConcurrentHashMap()
    map[Boolean::class.javaObjectType.name] = BooleanColumnConverter()
    map["boolean"] = BooleanColumnConverter()
    map[Byte::class.javaObjectType.name] = ByteColumnConverter()
    map["byte"] = ByteColumnConverter()
    map[ByteArray::class.java.name] = ByteArrayColumnConverter()
    map[Char::class.javaObjectType.name] = CharColumnConverter()
    map["char"] = CharColumnConverter()
    map[Date::class.java.name] = DateColumnConverter()
    map[Double::class.javaObjectType.name] = DoubleColumnConverter()
    map["double"] = DoubleColumnConverter()
    map[Float::class.javaObjectType.name] = FloatColumnConverter()
    map["float"] = FloatColumnConverter()
    map[Integer::class.javaObjectType.name] = IntegerColumnConverter()
    map["int"] = IntegerColumnConverter()
    map[Long::class.javaObjectType.name] = LongColumnConverter()
    map["long"] = LongColumnConverter()
    map[Short::class.javaObjectType.name] = ShortColumnConverter()
    map["short"] = ShortColumnConverter()
    map[java.sql.Date::class.java.name] = SqlDateColumnConverter()
    map[String::class.java.name] = StringColumnConverter()
    map
}

//private fun initConvert() {
//    if (!isEmpty(MAP_TYPE_CONVERT)) {
//        return
//    }
//    MAP_TYPE_CONVERT[Boolean::class.java.name] = BooleanColumnConverter()
//    MAP_TYPE_CONVERT[Byte::class.java.name] = ByteColumnConverter()
//    MAP_TYPE_CONVERT[ByteArray::class.java.name] = ByteArrayColumnConverter()
//    MAP_TYPE_CONVERT[Char::class.java.name] = CharColumnConverter()
//    MAP_TYPE_CONVERT[Date::class.java.name] = DateColumnConverter()
//    MAP_TYPE_CONVERT[Double::class.java.name] = DoubleColumnConverter()
//    MAP_TYPE_CONVERT[Float::class.java.name] = FloatColumnConverter()
//    MAP_TYPE_CONVERT[Integer::class.java.name] = IntegerColumnConverter()
//    MAP_TYPE_CONVERT[Long::class.java.name] = LongColumnConverter()
//    MAP_TYPE_CONVERT[Short::class.java.name] = ShortColumnConverter()
//    MAP_TYPE_CONVERT[java.sql.Date::class.java.name] = SqlDateColumnConverter()
//    MAP_TYPE_CONVERT[String::class.java.name] = StringColumnConverter()
//}