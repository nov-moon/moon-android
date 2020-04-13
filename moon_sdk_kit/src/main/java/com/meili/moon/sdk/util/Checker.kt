/**
 * 处理empty
 * Created by imuto on 17/11/24.
 */
@file:JvmName("ArrayUtil")

package com.meili.moon.sdk.util

import android.util.SparseArray
import android.util.SparseBooleanArray
import android.util.SparseIntArray
import java.io.File

@Deprecated("已过时", replaceWith = ReplaceWith("T.isEmptyOrNull()"))
fun isEmpty(list: Array<*>?) = list == null || list.isEmpty()

fun isEmpty(list: BooleanArray?) = list == null || list.isEmpty()
fun isEmpty(list: CharArray?) = list == null || list.isEmpty()
fun isEmpty(list: DoubleArray?) = list == null || list.isEmpty()
fun isEmpty(list: FloatArray?) = list == null || list.isEmpty()
fun isEmpty(list: IntArray?) = list == null || list.isEmpty()
fun isEmpty(list: LongArray?) = list == null || list.isEmpty()
fun isEmpty(list: ShortArray?) = list == null || list.isEmpty()
fun isEmpty(list: SparseIntArray?) = list == null || list.size() <= 0
fun isEmpty(list: SparseBooleanArray?) = list == null || list.size() <= 0
fun isEmpty(list: SparseArray<*>?) = list == null || list.size() <= 0
fun isEmpty(list: Collection<*>?) = list == null || list.isEmpty()
fun isEmpty(list: Map<*, *>?) = list == null || list.isEmpty()
fun isEmpty(str: CharSequence?) = str == null || str.isEmpty()
fun hasEmpty(vararg str: CharSequence?): Boolean {
    str.forEach {
        if (isEmpty(it)) return true
    }
    return false
}

fun exists(file: File?) = file != null && file.exists()

fun exists(file: String?) = file != null && exists(File(file))

fun largerSize(list: Array<*>?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: BooleanArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: CharArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: DoubleArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: FloatArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: IntArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: LongArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: ShortArray?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: SparseIntArray?, size: Int) = if (isEmpty(list)) false else list!!.size() > size
fun largerSize(list: SparseBooleanArray?, size: Int) = if (isEmpty(list)) false else list!!.size() > size
fun largerSize(list: SparseArray<*>?, size: Int) = if (isEmpty(list)) false else list!!.size() > size
fun largerSize(list: Collection<*>?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(list: Map<*, *>?, size: Int) = if (isEmpty(list)) false else list!!.size > size
fun largerSize(str: CharSequence?, size: Int) = if (isEmpty(str)) false else str!!.length > size

fun sameSize(list: Array<*>?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: BooleanArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: CharArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: DoubleArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: FloatArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: IntArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: LongArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: ShortArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: SparseIntArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size() == size)
fun sameSize(list: SparseBooleanArray?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size() == size)
fun sameSize(list: SparseArray<*>?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size() == size)
fun sameSize(list: Collection<*>?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(list: Map<*, *>?, size: Int) = (isEmpty(list) && size <= 0) || (list != null && list.size == size)
fun sameSize(str: CharSequence?, size: Int) = (isEmpty(str) && size <= 0) || (str != null && str.length == size)