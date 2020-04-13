package com.meili.moon.sdk.db.sqlite

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.meili.moon.sdk.db.ISqlInfo
import com.meili.moon.sdk.db.converter.getColumnConverter
import com.meili.moon.sdk.db.utils.convert2DbValueIfNeeded
import com.meili.moon.sdk.util.isEmpty

/**
 * 生成sql的statement
 * Created by imuto on 18/1/3.
 */
class SqlInfo(override var sql: String = "") : ISqlInfo{
    private val bindArgs = mutableListOf<Pair<String, Any?>>()

    override fun addBindArgs(pair: Pair<String, Any?>) {
        bindArgs.add(pair)
    }

    override fun addBindArgs(args: Collection<Pair<String, Any?>>) {
        bindArgs.addAll(args)
    }

    override fun buildStatement(database: SQLiteDatabase): SQLiteStatement {
        val result = database.compileStatement(sql)
        if (isEmpty(bindArgs)) {
            return result
        }
        var i = 1
        bindArgs.forEach {
            val value = convert2DbValueIfNeeded(it.second)
            with(result) {
                if (value == null) {
                    bindNull(i)
                } else {
                    val converter = value.javaClass.getColumnConverter()
                    val type = converter.getColumnType()
                    when (type) {
                        ColumnDbType.INTEGER -> bindLong(i, (value as Number).toLong())
                        ColumnDbType.REAL -> bindDouble(i, (value as Number).toDouble())
                        ColumnDbType.TEXT -> bindString(i, value.toString())
                        ColumnDbType.BLOB -> bindBlob(i, value as ByteArray)
                    }
                }
            }
            i++
        }

        return result
    }

    override fun getBindArgs(): Array<Any?> {
        val result: Array<Any?> = arrayOfNulls(bindArgs.size)
        for (i in bindArgs.indices) {
            result[i] = convert2DbValueIfNeeded(bindArgs[i].second)
        }
        return result
    }

    override fun getBindArgsAsStrArray(): Array<String?> {
        val result: Array<String?> = arrayOfNulls(bindArgs.size)
        for (i in bindArgs.indices) {
            result[i] = convert2DbValueIfNeeded(bindArgs[i].second)?.toString()
        }
        return result
    }
}