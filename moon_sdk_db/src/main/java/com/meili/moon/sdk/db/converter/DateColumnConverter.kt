package com.meili.moon.sdk.db.converter

import android.database.Cursor
import java.util.*

/**
 * 日期类型转换器
 * Created by imuto on 17/12/27.
 */
class DateColumnConverter : AbsIntColumnConverter<Date>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Date? {
        return if (cursor.isNull(index)) null else Date(cursor.getLong(index))
    }

    override fun fieldValue2DbValue(fieldValue: Date) = fieldValue.time
}