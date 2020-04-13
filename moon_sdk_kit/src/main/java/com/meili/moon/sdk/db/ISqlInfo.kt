package com.meili.moon.sdk.db

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement

/**
 * SQLiteStatement生成器
 */
interface ISqlInfo {
    val sql: String
    fun addBindArgs(pair: Pair<String, Any?>)

    fun addBindArgs(args: Collection<Pair<String, Any?>>)

    fun buildStatement(database: SQLiteDatabase): SQLiteStatement

    fun getBindArgs(): Array<Any?>

    fun getBindArgsAsStrArray(): Array<String?>
}