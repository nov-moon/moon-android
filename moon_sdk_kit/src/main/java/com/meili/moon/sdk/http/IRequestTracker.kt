package com.meili.moon.sdk.http

/**
 * 网络请求日志追踪接口
 * Created by imuto on 17/3/2.
 */
interface IRequestTracker {
    /**
     * 请求被等待，例如线程池已经到达最大数量
     */
    fun onWaiting(params: IRequestParams)

    /**
     * 启动线程
     */
    fun onStart(params: IRequestParams)

    /**
     * 请求网络的准备工作已经完成
     */
    fun onRequestCreate(params: IRequestParams)

    /**
     * 尝试使用缓存结果
     */
    fun onCache(params: IRequestParams)

    /**
     * 当请求成功
     */
    fun onSuccess(params: IRequestParams)

    /**
     * 当请求错误
     */
    fun onError(ex: Throwable, params: IRequestParams)

    /**
     * 当请求被手动cancel
     */
    fun onCancel(params: IRequestParams)

    /**
     * 当请求完成
     */
    fun onFinished(params: IRequestParams)
}
