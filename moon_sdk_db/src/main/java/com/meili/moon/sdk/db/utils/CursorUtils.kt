@file:JvmName("CursorUtils")

package com.meili.moon.sdk.db.utils

import android.database.Cursor
import com.meili.moon.sdk.db.table.DbModel
import com.meili.moon.sdk.db.table.TableModel

/**
 * Created by imuto on 18/1/4.
 */


/**关闭一个可关闭的对象*/
fun closeQuietly(closeable: Cursor?) {
    if (closeable == null) {
        return
    }
    try {
        closeable.close()
    } catch (ignored: Throwable) {
        ignored.printStackTrace()
    }
}

@Throws(Throwable::class)
internal fun <T: Any> TableModel<T>.getEntity(cursor: Cursor): T {
    val entity = createEntity()
    val columnMap = columnMap
    val columnCount = cursor.columnCount
    for (i in 0 until columnCount) {
        val columnName = cursor.getColumnName(i)
        columnMap[columnName]?.setValueFromCursor(entity as Any, cursor, i)
    }
    return entity
}

internal fun getDbModel(cursor: Cursor): DbModel {
    val result = DbModel()
    val columnCount = cursor.columnCount
    for (i in 0 until columnCount) {
        result.add(cursor.getColumnName(i), cursor.getString(i))
    }
    return result
}