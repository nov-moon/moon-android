package com.meili.moon.sdk.db.table

import com.meili.moon.sdk.util.isEmpty
import java.util.*

/**
 * Created by imuto on 18/1/3.
 */
class DbModel {
    private val dataMap = hashMapOf<String, String>()

    fun getString(columnName: String): String? {
        return dataMap[columnName]
    }

    fun getInt(columnName: String): Int? {
        return dataMap[columnName]?.toInt()
    }

    fun getBoolean(columnName: String): Boolean {
        val value = dataMap[columnName]
        return if (isEmpty(value)) {
            false
        } else {
            if (value!!.length == 1) "1" == value else value.toBoolean()
        }
    }

    fun getDouble(columnName: String): Double? {
        return dataMap[columnName]?.toDouble()
    }

    fun getFloat(columnName: String): Float? = dataMap[columnName]?.toFloat()

    fun getLong(columnName: String): Long? = dataMap[columnName]?.toLong()

    fun getDate(columnName: String): Date? {
        val date = dataMap[columnName]?.toLong() ?: return null
        return Date(date)
    }

    fun getSqlDate(columnName: String): java.sql.Date? {
        val date = dataMap[columnName]?.toLong() ?: return null
        return java.sql.Date(date)
    }

    fun add(columnName: String, valueStr: String) {
        dataMap.put(columnName, valueStr)
    }

    /**
     * @return key: columnName
     */
    fun getDataMap(): HashMap<String, String> {
        return dataMap
    }
}