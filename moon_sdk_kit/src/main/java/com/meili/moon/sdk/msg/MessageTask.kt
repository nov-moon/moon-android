package com.meili.moon.sdk.msg

import android.os.Bundle
import com.meili.moon.sdk.CommonSdk

import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.task.AbsTask

/**
 * 消息Task基类
 * Created by imuto on 17/2/14.
 */
abstract class MessageTask<ResultType>(
        /**
         * 获取task绑定的msg
         */
        protected val msg: BaseMessage) : AbsTask<ResultType>() {

    override fun cancel(immediately: Boolean) {
    }

    override fun onCancelled(byUser: Boolean) {
    }

    override fun onProgress(curr: Long, total: Long) {
        val callback = msg.getCallback() as MessageCallback<ResultType>
        callback.onProgress(curr, total)
    }

    /**
     * 获取task绑定的参数
     */
    protected val arguments: Bundle
        get() {
            return msg.arguments ?: Bundle()
        }

    override fun onSuccess(result: ResultType) {
        val callback = msg.getCallback() as MessageCallback<ResultType>
        callback.onSuccess(result)
    }


    override fun onError(exception: BaseException) {
        val callback = msg.getCallback()
        callback.onError(exception)
    }

    override fun onStarted() {
        val callback = msg.getCallback()
        callback.onStarted()
    }

    override fun onWaiting() {
        val callback = msg.getCallback()
        callback.onWaiting()
    }

    override fun onFinished(isSuccess: Boolean) {
        val callback = msg.getCallback()
        callback.onFinished(isSuccess)
    }

    fun update(curr: Long, total: Long) {
        CommonSdk.task().post {
            onProgress(curr, total)
        }
    }
}
