@file:kotlin.jvm.JvmName("MoneyFormatUtils")

package com.meili.moon.sdk.base.util

import java.text.DecimalFormat

/**
 * Created by imuto on 2018/11/27.
 */


/**格式化表达式：带两位小数的金钱表达式*/
val FORMAT_MONEY_FLOAT = DecimalFormat("###,##0.00")
/**格式化表达式：不带小数的金钱表达式*/
val FORMAT_MONEY_INT = DecimalFormat("###,##0")

fun CharSequence?.formatToMoney(format: DecimalFormat = FORMAT_MONEY_FLOAT): String {
    val value = if(this.isNullOrEmpty()) "0" else this
    return ""
}