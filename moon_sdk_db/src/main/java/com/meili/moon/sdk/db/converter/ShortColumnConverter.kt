package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * Created by imuto on 18/1/3.
 */
class ShortColumnConverter : AbsIntColumnConverter<Short>() {
    override fun getFieldValue(cursor: Cursor, index: Int): Short? {
        return if (cursor.isNull(index)) null else cursor.getShort(index)
    }
}