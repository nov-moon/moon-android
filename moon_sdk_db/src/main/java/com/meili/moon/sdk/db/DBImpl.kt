package com.meili.moon.sdk.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteStatement
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.util.closeQuietly
import java.io.File

/**
 * Created by imuto on 17/12/26.
 */
class DBImpl private constructor(override val dbConfig: IDB.Config, override val app: Context) : AbsDB() {
    override val table: ITable

    override val database: SQLiteDatabase

    companion object {
        private val DB_CACHE = hashMapOf<IDB.Config, IDB>()

        @Synchronized
        fun getInstance(app: Context, config: IDB.Config = IDB.Config()): IDB {
            var db = DB_CACHE[config]
            if (db == null) {
                db = DBImpl(config, app)
            } else {
                db.dbConfig.config(config)
            }
            val oldVersion = db.database.version
            if (oldVersion == config.dbVersion) {
                return db
            }
            if (oldVersion != 0) {
                if (db.dbConfig.dbUpgradeListener != null) {
                    db.dbConfig.dbUpgradeListener!!.onUpgrade(db, oldVersion, config.dbVersion)
                } else {
                    db.dropDB()
                }
            }
            db.database.version = config.dbVersion
            return db
        }
    }

    init {
        database = openOrCreateDatabase()
        table = TableImpl(this)
        dbConfig.dbOpenListener?.onDbOpened(this)
    }

    override fun close() {
        DB_CACHE.remove(dbConfig)
        database.close()
    }

    override fun execute(sql: String) {
        try {
            database.execSQL(sql)
        } catch (e: Throwable) {
            throw DbException(cause = e)
        }
    }

    override fun execute(sql: ISqlInfo) {
        var statement: SQLiteStatement? = null
        try {
            statement = sql.buildStatement(database)
            statement.execute()
        } catch (e: Throwable) {
            throw DbException(cause = e)
        } finally {
            closeQuietly(statement)
        }
    }

    override fun executeUpdateDelete(sql: String): Int {
        var statement: SQLiteStatement? = null
        try {
            statement = database.compileStatement(sql)
            return statement!!.executeUpdateDelete()
        } catch (e: Throwable) {
            throw DbException(cause = e)
        } finally {
            closeQuietly(statement)
        }
    }

    override fun executeUpdateDelete(sql: ISqlInfo): Int {
        var statement: SQLiteStatement? = null
        try {
            statement = sql.buildStatement(database)
            return statement.executeUpdateDelete()
        } catch (e: Throwable) {
            throw DbException(cause = e)
        } finally {
            closeQuietly(statement)
        }
    }

    override fun query(sql: String): Cursor {
        try {
            return database.rawQuery(sql, null)
        } catch (e: Throwable) {
            throw DbException(cause = e)
        }

    }

    override fun query(sql: ISqlInfo): Cursor {
        try {
            return database.rawQuery(sql.sql, sql.getBindArgsAsStrArray())
        } catch (e: Throwable) {
            throw DbException(cause = e)
        }
    }


    private fun openOrCreateDatabase(): SQLiteDatabase {
        val dbDir = dbConfig.dbDir
        return if (dbDir != null && (dbDir.exists() || dbDir.mkdirs())) {
            val dbFile = File(dbDir, dbConfig.dbName)
            SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        } else {
            app.openOrCreateDatabase(dbConfig.dbName, 0, null)
        }
    }
}