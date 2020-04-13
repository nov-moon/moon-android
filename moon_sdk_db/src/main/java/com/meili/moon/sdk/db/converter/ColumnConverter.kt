package com.meili.moon.sdk.db.converter

import android.database.Cursor
import com.meili.moon.sdk.db.sqlite.ColumnDbType

/**
 * 数据库列的类型转换器
 * Created by imuto on 17/12/27.
 */
interface ColumnConverter<PropertyType> {
    /**将cursor中指定字段，转换为给定的类型*/
    fun getFieldValue(cursor: Cursor, index: Int): PropertyType?

    /**将java类型转换为数据库类型*/
    fun fieldValue2DbValue(fieldValue: PropertyType): Any = fieldValue as Any

    /**获取当前转换器支持的数据库类型*/
    fun getColumnType(): ColumnDbType
}