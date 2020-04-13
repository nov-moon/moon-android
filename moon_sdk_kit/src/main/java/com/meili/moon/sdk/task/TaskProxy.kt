package com.meili.moon.sdk.task

import android.os.Handler
import android.os.Looper
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.exception.CancelledException
import com.meili.moon.sdk.task.ITask.State.*
import java.util.concurrent.Executor

/**
 * task的执行顺序控制类
 * Created by imuto on 17/11/23.
 */
class TaskProxy<ResultType>(private val task: AbsTask<ResultType>) : AbsTask<ResultType>() {

    private val executor: Executor
        get() = task.getExecutor() ?: DEFAULT_EXECUTOR

    private companion object {
        val DEFAULT_EXECUTOR = PriorityExecutor()
        val MSG_HANDLER = Handler(Looper.getMainLooper())
        val MSG_WHAT_BASE = 1000000000
        val MSG_WHAT_ON_FINISHED = MSG_WHAT_BASE + 1
        val MSG_WHAT_ON_PROGRESS = MSG_WHAT_ON_FINISHED + 1
    }

    init {
        task.setProxy(this)
        setProxy(null)
    }

    override fun doBackground() {
        onWaiting()
        executor.execute(PriorityRunnable(task.getPriority(), Runnable {
            var isSuccess = true
            try {
                //检查task是否已经被取消
                val checkCancel = {
                    if (hasCancelled()) {
                        throw CancelledException(msg = "task已经取消任务")
                    }
                }

                checkCancel()

                onStarted()

                checkCancel()

                task.doBackground()

                setResult(task.getResult())

                checkCancel()

                onSuccess(getResult()!!)
            } catch (e: CancelledException) {
                isSuccess = false
                onCancelled(isCancelByUser())
            } catch (e: Exception) {
                isSuccess = false
                if (e is BaseException) {
                    onError(e)
                } else {
                    onError(BaseException(msg = e.message, cause = e))
                }
            } finally {
                onFinished(isSuccess)
            }
        }))
    }

    @Synchronized
    override fun cancel(immediately: Boolean) {
        if (hasFinished()) {
            return
        }
        setState(ITask.State.CANCELLED)
        try {
            task.cancel(immediately)
        } catch (e: Exception) {
        }
        if (immediately) {
            onCancelled(isCancelByUser())
        }
    }

    override fun onStarted() {
        sendMsg(STARTED)
    }

    override fun onWaiting() {
        sendMsg(WAITING)
    }

    override fun onSuccess(result: ResultType) {
        sendMsg(SUCCESS, result = result)
    }

    override fun onError(exception: BaseException) {
        sendMsg(ERROR, exception = exception)
    }

    override fun onCancelled(byUser: Boolean) {
        sendMsg(CANCELLED, bl = byUser)
    }

    override fun onFinished(isSuccess: Boolean) {
        sendMsg(what = MSG_WHAT_ON_FINISHED, bl = isSuccess)
    }

    override fun onProgress(curr: Long, total: Long) {
        sendMsg(what = MSG_WHAT_ON_PROGRESS, arg1 = curr, arg2 = total)
    }

    private fun sendMsg(state: ITask.State? = null,
                        what: Int? = state?.value(),
                        arg1: Long = 0,
                        arg2: Long = 0,
                        bl: Boolean = true,
                        exception: BaseException? = null,
                        result: ResultType? = null) {
        if (state != null) {
            setState(state)
        }
        MSG_HANDLER.post(MessageRunnable(what, arg1, arg2, bl, exception, result))
    }

    private inner class MessageRunnable(
            val what: Int? = 0,
            val arg1: Long = 0,
            val arg2: Long = 0,
            val bl: Boolean = true,
            val exception: BaseException? = null,
            val result: ResultType? = null
    ) : Runnable {
        override fun run() {
            if (hasCancelled() && what != CANCELLED.value()) {
                return
            }
            var errorCode = 0
            try {
                when (what) {
                    WAITING.value() -> {
                        errorCode = 1
                        task.onWaiting()
                    }
                    STARTED.value() -> {
                        errorCode = 2
                        task.onStarted()
                    }
                    SUCCESS.value() -> {
                        errorCode = 3
                        task.onSuccess(result!!)
                    }
                    ERROR.value() -> {
                        errorCode = 4
                        task.onError(exception!!)
                    }
                    CANCELLED.value() -> {
                        errorCode = 5
                        task.onCancelled(bl)
                    }
                    MSG_WHAT_ON_PROGRESS -> {
                        errorCode = 6
                        task.onProgress(arg1, arg2)
                    }
                    MSG_WHAT_ON_FINISHED -> {
                        errorCode = 7
                        task.onFinished(bl)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                val throwable = BaseException(errorCode, "数据处理错误($errorCode)", e)
                if (what != ERROR.value()) {
                    onError(throwable)
                } else if (CommonSdk.environment().isDebug()) {
                    throw throwable
                }
            }
        }
    }
}