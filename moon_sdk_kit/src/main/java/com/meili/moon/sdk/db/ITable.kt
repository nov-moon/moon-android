package com.meili.moon.sdk.db

import com.meili.moon.sdk.exception.DbException
import com.meili.sdk.db.sql.ISelector
import kotlin.reflect.KClass

/**
 * 表操作对象
 * Created by imuto on 17/12/25.
 */
interface ITable {

    /**数据库操作对象*/
    val db: IDB

    /**保存**entity**对象，
     * 1. entity.id != null，
     *      * 如果id存在，则进行数据更新
     *      * 如果id不存在，则进行数据插入
     * 2. entity.id == null
     *      * 如果id为自增长，插入数据后，赋值id到entity中
     *      * 如果id不是自增长，则插入失败
     */
    @Throws(DbException::class)
    fun save(entity: Any)

    /**
     * 更新符合[where]条件的指定表[tableClz]的[keyValue]字段组。如果要更新某一个实体，请使用[save]方法
     *
     * @param tableClz 要更新的表
     * @param keyValue 更新的字段key、value
     * @param where 更新的条件
     *
     * @return 影响的条数
     */
    @Throws(DbException::class)
    fun update(tableClz: KClass<*>, vararg keyValue: Pair<String, Any>, where: (IWhere.() -> Unit)? = null): Int

    /**
     * 根据entity的id删除指定entity数组，如果其中有一个失败，则整个操作失败
     */
    @Throws(DbException::class)
    fun <T> delete(entity: List<T>)

    /**
     * 根据entity的id删除指定entity数组，如果其中有一个失败，则整个操作失败
     */
    @Throws(DbException::class)
    fun <T> delete(entity: T)

    /**
     * 删除指定表中符合where条件的记录，如果where为空，则删除失败。
     *
     * 如果要删除整张表，请使用[save]方法
     *
     * @return 删除的条数
     */
    @Throws(DbException::class)
    fun delete(tableClz: KClass<*>, where: (IWhere.() -> Unit)? = null): Int

    /**
     * 根据[id]获取指定表[tableClz]中的记录[T]
     *
     * @param T 要获取的数据泛型
     * @param id 表中的ID
     * @param tableClz 表的描述class
     */
    @Throws(DbException::class)
    fun <T : Any> get(id: Any, tableClz: KClass<T>): T?

    /**
     * 获取[tableClz]表中符合[where]条件的数据记录
     *
     * @param T 要获取的数据泛型
     * @param tableClz 表的描述class
     * @param where 获取的条件
     */
    @Throws(DbException::class)
    fun <T : Any> get(tableClz: KClass<T>, where: (IWhere.() -> Unit)? = null): List<T>

    /**
     * 获取[tableClz]表中符合[where]条件的第一条数据记录
     *
     * @param T 要获取的数据泛型
     * @param tableClz 表的描述class
     * @param where 获取的条件
     */
    @Throws(DbException::class)
    fun <T : Any> getFirst(tableClz: KClass<T>, where: (IWhere.() -> Unit)? = null): T?

    /**获取指定实体的selector实体，用来做高级查询*/
    fun <T : Any> selector(tableClz: KClass<T>, init_: (ISelector<T>.() -> Unit)? = null): ISelector<T>

}