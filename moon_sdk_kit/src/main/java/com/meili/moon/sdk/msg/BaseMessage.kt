package com.meili.moon.sdk.msg

import android.os.Bundle
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.common.Cancelable
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * task的Message对象
 *
 *
 * Created by imuto on 17/2/14.
 */
class BaseMessage(val msgId: String) : Cancelable, Cloneable {

    var arguments: Bundle? = null
    private var callback: MessageCallback<Any>? = null

    private var isSingleTask = false

    private val mCallbackProxy = object : MessageCallback<Any>() {

        override fun onFinished(isSuccess: Boolean) {
            mMessageQueue.remove(msgId)
            callback?.onFinished(isSuccess)
        }

        override fun onError(exception: BaseException) {
            callback?.onError(exception)
        }

        override fun onStarted() {
            callback?.onStarted()
        }

        override fun onSuccess(result: Any) {
            callback?.onSuccess(result)
        }

        override fun onProgress(curr: Long, total: Long) {
            callback?.onProgress(curr, total)
        }
    }

    /**
     * 设置启动的消息是否唯一，默认唯一
     */
    fun setSingleTask(isSingleTask: Boolean) {
        this.isSingleTask = isSingleTask
    }

    fun setCallback(callback: MessageCallback<*>) {
        this.callback = callback as MessageCallback<Any>
    }

    fun getCallback(): MessageCallback<*> {
        return mCallbackProxy
    }

    fun send(): Cancelable? {
        if (mMessageQueue.contains(msgId) && isSingleTask) {
            return null
        }
        mMessageQueue.add(msgId)
        return MessageRegistry.start(this)
    }

    override fun cancel(immediately: Boolean) {

    }

    override fun hasCancelled(): Boolean {
        return false
    }

    companion object {
        private val mMessageQueue = ConcurrentLinkedQueue<String>()
    }
}
