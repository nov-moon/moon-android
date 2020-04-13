package com.meili.moon.sdk.db

import com.meili.moon.sdk.db.table.TableModel
import com.meili.moon.sdk.db.utils.closeQuietly
import com.meili.moon.sdk.db.utils.getTable
import com.meili.moon.sdk.exception.DbException
import kotlin.reflect.KClass

/**
 * Created by imuto on 18/1/4.
 */
abstract class AbsDB : IDB {

    internal val tableMap = hashMapOf<KClass<*>, TableModel<*>>()

    override fun dropDB() {
        val cursor = query("SELECT name FROM sqlite_master WHERE type='table' AND name<>'sqlite_sequence'")
        try {
            while (cursor.moveToNext()) {
                try {
                    val tableName = cursor.getString(0)
                    execute("DROP TABLE $tableName")
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }

            synchronized(tableMap) {
                for (table in tableMap.values) {
                    table.checkedDatabase = false
                }
                tableMap.clear()
            }
        } catch (e: Throwable) {
            throw DbException(cause = e)
        } finally {
            closeQuietly(cursor)
        }
    }

    override fun dropTable(tableKClz: KClass<*>) {
        val table = getTable(tableKClz)
        if (!table.exist()) return
        execute("DROP TABLE '${table.name}'")
        table.checkedDatabase = false
        synchronized(tableMap) {
            tableMap.remove(tableKClz)
        }
    }

    override fun addColumn(tableKClz: KClass<*>, column: String) {
        val table = getTable(tableKClz)
        val columnModel = table.columnMap[column] ?: return
        val sql = "ALTER TABLE '${table.name}' ADD COLUMN '${columnModel.name}' ${columnModel.columnConverter.getColumnType()} ${columnModel.property}"
        execute(sql)
    }
}