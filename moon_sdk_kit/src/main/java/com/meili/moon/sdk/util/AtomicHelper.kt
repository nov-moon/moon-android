/**
 * 提供Atomic操作帮助
 * Created by imuto on 18/3/12.
 */
@file:JvmName("AtomicHelper")

package com.meili.moon.sdk.util

import java.util.concurrent.atomic.AtomicReference

fun <T> AtomicReference<T>.setOnce(v: T) = compareAndSet(null, v).assertTrue("当前参数只能设置一次")
fun <T> AtomicReference<T>.setOnce(expect: T, v: T) = compareAndSet(expect, v).assertTrue("当前参数只能设置一次")
fun <T> AtomicReference<T>.setOnceNoError(v: T) = compareAndSet(null, v)
fun <T> AtomicReference<T>.setOnceNoError(expect: T, v: T) = compareAndSet(expect, v)
