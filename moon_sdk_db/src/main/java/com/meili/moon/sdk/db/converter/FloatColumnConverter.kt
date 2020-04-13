package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * float类型转换器
 * Created by imuto on 17/12/27.
 */
class FloatColumnConverter : AbsRealColumnConverter<Float>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Float? {
        return if (cursor.isNull(index)) null else cursor.getFloat(index)
    }
}