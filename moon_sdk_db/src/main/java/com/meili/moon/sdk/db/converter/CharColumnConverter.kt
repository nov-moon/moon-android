package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * Created by imuto on 17/12/27.
 */
class CharColumnConverter : AbsIntColumnConverter<Char>() {
    override fun getFieldValue(cursor: Cursor, index: Int) = if (cursor.isNull(index)) null else cursor.getInt(index).toChar()

    override fun fieldValue2DbValue(fieldValue: Char): Any = fieldValue.toInt()
}