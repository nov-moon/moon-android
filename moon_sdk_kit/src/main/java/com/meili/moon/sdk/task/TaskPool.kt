package com.meili.moon.sdk.task

import com.meili.moon.sdk.IExecutorPool
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.setOnce
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference

/**
 * 线程池的管理对象
 * Created by imuto on 18/3/16.
 */
object TaskPool : IExecutorPool {

    private val mIOExecutor: AtomicReference<Executor> = AtomicReference()
    private val mDownloadExecutor: AtomicReference<Executor> = AtomicReference()
    private val mHttpExecutor: AtomicReference<Executor> = AtomicReference()
    private val mCommonExecutor: AtomicReference<Executor> = AtomicReference()

    private val coreSize = Runtime.getRuntime().availableProcessors()

    override fun io(): Executor {
        return get(mIOExecutor)
    }

    override fun download(): Executor {
        return get(mDownloadExecutor, 2)
    }

    override fun http(): Executor {
        return get(mHttpExecutor, fifo = false)
    }

    override fun common(): Executor {
        //cpu密集型任务，默认空闲一半核心给其他线程池调度
        var cs = coreSize
        if (coreSize > 2) cs /= 2

        return get(mCommonExecutor, cs)
    }

    private fun get(reference: AtomicReference<Executor>, coreSize: Int? = null, fifo: Boolean = true): Executor {
        if (reference.get() == null) {
            try {
                val executor = if (coreSize == null) PriorityExecutor(fifo = fifo) else PriorityExecutor(coreSize, fifo)
                reference.setOnce(executor)
            } catch (e: BaseException) {
                LogUtil.e(e)
            }
        }
        return reference.get()
    }

}