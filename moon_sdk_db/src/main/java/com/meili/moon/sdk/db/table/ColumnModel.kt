package com.meili.moon.sdk.db.table

import android.database.Cursor
import com.meili.moon.sdk.db.annotation.Column
import com.meili.moon.sdk.db.converter.ColumnConverter
import com.meili.moon.sdk.db.converter.getColumnConverter
import com.meili.moon.sdk.db.utils.findGetMethod
import com.meili.moon.sdk.db.utils.findSetMethod
import com.meili.moon.sdk.db.utils.isAutoIdType
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.util.isEmpty
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * 数据库列的模型类
 * Created by imuto on 18/1/3.
 */
class ColumnModel constructor(entityType: Class<*>, field: Field, column: Column?) {
    val name: String
    val property: String
    val isId: Boolean
    val isAutoId: Boolean

    private val getMethod: Method?
    private val setMethod: Method?
    private val columnField: Field
    internal val columnConverter: ColumnConverter<Any>

    init {
        field.isAccessible = true

        columnField = field
        name = if (column == null || isEmpty(column.value)) {
            field.name
        } else column.value

        property = column?.property ?: ""
        isId = column?.isId ?: false

        val fieldType = field.type
        if (isId && column!!.autoGen && !isAutoIdType(fieldType)) throw DbException(msg = "autoGen id type is error")
        isAutoId = isId && column!!.autoGen && isAutoIdType(fieldType)
        columnConverter = fieldType.getColumnConverter() as ColumnConverter<Any>

        getMethod = findGetMethod(entityType, field)
        getMethod?.isAccessible = true

        setMethod = findSetMethod(entityType, field)
        setMethod?.isAccessible = true
    }

    fun setValueFromCursor(entity: Any, cursor: Cursor, index: Int) {
        val value = columnConverter.getFieldValue(cursor, index) ?: return

        setFieldValue(entity, value)
    }

    fun getColumnValue(entity: Any): Any? {
        val fieldValue = getFieldValue(entity) ?: return null
        return if (isAutoId && (fieldValue == 0 || fieldValue == 0L)) {
            null
        } else {
            columnConverter.fieldValue2DbValue(fieldValue)
        }
    }

    fun setAutoIdValue(entity: Any, value: Long) {
//        var idValue: Long = value
//        if (ColumnUtils.isInteger(columnField.type)) {
//            idValue = value.toLong()
//        }
        setFieldValue(entity, value)
    }

    fun getFieldValue(entity: Any): Any? {
        return try {
            if (getMethod != null) {
                getMethod.invoke(entity)
            } else {
                columnField.get(entity)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setFieldValue(entity: Any, value: Any) {
        try {
            if (setMethod != null) {
                setMethod.invoke(entity, value)
            } else {
                columnField.set(entity, value)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}