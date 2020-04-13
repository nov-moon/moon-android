package com.meili.moon.sdk.task

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * 优先级线程池
 * Created by imuto on 17/11/24.
 */
class PriorityExecutor(poolSize: Int = CORE_POOL_SIZE, fifo: Boolean = true) : Executor {

    private companion object {
        /**工作线程数*/
        const val CORE_POOL_SIZE = 5
        /**最大线程数*/
        const val MAXIMUM_POOL_SIZE = 256
        /**活动线程数*/
        const val KEEP_ALIVE_TIME = 1L
        /**线程池ID生成器*/
        val SEQ_SEED = AtomicLong(0)

        /**先入先出*/
        val FIFO_CMP = Comparator<Runnable> { left, right ->
            if (left is PriorityRunnable && right is PriorityRunnable) {
                val result = left.priority.ordinal - right.priority.ordinal
                if (result == 0) ((left.SEQ - right.SEQ).toInt()) else 0
            } else {
                0
            }
        }

        /**先入后出队列*/
        val FILO_CMP = Comparator<Runnable> { lhs, rhs ->
            if (lhs is PriorityRunnable && rhs is PriorityRunnable) {
                val result = lhs.priority.ordinal - rhs.priority.ordinal
                if (result == 0) (rhs.SEQ - lhs.SEQ).toInt() else result
            } else {
                0
            }
        }

        /**线程工厂*/
        val THREAD_FACTORY = object : ThreadFactory {
            private val mCount = AtomicInteger(1)
            override fun newThread(r: Runnable?): Thread {
                return Thread(r, "MeiliTID#" + mCount.getAndIncrement())
            }
        }
    }

    private val mExecutor: ThreadPoolExecutor

    init {
        val workQueue = PriorityBlockingQueue<Runnable>(MAXIMUM_POOL_SIZE, if (fifo) FIFO_CMP else FILO_CMP)

        mExecutor = ThreadPoolExecutor(
                poolSize, MAXIMUM_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, THREAD_FACTORY)

        Runtime.getRuntime().availableProcessors()
    }

    /**获取核心线程数*/
    fun getCoreSize() = mExecutor.corePoolSize


    /**设置线程池核心线程数*/
    fun setCoreSize(size: Int) {
        if (size > 0) {
            mExecutor.corePoolSize = size
        }
    }

    /**线程池是否很忙*/
    fun isBusy() = mExecutor.activeCount >= mExecutor.corePoolSize

    /**获取执行器*/
    fun getExecutor() = mExecutor

    override fun execute(command: Runnable?) {
        if (command is PriorityRunnable) {
            command.SEQ = SEQ_SEED.getAndIncrement()
        }
        mExecutor.execute(command)
    }
}