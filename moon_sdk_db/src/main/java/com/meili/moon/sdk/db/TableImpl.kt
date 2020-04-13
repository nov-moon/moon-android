package com.meili.moon.sdk.db

import com.meili.moon.sdk.db.sqlite.*
import com.meili.moon.sdk.db.table.ColumnModel
import com.meili.moon.sdk.db.table.TableModel
import com.meili.moon.sdk.db.utils.closeQuietly
import com.meili.moon.sdk.db.utils.getTable
import com.meili.moon.sdk.db.utils.select
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.util.assertTrue
import com.meili.moon.sdk.util.isEmpty
import com.meili.sdk.db.sql.ISelector
import kotlin.reflect.KClass

/**
 * Created by imuto on 18/1/4.
 */
class TableImpl(override val db: IDB) : ITable {


    override fun save(entity: Any) {
        operationListOrEntity(entity, { model, item, isList ->
            saveOrUpdate(model, item)
        })

    }

    override fun update(tableClz: KClass<*>, vararg keyValue: Pair<String, Any>, where: (IWhere.() -> Unit)?): Int {
        val table = db.getTable(tableClz)
        if (!table.exist()) return 0

        val result: Int
        try {
            beginTransaction()
            var whereInstance: IWhere? = null
            if (where != null) {
                whereInstance = WhereBuilder()
                whereInstance.where()
            }
            result = db.executeUpdateDelete(table.buildUpdateSqlInfoByWhere(whereInstance, *keyValue)!!)
            setTransactionSuccessful()
        } finally {
            endTransaction()
        }

        return result
    }

    override fun <T> delete(entity: List<T>) {
        operationListOrEntity(entity, { model, item, isList ->
            db.execute(model.buildDeleteSqlInfo(item))
        })
    }

    override fun <T> delete(entity: T) {
        operationListOrEntity(entity, { model, item, isList ->
            db.execute(model.buildDeleteSqlInfo(item))
        })
    }

    override fun delete(tableClz: KClass<*>, where: (IWhere.() -> Unit)?): Int {
        val table = db.getTable(tableClz)
        if (!table.exist()) return 0
        val result: Int
        try {
            beginTransaction()

            result = db.executeUpdateDelete(table.buildDeleteSqlInfo(where))

            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
        return result
    }

    override fun <T : Any> get(id: Any, tableClz: KClass<T>): T? {
        val table = db.getTable(tableClz)
        if (!table.exist()) return null

        return select(table) {
            where {
                and(table.id!!.name, "=", id)
            }
            limit = 1
        }.findFirst()
    }

    override fun <T : Any> get(tableClz: KClass<T>, where: (IWhere.() -> Unit)?): List<T> {
        return selector(tableClz) { where(where) }.findAll()
    }

    override fun <T : Any> getFirst(tableClz: KClass<T>, where: (IWhere.() -> Unit)?): T? {
        return selector(tableClz) { where(where) }.findFirst()
    }

    override fun <T : Any> selector(tableClz: KClass<T>, init_: (ISelector<T>.() -> Unit)?): ISelector<T> {
        val table = db.getTable(tableClz)
        table.exist().assertTrue("数据库：${tableClz.simpleName}类映射的数据库表创建失败")
        return select(table, init_)
    }

    /**
     * 1. 打开事务
     * 2. 检查是否是list
     * 3. list处理
     *    1. 转型为list，并验证list是否为空
     *    2. 获取list的实体对象对应的表实体
     *    3. 检查对应的表是否已经创建，如果没有创建则创建
     *    4. 做相应操作
<<<<<<< HEAD
     * 4. 不是list处理
=======
     * 4. 不是list处理zhongqiu
>>>>>>> origin
     *    1. 重复3.2-3.4
     * 5.标志事务成功
     * 6.结束事务
     */
    private fun operationListOrEntity(entity: Any?, callback: (TableModel<*>, Any, Boolean) -> Unit) {
        try {
            beginTransaction()

            if (entity is List<*>) {
                val itemTmp = entity[0]
                val table = db.getTable(itemTmp!!::class)

                createTableIfNotExist(table)
                for (item in entity) {
                    callback.invoke(table, item!!, true)
                }
            } else {
                val table = db.getTable(entity!!::class)
                createTableIfNotExist(table)
                callback.invoke(table, entity, false)
            }

            setTransactionSuccessful()
        } finally {
            endTransaction()
        }
    }

    /**
     * 检查id
     *  * 不是null，并且是自增长
     *      1. entity中有id的值，则作为更新操作
     *      2. entity中没有id的值，则作为保存操作，并且将id重新绑定到entity中
     *  * 是null
     *      1. 执行replace操作
     */
    @Throws(DbException::class)
    private fun saveOrUpdate(table: TableModel<*>, entity: Any) {
        if (table.id != null && table.id.isAutoId) {
            if (table.id.getColumnValue(entity) != null && (table.id.getColumnValue(entity) as Number).toLong() > 0) {
                db.execute(table.buildUpdateSqlInfo(entity)!!)
            } else {
                insertAutoIncrementEntityAndBindId(table, entity, table.id)
            }
        } else {
            db.execute(table.buildReplaceSqlInfo(entity)!!)
        }
    }

    @Throws(DbException::class)
    private fun saveBindingId(table: TableModel<*>, entity: Any): Boolean {
        val id = table.id
        if (id != null && id.isAutoId) {
            if (insertAutoIncrementEntityAndBindId(table, entity, id)) return false
            return true
        } else {
            db.execute(table.buildInsertSqlInfo(entity)!!)
            return true
        }
    }

    private fun insertAutoIncrementEntityAndBindId(table: TableModel<*>, entity: Any, id: ColumnModel): Boolean {
        db.execute(table.buildInsertSqlInfo(entity)!!)
        val idValue = getLastAutoIncrementId(table.name)
        if (idValue == -1L) {
            return true
        }
        id.setAutoIdValue(entity, idValue)
        return false
    }

    /**
     * 1. 检查Table是否已经创建
     *  * 已经创建，直接返回
     *  * 未创建
     *      1.创建table
     *      2.尝试执行table的onCreated语句
     *      3.尝试执行onTableCreated方法
     */
    @Throws(DbException::class)
    private fun createTableIfNotExist(table: TableModel<*>) {
        if (table.exist()) {
            return
        }
        synchronized(table.javaClass) {
            if (table.exist()) {
                return
            }
            val sqlInfo = table.buildCreateTableSqlInfo()
            db.execute(sqlInfo)
            if (!isEmpty(table.onCreated)) {
                db.execute(table.onCreated)
            }
            table.checkedDatabase = true
            db.dbConfig.tableCreateListener?.onTableCreated(db, table.name)
        }
    }

    /**获取指定表的自增长最后id*/
    @Throws(DbException::class)
    private fun getLastAutoIncrementId(tableName: String): Long {
        var id: Long = -1
        val cursor = db.query("SELECT seq FROM sqlite_sequence WHERE name='$tableName' LIMIT 1")
        try {
            if (cursor.moveToNext()) {
                id = cursor.getLong(0)
            }
        } catch (e: Throwable) {
            throw DbException(cause = e)
        } finally {
            closeQuietly(cursor)
        }
        return id
    }

    private fun beginTransaction() {
        if (db.dbConfig.allowTransaction) {
            db.database.beginTransaction()
        }
    }

    private fun setTransactionSuccessful() {
        if (db.dbConfig.allowTransaction) {
            db.database.setTransactionSuccessful()
        }
    }

    private fun endTransaction() {
        if (db.dbConfig.allowTransaction) {
            db.database.endTransaction()
        }
    }
}