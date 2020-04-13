package com.meili.moon.sdk.http

import com.meili.moon.sdk.common.*
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.common.CacheHandler
import com.meili.moon.sdk.http.exception.MockException
import com.meili.moon.sdk.http.impl.DefThrowableMessageProvider
import com.meili.moon.sdk.http.impl.HttpImpl
import com.meili.moon.sdk.http.impl.HttpRetryHandler
import com.meili.moon.sdk.http.loader.IResourceLoader
import com.meili.moon.sdk.http.loader.LoaderFactory
import com.meili.moon.sdk.http.loader.isForceMock
import com.meili.moon.sdk.http.util.RequestTrackerWrap
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.task.AbsTask
import java.util.concurrent.atomic.AtomicInteger

/**
 * 同步线程的http任务
 * Created by imuto on 17/12/5.
 */
class HttpTask<ResultType>(
        /**网络请求参数*/
        private val params: IHttpRequestParams,
        private val successCallback: SuccessLambda<ResultType> = null,
        private val errorCallback: ErrorLambda = null,
        private val waitingCallback: WaitingLambda = null,
        private val startCallback: StartLambda = null,
        private val progressCallback: ProgressLambda = null,
        private val cancelCallback: CancelLambda = null,
        private val finishCallback: FinishLambda = null,
        private val cacheCallback: CacheLambda = null
) : AbsTask<ResultType>() {

    private val mTracker: RequestTrackerWrap? =
            if (params is IRequestParams.IHttpAdvanceFeatures && params.getTracker() != null) {
                RequestTrackerWrap(params.getTracker())
            } else {
                null
            }

    private val progressHandler: Callback.ProgressCallback? =
            if (progressCallback != null) {
                ProgressHandlerPrivate(params)
            } else {
                null
            }
    private var loader: IResourceLoader<*>? = null

    /**当status的值大于本值，则表示他的缓存结果和服务器不同*/
    private val CACHE_INVOKE_STATUS_MAX = 1000
    /**标记status为服务器缓存和本地不同*/
    private val CACHE_INVOKE_STATUS_SAME = CACHE_INVOKE_STATUS_MAX + 1
    /**标记服务器数据和本地缓存是否相同，如果相同则不会回调第二次success和finish，为什么这么做，可以@youdong.jiang*/
    private var invokeStatus = AtomicInteger(0)

    override fun cancel(immediately: Boolean) {
        loader?.cancel(immediately)
    }

    override fun onWaiting() {
        mTracker?.onWaiting(params)
        waitingCallback?.invoke()
    }

    override fun onStarted() {
        mTracker?.onStart(params)
        startCallback?.invoke()
    }

    override fun doBackground() {
        params.init()

        LogUtil.d(params.toString())

        var progressVar: ProgressLambda = null
        if (progressHandler != null) {
            progressVar = { curr, total -> progressHandler.onProgress(curr, total) }
        }
        loader = LoaderFactory.getLoader<ResultType>(params, progressVar)
        mTracker?.onRequestCreate(params)

        val cacheHandler = if (params is CacheHandler && params.cacheable()) {
            params
        } else null

        cacheHandler?.tryUseCache(params, cacheCallback, loader, this)

        val available = loader?.validate() ?: false

        if (!available) {
            return
        }

        var exception: Exception? = null
        var retry = true
        val retryHandler = if (params is IRequestParams.IHttpAdvanceFeatures) {
            params.getRetryHandler() ?: HttpRetryHandler()
        } else {
            HttpRetryHandler()
        }
        while (retry) {
            try {
                retry = false

                if (params is IRequestParams.IHttpAdvanceFeatures) {
                    params.getRequestFilter()?.filter(params, true)
                }

                @Suppress("UNCHECKED_CAST")
                setResult(loader!!.load() as? ResultType)

                if (params is IRequestParams.IHttpAdvanceFeatures) {
                    params.getRequestFilter()?.filter(params, false)
                }

                val isSame2Cache = cacheHandler?.isSame2Cache(params) ?: false
                if (!isSame2Cache) {
                    invokeStatus.set(CACHE_INVOKE_STATUS_SAME)
                }

                cacheHandler?.saveCache(params, loader)
            } catch (e: Exception) {
                if (e is MockException && !params.isForceMock()) {
                    loader = LoaderFactory.getLoader<ResultType>(params, progressVar, false)
                    retry = true
                    continue
                }
                retry = retryHandler.retry(params, e) && !hasCancelled()
                if (!retry) exception = e

            }
        }
        if (exception != null && !hasCancelled()) {
            if (exception !is BaseException) {
                throw DefThrowableMessageProvider.onThrowable(exception)
            }
            throw exception
        }
    }

    override fun onSuccess(result: ResultType) {
        mTracker?.onSuccess(params)
        if (invokeStatus.get() > CACHE_INVOKE_STATUS_MAX || invokeStatus.getAndIncrement() <= 0) {
            successCallback?.invoke(result)
        }
    }

    override fun onError(exception: BaseException) {
        mTracker?.onError(exception, params)
        try {
            var paramStr = params.toString()
            paramStr = paramStr.replace("%", "_")
            LogUtil.e("请求错误，入参如下：$paramStr\n错误日志如下：")
        } catch (e: Exception) {
            e.printStackTrace()
        }
        exception.printStackTrace()
        errorCallback?.invoke(params.response.handleError(exception))
    }

    override fun onCancelled(byUser: Boolean) {
        mTracker?.onCancel(params)
        cancelCallback?.invoke(byUser)
    }

    override fun onFinished(isSuccess: Boolean) {
        mTracker?.onFinished(params)
        if (invokeStatus.get() > CACHE_INVOKE_STATUS_MAX || invokeStatus.getAndIncrement() <= 1) {
            finishCallback?.invoke(isSuccess)
        }
        HttpImpl.taskMap.remove(params)
    }

    override fun onProgress(curr: Long, total: Long) {
        progressCallback?.invoke(curr, total)
    }

    private inner class ProgressHandlerPrivate(params: IRequestParams) : Callback.ProgressCallback {
        private var progressSpacingTime =
                if (params is IRequestParams.IDownloadFeatures) {
                    params.getProgressSpacingTime()
                } else {
                    100
                }

        private var lastUpdateTime: Long = 0

        override fun onProgress(curr: Long, total: Long) {
            if (total < 0 || curr < 0 || curr > total) {
                return
            }
            val currTime = System.currentTimeMillis()
            if (currTime - lastUpdateTime >= progressSpacingTime) {
                lastUpdateTime = currTime
                (getProxy() ?: this@HttpTask).onProgress(curr, total)
            }
        }
    }
}