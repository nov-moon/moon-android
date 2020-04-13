@file:JvmName("ColumnUtils")

package com.meili.moon.sdk.db.utils

import com.meili.moon.sdk.db.converter.ColumnConverter
import com.meili.moon.sdk.db.converter.getColumnConverter
import com.meili.moon.sdk.log.LogUtil
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * Created by imuto on 18/1/4.
 */
private val BOOLEAN_TYPES = hashSetOf<String>(Boolean::class.javaObjectType.name, "boolean")
private val INTEGER_TYPES = hashSetOf<String>(Int::class.javaObjectType.name, "int")
private val AUTO_INCREMENT_TYPES: HashSet<String> by lazy {
    val hashSetOf = hashSetOf<String>(Long::class.javaObjectType.name, "long")
    hashSetOf.addAll(INTEGER_TYPES)
    hashSetOf
}

internal fun isAutoIdType(fieldType: Class<*>): Boolean {
    return AUTO_INCREMENT_TYPES.contains(fieldType.name)
}

internal fun isInteger(fieldType: Class<*>): Boolean {
    return INTEGER_TYPES.contains(fieldType.name)
}

internal fun isBoolean(fieldType: Class<*>): Boolean {
    return BOOLEAN_TYPES.contains(fieldType.name)
}

internal fun convert2DbValueIfNeeded(value: Any?): Any? {
    if (value == null) return null
    val columnConverter = value.javaClass.getColumnConverter()
    return (columnConverter as ColumnConverter<Any>).fieldValue2DbValue(value)
}

internal fun findGetMethod(entityType: Class<*>, field: Field): Method? {
    if (Any::javaClass == entityType) return null

    var method: Method? = null
    if (isBoolean(field.type)) {
        method = findBooleanGetMethod(entityType, field.name)
    }
    if (method == null) {
        val methodName = "get" + field.name.substring(0, 1).toUpperCase() + field.name.substring(1)
        try {
            method = entityType.getDeclaredMethod(methodName)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return if (method == null) {
        return findGetMethod(entityType.superclass, field)
    } else method
}

internal fun findSetMethod(entityType: Class<*>, field: Field): Method? {
    if (Any::class.java == entityType) return null
    var method: Method? = null
    if (isBoolean(field.type)) {
        method = findBooleanSetMethod(entityType, field.name, field.type)
    }
    if (method == null) {
        val methodName = "set" + field.name.substring(0, 1).toUpperCase() + field.name.substring(1)
        try {
            method = entityType.getDeclaredMethod(methodName, field.type)
        } catch (e: NoSuchMethodException) {
            LogUtil.d(entityType.name + "#" + methodName + " not exist")
        }
    }

    return if (method == null) {
        findSetMethod(entityType.superclass, field)
    } else method
}

private fun findBooleanGetMethod(entityType: Class<*>, fieldName: String): Method? {
    val methodName = if (fieldName.startsWith("is")) {
        fieldName
    } else {
        "is" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
    }
    return try {
        entityType.getDeclaredMethod(methodName)
    } catch (e: NoSuchMethodException) {
        LogUtil.d(entityType.name + "#" + methodName + " not exist")
        null
    }
}


private fun findBooleanSetMethod(entityType: Class<*>, fieldName: String, fieldType: Class<*>): Method? {
    val methodName = if (fieldName.startsWith("is")) {
        "set" + fieldName.substring(2, 3).toUpperCase() + fieldName.substring(3)
    } else {
        "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
    }
    return try {
        entityType.getDeclaredMethod(methodName, fieldType)
    } catch (e: NoSuchMethodException) {
        LogUtil.d(entityType.name + "#" + methodName + " not exist")
        null
    }
}
