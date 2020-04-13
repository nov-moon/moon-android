/**
 * 参数检查工具类
 * Created by imuto on 18/3/12.
 */
@file:JvmName("ParamChecker")

package com.meili.moon.sdk.util

import com.meili.moon.sdk.common.BaseException

private const val errorMsg = "：参数错误"


/**检查参数为true,否则抛出后面的错误*/
fun Boolean.assertTrue(ex: Throwable): Boolean {
    if (!this) {
        throw ex
    }
    return this
}

/**检查参数为true,否则抛出后面的错误*/
fun Boolean.assertTrue(msg: String = "assertTrue$errorMsg"): Boolean {
    if (!this) {
        throw BaseException(msg = msg)
    }
    return this
}

/**检查参数为false,否则抛出后面的错误*/
fun Boolean.assertFalse(ex: Throwable) = !(!this).assertTrue(ex)

/**检查参数为false,否则抛出后面的错误*/
fun Boolean.assertFalse(msg: String = "assertFalse$errorMsg") = !(!this).assertTrue(msg)

/**检查参数[v]不为null，如果为null，则抛出指定msg的空指针异常*/
fun <T> assertNonNull(v: T?, msg: String = "assertNonNull$errorMsg"): T {

    if (v == null) {
        throw NullPointerException(msg)
    }

    return v
}

/**检查参数[v]不为null，如果为null，则抛出指定的Throwable*/
fun <T> assertNonNull(v: T?, ex: Throwable): T {

    if (v == null) {
        throw ex
    }

    return v
}