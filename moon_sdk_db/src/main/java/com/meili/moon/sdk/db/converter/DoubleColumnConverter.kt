package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * double类型转换器
 * Created by imuto on 17/12/27.
 */
class DoubleColumnConverter: AbsRealColumnConverter<Double>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Double? {
        return if (cursor.isNull(index)) null else cursor.getDouble(index)
    }

    override fun fieldValue2DbValue(fieldValue: Double) = fieldValue
}