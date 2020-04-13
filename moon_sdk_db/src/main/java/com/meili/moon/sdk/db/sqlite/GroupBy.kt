package com.meili.moon.sdk.db.sqlite

import com.meili.moon.sdk.db.IGroupBy
import com.meili.moon.sdk.db.IWhere
import com.meili.moon.sdk.util.isEmpty

/**
 * groupBy字段，当前不支持聚合函数，后面会做优化
 * Created by imuto on 18/1/25.
 */
class GroupBy(private val columns: List<String>) : IGroupBy {
    private var having: IWhere? = null

    override fun having(init: IWhere.() -> Unit) {
        having = WhereBuilder()
        init.invoke(having!!)
    }

    override fun build(): String {
        return toString()
    }

    override fun toString(): String {
        if (isEmpty(columns)) {
            return ""
        }
        val result = StringBuilder(" GROUP BY ")
        columns.map {
            if (isEmpty(it)) "NULL" else it
        }.forEach {
                    result.append("'")
                    result.append(it)
                    result.append("',")
                }
        result.deleteCharAt(result.length - 1)

        if (having == null) {
            return result.toString()
        }

        val where = having!!.build()
        if (!isEmpty(where)) {
            result.append(" WHERE")
            result.append(where)
        }

        return result.toString()
    }
}