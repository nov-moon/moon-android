package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * Created by imuto on 18/1/3.
 */
class LongColumnConverter : AbsIntColumnConverter<Long>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Long? {
        return if (cursor.isNull(index)) null else cursor.getLong(index)
    }
}