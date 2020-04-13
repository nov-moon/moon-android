package com.meili.moon.sdk.task

import android.os.Handler
import android.os.Looper
import com.meili.moon.sdk.ITaskExecutor
import com.meili.moon.sdk.common.BaseException
import java.util.concurrent.ConcurrentSkipListSet

/**
 * taskExecutor默认实现, 主要有三个功能：
 *
 * 1. post 启动一个ui线程的runnable
 *
 * 2. start 启动一个task
 *
 * 3. 管理注册MessageTask类型
 *
 * Created by imuto on 17/11/24.
 */
object DefTaskExecutor : ITaskExecutor {

    private var mHandler = Handler(Looper.getMainLooper())
    private val postIds = ConcurrentSkipListSet<Long>()

    override fun post(runnable: Runnable, delayMillis: Long) {
        mHandler.postDelayed(runnable, delayMillis)
    }

    override fun post(runnable: Runnable) {
        post(runnable, 0)
    }

    override fun post(delayMillis: Long, runnable: () -> Unit) {
        post(Runnable { runnable.invoke() }, delayMillis)
    }

    override fun post(runnable: () -> Unit) {
        post(0, runnable)
    }

    override fun postOnce(postId: Long, delayMillis: Long, runnable: () -> Unit) {
        if (postIds.contains(postId)) {
            return
        }
        postIds.add(postId)

        post(delayMillis) {
            runnable()
            postIds.remove(postId)
        }
    }

    override fun removeCallbacks(runnable: Runnable) {
        mHandler.removeCallbacks(runnable)
    }

    override fun <T> start(task: AbsTask<T>): AbsTask<T> {
        val proxy = task as? TaskProxy ?: TaskProxy(task)
        try {
            proxy.doBackground()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return proxy
    }

    override fun <T> startSync(task: AbsTask<T>): T? {
        task.onWaiting()
        task.onStarted()
        task.doBackground()
        val result = task.getResult()
        if (result == null) task.onError(BaseException(msg = "未知错误")) else task.onSuccess(result)
        return result
    }

}