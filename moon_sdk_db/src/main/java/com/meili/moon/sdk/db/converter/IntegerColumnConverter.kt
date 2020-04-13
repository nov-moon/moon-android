package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 *
 * Created by imuto on 17/12/27.
 */
class IntegerColumnConverter : AbsIntColumnConverter<Int>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Int? {
        return if (cursor.isNull(index)) null else cursor.getInt(index)
    }
}