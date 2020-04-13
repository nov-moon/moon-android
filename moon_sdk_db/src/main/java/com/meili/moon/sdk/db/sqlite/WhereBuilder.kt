package com.meili.moon.sdk.db.sqlite

import com.meili.moon.sdk.db.IWhere
import com.meili.moon.sdk.db.converter.getColumnConverter
import com.meili.moon.sdk.db.utils.convert2DbValueIfNeeded

/**
 * where条件语句构造器
 * Created by imuto on 18/1/8.
 */
open class WhereBuilder : IWhere {

    private companion object {
        /**支持的操作符*/
        private val OPTION_MAP: Map<String, Option> by lazy {
            val map = hashMapOf<String, Option>()
            map[">"] = Option(">")
            map["<"] = Option("<")
            map[">="] = Option(">=")
            map["<="] = Option("<=")
            map["="] = Option(opt = { name, values ->
                val op = if (values[0] == "NULL") "IS" else "="
                "$name $op '${values[0]}'"
            })
            map["=="] = map["="]!!
            map["!="] = Option(opt = { name, values ->
                val op = if (values[0] == "NULL") "IS NOT" else "<>"
                "$name $op '${values[0]}'"
            })
            map["LIKE"] = Option("LIKE")
            map["BETWEEN"] = Option(opt = { name, values ->
                "$name BETWEEN '${values[0]}' AND '${values[1]}'"
            })
            map["IN"] = Option(opt = { name, values ->
                var result = "$name IN ( "
                values.forEach {
                    result = "$result, '$it'"
                }
                "$result )"
            })
            map["NOT IN"] = Option(opt = { name, values ->
                var result = "$name NOT IN ( "
                values.forEach {
                    result = "$result, '$it'"
                }
                "$result )"
            })
            map
        }

        private fun get(op: String, name: String, value: List<Any?>): String? {
            return OPTION_MAP[op.toUpperCase()]?.get(name, value)
        }

        private class Option(val op1: String? = null, val opt: ((String, MutableList<Any>) -> String)? = null) {
            /**根据[name]和[value]获取带前后空格的表达式*/
            fun get(name: String, value: List<Any?>): String {
                val list = value.toMutableList()
                //对value做过滤操作
                // 1. 如果为null，转换为NULL
                // 2. 如果为文本类型，检查是否有单引号，有的话将单引号转换为两个单引号
                list.indices
                        .forEach {
                            val item = list[it]
                            if (item == null) {
                                list[it] = "NULL"
                            } else {
                                val dbItem = convert2DbValueIfNeeded(item)
                                if (ColumnDbType.TEXT == dbItem!!.javaClass.getColumnConverter().getColumnType()) {
                                    if ((item as String).contains('\'')) {
                                        item.replace("'", "''")
                                    }
                                }
                            }
                        }
                return when {
                /**如果操作表达式可用*/
                    opt != null -> " ${opt.invoke(name, list as MutableList<Any>)} "
                /**如果操作符不为null,操作符连接只使用value的第一个值*/
                    op1 != null -> {
                        when {
                        /**如果值为null，则认为为没有value的表达式*/
                            value.isEmpty() -> " $name + $op1 "
                        /**正常的表达式*/
                            else -> " $name $op1 '${value[0]}' "
                        }
                    }
                    else -> ""
                }
            }
        }
    }


    private val whereItems = arrayListOf<String>()

    override fun and(columnName: String, op: String, value: Any?): IWhere {
        var result = get(op, columnName, listOf(value))
        if (!whereItems.isEmpty()) {
            result = " AND $result"
        }
        whereItems.add(result!!)
        return this
    }

    /**添加一个and条件*/
    override fun and(where: IWhere): IWhere {
        val condition = if (whereItems.size == 0) " " else "AND "
        return expression(condition + "(" + where.toString() + ")")
    }

    /**添加一个or条件*/
    override fun or(columnName: String, op: String, value: Any?): IWhere {
        var result = get(op, columnName, listOf(value))
        if (whereItems.isEmpty()) {
            result = " OR $result"
        }
        whereItems.add(result!!)
        return this
    }

    /** 添加一个or条件*/
    override fun or(where: IWhere): IWhere {
        val condition = if (whereItems.size == 0) " " else "OR "
        return expression(condition + "(" + where.toString() + ")")
    }

    override fun expression(expression: String): IWhere {
        whereItems.add(" $expression")
        return this
    }

    override fun build(): String {
        val sql = whereItems.joinToString(separator = "")

        sql.log()

        return sql
    }

    override fun toString(): String {
        return build()
    }
}