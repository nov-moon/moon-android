package com.meili.moon.sdk.util

import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray

/**
 * 从0遍历到当前值
 *
 * [include]表示是否包含当前值，默认不包含，如果包含则遍历总数为：当前值+1
 */
inline fun Int.foreach(include: Boolean = false, lambda: (item: Int) -> Unit) {

    val range = if (include) {
        (0..this)
    } else {
        (0 until this)
    }

    range.forEach(lambda)
}

/**
 * 从当前值反向遍历到0
 *
 * @param [include] 是否包含当前值，默认不包含，如果包含则遍历总数为：当前值+1
 *
 */
inline fun Int.foreachInverse(include: Boolean = false, lambda: (item: Int) -> Unit) {
    if (this <= 0) return
    val start = if (include) this else this - 1
    (start downTo 0).forEach(lambda)
}

/**
 * 反向遍历一个区间值，也可以直接使用downTo方法
 *
 */
inline fun IntRange.foreachInverse(lambda: (item: Int) -> Unit) {
    if (isEmpty()) return
    (endInclusive downTo start).forEach(lambda)
}

/**
 * 反向遍历一个区间值，也可以直接使用downTo方法
 *
 */
inline fun LongRange.foreachInverse(lambda: (item: Long) -> Unit) {
    if (isEmpty()) return
    (endInclusive downTo start).forEach(lambda)
}


/**
 * 遍历当前集合，入参[lambda]的第一个参数为当前循环的index，第二个参数为当前集合的item对象。
 * 如不需index信息，请参考另外一个[forEach]方法
 */
inline fun <T> SparseArray<T>.forEach(lambda: (index: Int, item: T) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(i, valueAt(i))
    }
}


/**
 * 遍历当前集合，入参[lambda]的参数为当前集合的item对象。
 * 如需index信息，请参考[forEach]方法
 */
inline fun <T> SparseArray<T>.forEach(lambda: (item: T) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(valueAt(i))
    }
}


/**
 * 遍历当前集合，入参[lambda]的第一个参数为当前循环的index，第二个参数为当前集合的item对象。
 * 如不需index信息，请参考另外一个[forEach]方法
 */
inline fun SparseIntArray.forEach(lambda: (index: Int, item: Int) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(i, valueAt(i))
    }
}

/**
 * 遍历当前集合，入参[lambda]的参数为当前集合的item对象。
 * 如需index信息，请参考[forEach]方法
 */
inline fun SparseIntArray.forEach(lambda: (item: Int) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(valueAt(i))
    }
}

/**
 * 遍历当前集合，入参[lambda]的第一个参数为当前循环的index，第二个参数为当前集合的item对象。
 * 如不需index信息，请参考另外一个[forEach]方法
 */
inline fun SparseBooleanArray.forEach(lambda: (index: Int, item: Boolean) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(i, valueAt(i))
    }
}

/**
 * 遍历当前集合，入参[lambda]的参数为当前集合的item对象。
 * 如需index信息，请参考[forEach]方法
 */
inline fun SparseBooleanArray.forEach(lambda: (item: Boolean) -> Unit) {
    for (i in 0 until size()) {
        lambda.invoke(valueAt(i))
    }
}