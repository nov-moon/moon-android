package com.meili.moon.sdk.db

/**
 * where的条件语句
 * Created by imuto on 18/1/10.
 */
interface IWhere {
    /**
     * 添加一个and条件
     *
     * 操作符号
     * 1. <> 不等于
     * 2. &gt; 大于
     * 3. <  小于
     * 4. &gt;= 大于等于 />
     * 5. <= 小于等于
     * 6. BETWEEN 在范围之间,**直接使用BETWEEN，不要带AND**
     * 7. LIKE 搜索,like可使用通配符( _ 只有一个)（ %或者* 任意多个），例如LIKE '_K%'代表第二个字母是K的
     * 8. IS NOT NULL 不为空
     * 9. IS NULL 为空
     * 10. IN () 在例举之中
     * 11. NOT IN () 不在例举之中**不支持NOT IN**
     *
     * @param columnName 表中的字段名称
     * @param op 操作符号
     * @param value column具体的值
     */
    fun and(columnName: String, op: String, value: Any?): IWhere

    /**添加一个and条件*/
    fun and(where: IWhere): IWhere

    /**
     * 添加一个or条件
     *
     * 操作符号
     * 1. <> 不等于
     * 2. &gt; 大于
     * 3. <  小于
     * 4. &gt;= 大于等于 />
     * 5. <= 小于等于
     * 6. BETWEEN 在范围之间,**直接使用BETWEEN，不要带AND**
     * 7. LIKE 搜索,like可使用通配符( _ 只有一个)（ %或者* 任意多个），例如LIKE '_K%'代表第二个字母是K的
     * 8. IS NOT NULL 不为空
     * 9. IS NULL 为空
     * 10. IN () 在例举之中
     * 11. NOT IN () 不在例举之中**不支持NOT IN**
     *
     * @param columnName 表中的字段名称
     * @param op 操作符号
     * @param value column具体的值
     */
    fun or(columnName: String, op: String, value: Any?): IWhere

    /**添加一个or条件*/
    fun or(where: IWhere): IWhere

    /**添加一个条件表达式，应该包含相应的and或者or*/
    fun expression(expression: String): IWhere

    /**构建where为一个string表达式*/
    fun build(): String
}