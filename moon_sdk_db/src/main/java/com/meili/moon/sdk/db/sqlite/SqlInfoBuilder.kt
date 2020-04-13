@file:JvmName("SqlInfoBuilder")

package com.meili.moon.sdk.db.sqlite

import com.meili.moon.sdk.db.ISqlInfo
import com.meili.moon.sdk.db.IWhere
import com.meili.moon.sdk.db.MoonDB
import com.meili.moon.sdk.db.table.TableModel
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.isEmpty
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * TableModel的扩展，构建 **增、删、改** 语句
 * Created by imuto on 18/1/5.
 */

private val SQL_CACHE_INSERT = ConcurrentHashMap<TableModel<*>, String>()
private val SQL_CACHE_REPLACE = ConcurrentHashMap<TableModel<*>, String>()

internal fun String.log() {
    if (!MoonDB.logable) return
    LogUtil.d("MoonDb", this)
}

internal fun SqlInfo.log() {
    if (!MoonDB.logable) return
    LogUtil.d("MoonDb", this.sql)
}

/**构建插入语句*/
internal fun TableModel<*>.buildInsertSqlInfo(entity: Any): SqlInfo? {
    val entityPairs = entity2PairList(this, entity) ?: return null

    val result = SqlInfo()
    var sql = SQL_CACHE_INSERT[this]
    if (isEmpty(sql)) {
        val builder = StringBuilder()
        builder.append("INSERT INTO '$name' (")
        for (kv in entityPairs) {
            builder.append("'${kv.first}',")
        }
        builder.deleteCharAt(builder.length - 1)
        builder.append(") VALUES (")

        for (i in entityPairs) {
            builder.append("?,")
        }
        builder.deleteCharAt(builder.length - 1)
        builder.append(")")

        sql = builder.toString()
        SQL_CACHE_INSERT.put(this, sql)
    }
    result.sql = sql!!
    result.addBindArgs(entityPairs)

    result.log()

    return result
}

/**构建replace语句*/
internal fun TableModel<*>.buildReplaceSqlInfo(entity: Any): SqlInfo? {
    val entityPairs = entity2PairList(this, entity) ?: return null

    val result = SqlInfo()
    var sql = SQL_CACHE_REPLACE[this]
    if (isEmpty(sql)) {
        val builder = StringBuilder()
        builder.append("REPLACE INTO '$name' (")
        for (kv in entityPairs) {
            builder.append("'${kv.first}',")
        }
        builder.deleteCharAt(builder.length - 1)
        builder.append(") VALUES (")

        for (i in entityPairs) {
            builder.append("?,")
        }
        builder.deleteCharAt(builder.length - 1)
        builder.append(")")

        sql = builder.toString()
        SQL_CACHE_REPLACE.put(this, sql)
    }
    result.sql = sql!!
    result.addBindArgs(entityPairs)

    result.log()
    return result
}


/**构建删除语句*/
internal fun <T : Any> TableModel<*>.buildDeleteSqlInfo(entity: T): ISqlInfo {
    val idValue = id!!.getColumnValue(entity)
            ?: throw DbException(msg = "this entity[$entityType]'s id value is null")
    return buildDeleteSqlInfoById(idValue)
}

/**构建删除语句*/
internal fun TableModel<*>.buildDeleteSqlInfoById(idValue: Any): ISqlInfo {
    return buildDeleteSqlInfo {
        and(id!!.name, "=", idValue)
    }
}

/**构建删除语句*/
internal fun TableModel<*>.buildDeleteSqlInfo(where: (IWhere.() -> Unit)?): ISqlInfo {
    if (where == null) return SqlInfo()
    val instance = WhereBuilder()
    instance.where()
    val build = instance.build()
    if (isEmpty(build)) throw DbException(msg = "whereBuilder can not be empty")
    val sql = "DELETE FROM '$name' WHERE $build"

    sql.log()

    return SqlInfo(sql)
}


/**构建更新语句*/
internal fun TableModel<*>.buildUpdateSqlInfo(entity: Any, vararg updateColumnNames: String): ISqlInfo? {

    val entityPairs = entity2PairList(this, entity) ?: return null

    val idValue = id!!.getColumnValue(entity)
            ?: throw DbException(msg = "this entity[$entityType]'s id value is null")

    val result = SqlInfo()
    val builder = StringBuilder("UPDATE '$name' SET ")
    entityPairs.asSequence()
            .filter { updateColumnNames.isEmpty() || updateColumnNames.contains(it.first) }
            .forEach {
                builder.append("'${it.first}'=?,")
                result.addBindArgs(it)
            }
    builder.deleteCharAt(builder.length - 1)
    builder.append(" WHERE ${WhereBuilder().and(id.name, "=", idValue)}")

    result.sql = builder.toString()

    result.log()

    return result
}


/**构建更新语句*/
internal fun TableModel<*>.buildUpdateSqlInfoByWhere(whereBuilder: IWhere?, vararg nameValuePairs: Pair<String, Any>): ISqlInfo? {

    if (nameValuePairs.isEmpty()) return null

    val result = SqlInfo()
    val builder = StringBuilder("UPDATE '$name' SET ")

    for (kv in nameValuePairs) {
        builder.append("'${kv.first}'=?,")
        result.addBindArgs(kv)
    }
    builder.deleteCharAt(builder.length - 1)

    val build by lazy { whereBuilder!!.build() }

    if (whereBuilder != null && !isEmpty(build)) {
        builder.append(" WHERE ").append(build)
    }

    result.sql = builder.toString()

    result.log()

    return result
}


/**构建建表语句*/
internal fun TableModel<*>.buildCreateTableSqlInfo(): ISqlInfo {

    val builder = StringBuilder()
    builder.append("CREATE TABLE IF NOT EXISTS '$name' (")
    if (id != null) {
        builder.append("'${id.name}' ")
        if (id.isAutoId) {
            builder.append("INTEGER PRIMARY KEY AUTOINCREMENT, ")
        } else {
            builder.append(id.columnConverter.getColumnType()).append(" PRIMARY KEY, ")
        }
    }
    columnMap.values
            .filterNot { it.isId }
            .forEach {
                builder.append("'${it.name}' ${it.columnConverter.getColumnType()} ${it.property},")
            }
    builder.deleteCharAt(builder.length - 1)
    builder.append(")")
    val sql = builder.toString()

    sql.log()

    return SqlInfo(sql)
}


/**将TableModel中的column转换为list*/
private fun entity2PairList(table: TableModel<*>, entity: Any): List<Pair<String, Any?>>? {
    val keyValueList = ArrayList<Pair<String, Any?>>(table.columnMap.size)
    table.columnMap.values
            .asSequence()
            .filterNot { it.isAutoId }
            .mapTo(keyValueList) { Pair(it.name, it.getFieldValue(entity)) }
    if (isEmpty(keyValueList)) return null
    return keyValueList
}