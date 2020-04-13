package com.meili.moon.sdk.db.converter

import android.database.Cursor
import com.meili.moon.sdk.db.sqlite.ColumnDbType

/**
 * Created by imuto on 18/1/3.
 */
class StringColumnConverter : ColumnConverter<String> {
    override fun getFieldValue(cursor: Cursor, index: Int): String? {
        return if (cursor.isNull(index)) null else cursor.getString(index)
    }

    override fun getColumnType(): ColumnDbType {
        return ColumnDbType.TEXT
    }
}