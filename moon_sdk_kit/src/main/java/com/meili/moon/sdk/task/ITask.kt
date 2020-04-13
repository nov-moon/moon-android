package com.meili.moon.sdk.task

import com.meili.moon.sdk.common.Callback.*
import com.meili.moon.sdk.common.Cancelable

/**
 * task标准
 * Created by imuto on 17/11/23.
 */
interface ITask<ResultType> : WaitingCallback, StartedCallback,
        ProgressCallback, IHttpCallback<ResultType>, CancelCallback, Cancelable {

    /**task任务执行*/
    @Throws(Throwable::class)
    fun doBackground()

    /**任务是否已经结束*/
    fun hasFinished(): Boolean

    /**获取当前task的状态*/
    fun getState(): State

    /**获取task的结果*/
    fun getResult(): ResultType?

    /**一个task所拥有的标准状态*/
    enum class State constructor(private val value: Int) {
        /**原始状态*/
        IDLE(0),
        /**开始等待*/
        WAITING(1),
        /**已经开始*/
        STARTED(2),
        /**已经成功*/
        SUCCESS(3),
        /**已经取消*/
        CANCELLED(4),
        /**已经错误*/
        ERROR(5);

        fun value(): Int = value
    }
}