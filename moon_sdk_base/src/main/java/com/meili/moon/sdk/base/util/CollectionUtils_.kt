package com.meili.moon.sdk.base.util

/**
 * Created by imuto on 2018/7/11.
 */

inline fun Int.foreach(callback: (i: Int) -> Unit) {
    (0 until this).forEach { callback.invoke(it) }
}