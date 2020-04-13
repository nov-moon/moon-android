package com.meili.moon.sdk.task

/**
 * 子线程到主线程的消息同步接口
 * Created by imuto on 17/11/23.
 */
interface IHandler {
    fun sendMessage(what: Int, arg1: Int = 0, arg2: Int = 0, obj: Any? = null, delayMillis: Long = 0)

    fun handleMessage(what: Int, arg1: Int = 0, arg2: Int = 0, obj: Any? = null)

    fun post(runnable: Runnable, delayMillis: Long = 0)

    fun removeCallbacks(runnable: Runnable)
}