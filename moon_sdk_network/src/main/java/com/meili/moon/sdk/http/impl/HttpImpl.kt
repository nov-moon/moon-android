package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.IComponent
import com.meili.moon.sdk.common.*
import com.meili.moon.sdk.http.HttpMethod
import com.meili.moon.sdk.http.HttpTask
import com.meili.moon.sdk.http.IHttp
import com.meili.moon.sdk.task.ITask
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.jvm.reflect
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams as Param


/**
 * 发起http请求的实现类
 * Created by imuto on 17/12/5.
 */
object HttpImpl : IHttp, IComponent {

    val taskMap = ConcurrentHashMap<Param, ITask<*>>()
    private var strategy = IHttp.SameRequestStrategy.CANCEL_PRE_REQUEST

    override fun config(strategy: IHttp.SameRequestStrategy) {
        this.strategy = strategy
    }

    override fun cancelAll() {
        synchronized(taskMap) {
            taskMap.forEach {
                it.value.cancel(true)
            }
        }
    }

    override fun init(env: Environment) {
        ComponentsInstaller.installHttp(this, env)
    }

    override fun <T> get(params: Param, callback: Callback.IHttpCallback<T>): ITask<T> {
        params.method = HttpMethod.GET
        return call(params, callback)
    }

    override fun <T> get(params: Param,
                         success: SuccessLambda<T>,
                         error: ErrorLambda,
                         start: StartLambda,
                         progress: ProgressLambda,
                         cancel: CancelLambda,
                         finish: FinishLambda): ITask<T> {
        return request(HttpMethod.GET, params, success, error, start, progress, cancel, finish)
    }

    @Throws(BaseException::class)
    override fun <T> getSync(params: Param, resultClazz: Class<T>): T? {
        return requestSync(HttpMethod.GET, params, resultClazz)
    }

    @Throws(BaseException::class)
    override fun <T> getSyncForList(params: Param, resultClazz: Class<T>): List<T>? {
        return requestSyncForList(HttpMethod.GET, params, resultClazz)
    }

    override fun <T> post(params: Param, callback: Callback.IHttpCallback<T>): ITask<T> {
        params.method = HttpMethod.POST
        return call(params, callback)
    }

    override fun <T> post(params: Param,
                          success: SuccessLambda<T>,
                          error: ErrorLambda,
                          start: StartLambda,
                          progress: ProgressLambda,
                          cancel: CancelLambda,
                          finish: FinishLambda): ITask<T> {
        return request(HttpMethod.POST, params, success, error, start, progress, cancel, finish)
    }

    @Throws(BaseException::class)
    override fun <T> postSync(params: Param, resultClazz: Class<T>): T? {
        return requestSync(HttpMethod.POST, params, resultClazz)
    }

    @Throws(BaseException::class)
    override fun <T> postSyncForList(params: Param, resultClazz: Class<T>): List<T>? {
        return requestSyncForList(HttpMethod.POST, params, resultClazz)
    }

    override fun <T> request(method: HttpMethod, params: Param, callback: Callback.IHttpCallback<T>): ITask<T> {
        params.method = method
        return call(params, callback)
    }

    override fun <T> request(method: HttpMethod, params: Param,
                             success: SuccessLambda<T>,
                             error: ErrorLambda,
                             start: StartLambda,
                             progress: ProgressLambda,
                             cancel: CancelLambda,
                             finish: FinishLambda): ITask<T> {
        params.method = method
        return request(params, success, error, start, progress, cancel, finish)
    }

    private fun <T> request(params: Param,
                            success: SuccessLambda<T>,
                            error: ErrorLambda,
                            start: StartLambda,
                            progress: ProgressLambda,
                            cancel: CancelLambda,
                            finish: FinishLambda,
                            isFromCallback: Boolean = false): ITask<T> {
        val sameTask = taskMap[params]
        if (sameTask != null) {
            when (strategy) {
                IHttp.SameRequestStrategy.CANCEL_PRE_REQUEST -> {
                    sameTask.cancel(true)
                    taskMap.remove(params)
                }
                IHttp.SameRequestStrategy.CANCEL_CURR_REQUEST -> {
                    return sameTask as ITask<T>
                }
            }
        }

        if (!isFromCallback && success != null) {
            params.response.parseItemParamType(success.reflect())
        }
        val httpTask = HttpTask(params, success, error, null, start, progress, cancel, finish)
        val taskProxy = CommonSdk.task().start(httpTask)

        taskMap[params] = taskProxy

        return taskProxy
    }

    @Throws(BaseException::class)
    override fun <T> requestSync(method: HttpMethod, params: Param, resultClazz: Class<T>): T? {
        params.method = method
        params.response.itemKClass = resultClazz
        params.response.isListResult = false
        var result: T? = null
        val httpTask = HttpTask<T>(params, { result = it }, { throw it })
        CommonSdk.task().startSync(httpTask)
        return result
    }

    @Throws(BaseException::class)
    override fun <T> requestSyncForList(method: HttpMethod, params: Param, resultClazz: Class<T>): List<T>? {
        params.method = method
        params.response.itemKClass = resultClazz
        params.response.isListResult = true
        var result: List<T>? = null
        val httpTask = HttpTask<List<T>>(params, { result = it }, { throw it })

        CommonSdk.task().startSync(httpTask)
        return result
    }

    //关联callback，并回调指定类型的方法
    private fun <T> call(params: Param, callback: Callback.IHttpCallback<T>): ITask<T> {
        when (callback) {
            is Callback.Typed -> params.response.parseItemParamType(callback.typed)
            is Callback.KTyped -> params.response.parseItemParamType(callback.typed)
            else -> params.response.parseItemParamType(callback)
        }

        var start: (() -> Unit)? = null
        if (callback is Callback.StartedCallback) {
            start = { callback.onStarted() }
        }
        var progress: ((Long, Long) -> Unit)? = null
        if (callback is Callback.ProgressCallback) {
            progress = { curr, total -> callback.onProgress(curr, total) }
        }
        var cancel: ((Boolean) -> Unit)? = null
        if (callback is Callback.CancelCallback) {
            cancel = { callback.onCancelled(true) }
        }
        return request(params, { callback.onSuccess(it) }, { callback.onError(it) }, start, progress, cancel, { callback.onFinished(it) }, true)
    }
}