package com.meili.moon.sdk.db.converter

import android.database.Cursor

/**
 * 字节类型转换器
 * Created by imuto on 17/12/27.
 */
class ByteColumnConverter : AbsIntColumnConverter<Byte>() {

    override fun getFieldValue(cursor: Cursor, index: Int) = if (cursor.isNull(index)) null else cursor.getInt(index).toByte()

    override fun fieldValue2DbValue(fieldValue: Byte) = fieldValue
}