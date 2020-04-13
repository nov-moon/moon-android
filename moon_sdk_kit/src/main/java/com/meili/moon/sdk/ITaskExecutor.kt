package com.meili.moon.sdk

import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.task.AbsTask

/**
 * 任务执行器的标准接口
 * Created by imuto on 17/11/24.
 */
interface ITaskExecutor {
    /**post一个runnable到主线程，并执行。可以设置延迟时间*/
    fun post(runnable: Runnable, delayMillis: Long = 0)

    /**post一个runnable到主线程，并执行。可以设置延迟时间*/
    fun post(delayMillis: Long = 0, runnable: () -> Unit)

    /**post一个runnable到主线程，并执行*/
    fun post(runnable: Runnable)

    /**post一个lambda到主线程，并执行*/
    fun post(runnable: () -> Unit)

    /**
     * 指定一个[postId]，如果有相同id的post请求，则会忽略当前post，
     * 至到此id的post被执行完成才能进行下一个此id的post，中间的post调用都会被忽略。
     *
     * 例如：
     * 第一次调用postOnce(1001){}：加入到post队列等待执行
     * 第二次调用postOnce(1001){}：由于第一次还没有执行完成，这次忽略
     * 第三次调用postOnce(1001){}：由于第一次还没有执行完成，这次忽略
     * 第三次调用postOnce(1001){}：由于第一次还没有执行完成，这次忽略
     *
     * 第一次调用postOnce执行完成
     *
     * 第四次调用postOnce(1001){}：加入到post队列等待执行
     *
     * 如上示例，只有第一次和第四次的postOnce才会被执行
     *
     */
    fun postOnce(postId: Long, delayMillis: Long = 0, runnable: () -> Unit)

    /**移除一个未执行的runnable*/
    fun removeCallbacks(runnable: Runnable)

    fun <T> start(task: AbsTask<T>): AbsTask<T>

    @Throws(BaseException::class)
    fun <T> startSync(task: AbsTask<T>): T?
}