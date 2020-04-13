package com.meili.moon.sdk.db.converter

import android.database.Cursor
import java.sql.Date

/**
 * Created by imuto on 18/1/3.
 */
class SqlDateColumnConverter : AbsIntColumnConverter<Date>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Date? {
        return if (cursor.isNull(index)) null else java.sql.Date(cursor.getLong(index))
    }

    override fun fieldValue2DbValue(fieldValue: Date): Any {
        return fieldValue.time
    }
}