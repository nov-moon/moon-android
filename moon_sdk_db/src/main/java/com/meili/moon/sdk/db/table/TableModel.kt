package com.meili.moon.sdk.db.table

import com.meili.moon.sdk.db.IDB
import com.meili.moon.sdk.db.annotation.Table
import com.meili.moon.sdk.db.utils.exist
import com.meili.moon.sdk.db.utils.findColumnMap
import com.meili.moon.sdk.exception.DbException
import com.meili.moon.sdk.util.isEmpty
import java.lang.reflect.Constructor
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 18/1/3.
 */
class TableModel<T : Any>(val db: IDB, val entityType: KClass<T>) {
    /**数据库表名*/
    val name: String
    /**当数据库创建的时候的执行语句*/
    val onCreated: String
    /**数据库id字段*/
    val id: ColumnModel?
    /**对应model实体类的构造方法*/
    private val constructor: Constructor<T> = entityType.java.getConstructor()
    /**是否已经检查过表的存在*/
    @Volatile
    internal var checkedDatabase: Boolean = false

    /**
     * key: columnName
     */
    val columnMap: LinkedHashMap<String, ColumnModel>

    init {
        this.constructor.isAccessible = true
        val table = entityType.findAnnotation<Table>()
                ?: throw DbException(msg = "保存数据库的实体类必须有[Table]注解")

        this.name = if (isEmpty(table.value)) entityType.simpleName!! else table.value

        this.onCreated = table.onCreated
        this.columnMap = entityType.findColumnMap(table.propertyWithAnnotation)

        id = columnMap.values.firstOrNull { it.isId }
    }

    @Throws(Throwable::class)
    fun createEntity(): T {
        return this.constructor.newInstance()
    }

    /**
     * 表是否存在
     */
    @Throws(DbException::class)
    fun exist(): Boolean {
        if (checkedDatabase) {
            return true
        }

        checkedDatabase = db.exist(name)
        return checkedDatabase
    }

    override fun toString(): String {
        return name
    }
}