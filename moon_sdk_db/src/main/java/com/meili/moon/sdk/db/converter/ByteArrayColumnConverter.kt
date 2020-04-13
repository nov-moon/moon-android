package com.meili.moon.sdk.db.converter

import android.database.Cursor
import com.meili.moon.sdk.db.sqlite.ColumnDbType

/**
 * 字节数组转换器
 * Created by imuto on 17/12/27.
 */
class ByteArrayColumnConverter : ColumnConverter<ByteArray> {
    override fun getFieldValue(cursor: Cursor, index: Int): ByteArray? {
        return if (cursor.isNull(index)) null else cursor.getBlob(index)
    }

    override fun fieldValue2DbValue(fieldValue: ByteArray) = fieldValue

    override fun getColumnType() = ColumnDbType.BLOB
}