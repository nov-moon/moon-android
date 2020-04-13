package com.meili.sdk.db.sql

import android.database.Cursor
import com.meili.moon.sdk.db.IGroupBy
import com.meili.moon.sdk.db.IWhere

/**
 * 定义一个select语句，用来获取指定类型[T]的实例或者数量
 * Created by imuto on 18/1/10.
 */
interface ISelector<T> {

    var limit: Int
    var offset: Int
    val table: String

    fun select(columns: List<String>): ISelector<T>

    fun where(init: (IWhere.() -> Unit)? = null): ISelector<T>

    fun groupBy(vararg columns: String, init: IGroupBy.() -> Unit): ISelector<T>

    fun orderBy(orderBy: OrderBy): ISelector<T>

    fun findFirst(converter: ((Cursor) -> T)? = null): T?

    fun findAll(converter: ((Cursor) -> T)? = null): List<T>

    fun count(columnName: String? = null): Long

    class OrderBy(private var columnName: String, private var desc: Boolean = false) {
        override fun toString(): String {
            return "\"" + columnName + "\"" + if (desc) " DESC" else " ASC"
        }
    }
}