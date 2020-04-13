package com.meili.moon.sdk.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.meili.moon.sdk.exception.DbException
import java.io.Closeable
import java.io.File
import kotlin.reflect.KClass

/**
 * 数据库操作对象
 * Created by imuto on 17/12/25.
 */
interface IDB : Closeable {

    /**app对象*/
    val app: Context

    /**数据库配置*/
    val dbConfig: Config

    /**获取Android的database对象*/
    val database: SQLiteDatabase

    val table: ITable

    /**删除数据库*/
    fun dropDB()

    /**删除指定table*/
    @Throws(DbException::class)
    fun dropTable(tableKClz: KClass<*>)

    /**给指定表添加指定列*/
    @Throws(DbException::class)
    fun addColumn(tableKClz: KClass<*>, column: String)

    /**执行操作*/
    @Throws(DbException::class)
    fun execute(sql: ISqlInfo)

    /**执行操作*/
    @Throws(DbException::class)
    fun execute(sql: String)

    /**执行update或者delete操作*/
    @Throws(DbException::class)
    fun executeUpdateDelete(sql: String): Int

    /**执行update或者delete操作*/
    @Throws(DbException::class)
    fun executeUpdateDelete(sql: ISqlInfo): Int

    /**查询数据*/
    @Throws(DbException::class)
    fun query(sql: ISqlInfo): Cursor

    /**查询数据*/
    @Throws(DbException::class)
    fun query(sql: String): Cursor

    /**
     * 数据库打开监听器
     */
    interface DbOpenListener {
        fun onDbOpened(db: IDB)
    }

    /**
     * 数据库升级监听器
     */
    interface DbUpgradeListener {
        fun onUpgrade(db: IDB, oldVersion: Int, newVersion: Int)
    }

    /**
     * 表创建监听器
     */
    interface TableCreateListener {
        fun onTableCreated(db: IDB, table: String)
    }

    data class Config(
            /**数据库的位置*/
            var dbDir: File? = null,
            /**数据库名称*/
            var dbName: String = "mljr.db",
            /**数据库版本号*/
            var dbVersion: Int = 1,
            /**数据库是否允许使用事务*/
            var allowTransaction: Boolean = true,
            /**数据库升级监听*/
            var dbUpgradeListener: DbUpgradeListener? = null,
            /**数据库创建监听*/
            var tableCreateListener: TableCreateListener? = null,
            /**数据库打开监听*/
            var dbOpenListener: DbOpenListener? = null) {

        fun config(config: Config) {
            dbVersion = config.dbVersion
            allowTransaction = config.allowTransaction
            dbUpgradeListener = config.dbUpgradeListener
            tableCreateListener = config.tableCreateListener
            dbOpenListener = config.dbOpenListener
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false

            val daoConfig = o as Config?

            if (dbName != daoConfig!!.dbName) return false
            return if (dbDir == null) daoConfig.dbDir == null else dbDir == daoConfig.dbDir
        }

        override fun hashCode(): Int {
            var result = dbName.hashCode()
            result = 31 * result + if (dbDir != null) dbDir!!.hashCode() else 0
            return result
        }

        override fun toString(): String {
            return dbDir.toString() + "/" + dbName
        }
    }
}