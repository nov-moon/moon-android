package com.meili.moon.sdk.http.util

import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IRequestTracker
import com.meili.moon.sdk.http.exception.HttpException

/**
 * 请求追踪器的包装类
 * Created by imuto on 17/3/2.
 */
/* package */ internal class RequestTrackerWrap(private val realTracker: IRequestTracker?) : IRequestTracker {

    init {
        if (realTracker != null && realTracker is RequestTrackerWrap) {
            throw HttpException(msg = "RequestTracker对象不能为RequestTrackerWrap的实例")
        }
    }

    override fun onStart(params: IRequestParams) {
        try {
            realTracker?.onStart(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onRequestCreate(params: IRequestParams) {
        try {
            realTracker?.onRequestCreate(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onWaiting(params: IRequestParams) {
        try {
            realTracker?.onWaiting(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCache(params: IRequestParams) {
        try {
            realTracker?.onCache(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccess(params: IRequestParams) {
        try {
            realTracker?.onSuccess(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onError(ex: Throwable, params: IRequestParams) {
        try {
            realTracker?.onError(ex, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCancel(params: IRequestParams) {
        try {
            realTracker?.onCancel(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onFinished(params: IRequestParams) {
        try {
            realTracker?.onFinished(params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
