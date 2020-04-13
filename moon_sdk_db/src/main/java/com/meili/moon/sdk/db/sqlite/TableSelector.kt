package com.meili.moon.sdk.db.sqlite

import android.database.Cursor
import com.meili.moon.sdk.db.table.TableModel
import com.meili.moon.sdk.db.utils.getEntity

/**
 * Table的查询功能类
 * Created by imuto on 18/1/25.
 */
class TableSelector<T : Any>(private val tableModel: TableModel<T>)
    : Selector<T>(tableModel.db,
        tableModel.name,
        tableModel.id?.name ?: "_id",
        { tableModel.exist() }) {

    override fun findFirst(converter: ((Cursor) -> T)?): T? {
        return if (converter == null) {
            super.findFirst {
                tableModel.getEntity(it)
            }
        } else {
            super.findFirst(converter)
        }
    }

    override fun findAll(converter: ((Cursor) -> T)?): List<T> {
        return if (converter == null) {
            super.findAll {
                tableModel.getEntity(it)
            }
        } else {
            super.findAll(converter)
        }
    }

}
