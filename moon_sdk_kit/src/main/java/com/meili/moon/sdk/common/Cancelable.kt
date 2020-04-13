package com.meili.moon.sdk.common

/**
 * 可以被取消
 * Created by imuto on 17/11/23.
 */
interface Cancelable {
    /**等待任务结束后，取消回调等后续操作*/
    fun cancel(immediately: Boolean = false)

    /**是否已经取消任务*/
    fun hasCancelled(): Boolean
}