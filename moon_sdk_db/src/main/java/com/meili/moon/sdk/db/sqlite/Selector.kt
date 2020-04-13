package com.meili.moon.sdk.db.sqlite

import android.database.Cursor
import com.meili.moon.sdk.db.IDB
import com.meili.moon.sdk.db.IGroupBy
import com.meili.moon.sdk.db.IWhere
import com.meili.moon.sdk.db.utils.closeQuietly
import com.meili.moon.sdk.db.utils.exist
import com.meili.moon.sdk.util.isEmpty
import com.meili.sdk.db.sql.ISelector

/**
 * select 的实现类
 * Created by imuto on 18/1/10.
 */
open class Selector<T>(
        /**数据库操作对象*/
        val db: IDB,
        /**要查询的表名*/
        override val table: String,
        val idName: String,
        /**表是否已经存在的委托判断方法*/
        val exist: (() -> Boolean)? = null) : ISelector<T> {

    /**group by 语句块，可能包含having*/
    private var groupBy: GroupBy? = null
    /**where 语句块*/
    private var where: WhereBuilder? = null
    /**排序语句块*/
    private var orderBy: MutableList<ISelector.OrderBy>? = null
    /**要查询的column，如果为null，则查询所有*/
    private var columns: List<String>? = null

    override var limit: Int = 0
    override var offset: Int = 0

    override fun select(columns: List<String>): ISelector<T> {
        this.columns = columns
        return this
    }

    override fun groupBy(vararg columns: String, init: IGroupBy.() -> Unit): ISelector<T> {
        groupBy = GroupBy(columns.toList())
        groupBy!!.init()
        return this
    }

    override fun where(init: (IWhere.() -> Unit)?): ISelector<T> {
        if (init == null) return this
        if (where == null) where = WhereBuilder()
        where!!.init()
        return this
    }

    override fun orderBy(orderBy: ISelector.OrderBy): ISelector<T> {
        if (this.orderBy == null) {
            this.orderBy = mutableListOf()
        }
        this.orderBy?.add(orderBy)
        return this
    }

    override fun findFirst(converter: ((Cursor) -> T)?): T? {
        if (!exist()) return null
        val query = db.query(toString())
        try {
            if (query.moveToNext()) {
                return converter!!(query)
            }
        } finally {
            closeQuietly(query)
        }
        return null
    }

    override fun findAll(converter: ((Cursor) -> T)?): List<T> {
        val result = mutableListOf<T>()
        if (!exist()) return result

        val query = db.query(toString())
        try {
            while (query.moveToNext()) {
                result.add(converter!!(query))
            }
        } finally {
            closeQuietly(query)
        }
        return result
    }

    override fun count(columnName: String?): Long {
        if (!exist()) return 0

        val column = columnName ?: idName

        val whereStr = if (where == null) "" else "WHERE ${where!!.build()}"

        val sql = "SELECT COUNT('$column') as count from '$table' $whereStr"
        sql.log()
        val query = db.query(sql)
        try {
            if (query.moveToNext()) {
                return query.getLong(0)
            }
        } finally {
            closeQuietly(query)
        }
        return 0
    }


    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("SELECT ")
        if (isEmpty(columns)) {
            sb.append("* ")
        } else {
            columns!!.forEach {
                sb.append(it)
                sb.append(",")
            }
            sb.deleteCharAt(sb.length - 1)
            sb.append(" ")
        }
        sb.append("FROM '")
                .append(table)
                .append("' ")
        val whereStr by lazy {
            where.toString()
        }
        if (where != null && !isEmpty(whereStr)) {
            sb.append("WHERE ")
                    .append(where.toString())
                    .append(" ")
        }

        if (groupBy != null) {
            sb.append(groupBy.toString())
                    .append(" ")
        }
        if (!isEmpty(orderBy)) {
            sb.append(" ORDER BY ")
            orderBy?.forEach {
                sb.append(orderBy.toString())
                        .append(", ")
            }
            sb.deleteCharAt(sb.length - 1)
            sb.deleteCharAt(sb.length - 1)
            sb.append(" ")
        }

        if (limit > 0) {
            sb.append("LIMIT ")
                    .append(limit)
                    .append(" OFFSET ")
                    .append(offset)
                    .append(" ")
        }

        val sql = sb.toString()

        sql.log()

        return sql
    }

    private fun exist() = exist?.invoke() ?: db.exist(table)

}