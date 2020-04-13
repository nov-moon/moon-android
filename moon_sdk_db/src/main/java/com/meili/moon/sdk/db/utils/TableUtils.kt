@file:JvmName("TableUtils")

package com.meili.moon.sdk.db.utils

import com.meili.moon.sdk.db.AbsDB
import com.meili.moon.sdk.db.IDB
import com.meili.moon.sdk.db.annotation.Column
import com.meili.moon.sdk.db.converter.isSupportColumnConverter
import com.meili.moon.sdk.db.sqlite.TableSelector
import com.meili.moon.sdk.db.table.ColumnModel
import com.meili.moon.sdk.db.table.TableModel
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.util.isEmpty
import com.meili.sdk.db.sql.ISelector
import java.lang.reflect.Modifier
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation

/**
 * table的工具类
 * Created by imuto on 18/1/3.
 */

/**初始化table的映射对象*/
internal fun KClass<*>.findColumnMap(propertyWithAnnotation: Boolean): LinkedHashMap<String, ColumnModel> {
    val columnMap = linkedMapOf<String, ColumnModel>()
    addColumns2Map(this, columnMap, propertyWithAnnotation)
    return columnMap
}

internal fun IDB.exist(name: String): Boolean {
    val cursor = query("SELECT COUNT(*) AS c FROM sqlite_master WHERE type='table' AND name='$name'")
    try {
        if (cursor.moveToNext()) {
            val count = cursor.getInt(0)
            if (count > 0) {
                return true
            }
        }
    } catch (e: Throwable) {
        throw DbException(cause = e)
    } finally {
        closeQuietly(cursor)
    }

    return false
}

fun <T : Any> select(tableModel: TableModel<T>, init_: (TableSelector<T>.() -> Unit)?): ISelector<T> {
    val selector = TableSelector(tableModel)
    if (init_ != null) {
        selector.init_()
    }
    return selector
}

internal fun <T : Any> IDB.getTable(tableClz: KClass<T>): TableModel<T> {
    val absDB = this as AbsDB
    synchronized(absDB.tableMap) {
        var tableModel = absDB.tableMap[tableClz]
        if (tableModel == null) {
            tableModel = TableModel(this, tableClz)
            absDB.tableMap[tableClz] = tableModel
        }
        return tableModel as TableModel<T>
    }
}

private fun addColumns2Map(entityType: KClass<*>, columnMap: HashMap<String, ColumnModel>, propertyWithAnnotation: Boolean) {

    if (Any::class == entityType) return

    try {
        val fields = entityType.java.declaredFields
        for (field in fields) {
            val modify = field.modifiers
            if (Modifier.isStatic(modify)
                    || Modifier.isTransient(modify)
                    || !field.type.isSupportColumnConverter()) {
                continue
            }
            val columnAnn = entityType.declaredMemberProperties.first { it.name == field.name }.findAnnotation<Column>()

            if (columnAnn == null && propertyWithAnnotation) {
                continue
            }
            if (columnAnn != null && columnAnn.ignore) {
                continue
            }
            val column = ColumnModel(entityType.java, field, columnAnn)
            if (!columnMap.containsKey(column.name)) {
                columnMap[column.name] = column
            }
        }

        if (isEmpty(entityType.allSuperclasses)) {
            return
        }
        addColumns2Map(entityType.allSuperclasses.first(), columnMap, propertyWithAnnotation)
    } catch (e: Throwable) {
        if (e is DbException) {
            throw e
        } else {
            e.printStackTrace()
        }
    }

}