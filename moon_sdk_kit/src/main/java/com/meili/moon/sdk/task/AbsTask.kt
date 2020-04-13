package com.meili.moon.sdk.task

import com.meili.moon.sdk.task.ITask.State
import java.util.concurrent.Executor

/**
 * task的虚拟实例
 * Created by imuto on 17/11/23.
 */
abstract class AbsTask<ResultType> : ITask<ResultType> {
    /**task的proxy实例*/
    private var taskProxy: AbsTask<ResultType>? = null

    /**task的当前状态值*/
    @Volatile private var state = State.IDLE

    /**task的结果集*/
    @Volatile private var result: ResultType? = null

    /**取消task是否是用户行为*/
    @Volatile private var cancelByUser: Boolean = true

    /**获取当前task的proxy*/
    fun getProxy(): AbsTask<ResultType>? = taskProxy

    /**设置当前task的proxy*/
    internal fun setProxy(proxy: AbsTask<ResultType>?) {
        taskProxy = proxy
    }

    /**结束自己*/
    protected fun cancelSelf() {
        cancelByUser = false
        cancel(true)
    }

    /**是否是用户取消*/
    internal fun isCancelByUser() = cancelByUser

    /**获取当前task的优先级*/
    internal open fun getPriority(): Priority? = null

    /**获取当前task的执行器*/
    internal open fun getExecutor(): Executor? = null

    fun setResult(result: ResultType?) {
        this.result = result
    }

    override fun getResult(): ResultType? = result

    override fun hasCancelled(): Boolean = state == State.CANCELLED

    override fun hasFinished(): Boolean = when (state) {
        State.CANCELLED, State.ERROR, State.SUCCESS ->
            true
        else ->
            false
    }

    internal fun setState(state: State) {
        this.state = state
    }

    override fun getState(): State = state
}