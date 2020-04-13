package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * boolean类型的类型转换器
 * Created by imuto on 17/12/27.
 */
class BooleanColumnConverter : AbsIntColumnConverter<Boolean>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Boolean {
        return if (cursor.isNull(index)) false else cursor.getInt(index) == 1
    }

    override fun fieldValue2DbValue(fieldValue: Boolean) = if (fieldValue) 1 else 0
}