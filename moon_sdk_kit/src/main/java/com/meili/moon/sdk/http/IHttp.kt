package com.meili.moon.sdk.http

import com.meili.moon.sdk.common.*
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.task.ITask

/**
 * Http请求的标准接口
 * Created by imuto on 17/12/5.
 */
interface IHttp {
    /**发起一个get请求，结果在callback中进行回调*/
    fun <T> get(params: IHttpRequestParams, callback: Callback.IHttpCallback<T>): ITask<T>

    /**发起一个get请求，结果在自定义的lambda表达式中回调*/
    fun <T> get(params: IHttpRequestParams,
                success: SuccessLambda<T> = null,
                error: ErrorLambda = null,
                start: StartLambda = null,
                progress: ProgressLambda = null,
                cancel: CancelLambda = null,
                finish: FinishLambda = null): ITask<T>

    /**发起一个同步get请求，获取指定类型的结果*/
    @Throws(BaseException::class)
    fun <T> getSync(params: IHttpRequestParams, resultClazz: Class<T>): T?

    /**发起一个同步get请求，获取指定类型的List结果*/
    @Throws(BaseException::class)
    fun <T> getSyncForList(params: IHttpRequestParams, resultClazz: Class<T>): List<T>?

    /**发起一个post请求，结果在callback中进行回调*/
    fun <T> post(params: IHttpRequestParams, callback: Callback.IHttpCallback<T>): ITask<T>

    /**发起一个post请求，结果在自定义的lambda表达式中回调*/
    fun <T> post(params: IHttpRequestParams,
                 success: SuccessLambda<T> = null,
                 error: ErrorLambda = null,
                 start: StartLambda = null,
                 progress: ProgressLambda = null,
                 cancel: CancelLambda = null,
                 finish: FinishLambda = null): ITask<T>

    /**发起一个同步post请求，获取指定类型的结果*/
    fun <T> postSync(params: IHttpRequestParams, resultClazz: Class<T>): T?

    /**发起一个同步post请求，获取指定类型的List结果*/
    fun <T> postSyncForList(params: IHttpRequestParams, resultClazz: Class<T>): List<T>?


    /**发起一个异步指定请求方法的请求，结果在callback中进行回调*/
    fun <T> request(method: HttpMethod, params: IHttpRequestParams, callback: Callback.IHttpCallback<T>): ITask<T>

    /**发起一个异步指定请求方法的请求，结果在自定义的lambda表达式中回调*/
    fun <T> request(method: HttpMethod, params: IHttpRequestParams,
                    success: SuccessLambda<T> = null,
                    error: ErrorLambda = null,
                    start: StartLambda = null,
                    progress: ProgressLambda = null,
                    cancel: CancelLambda = null,
                    finish: FinishLambda = null): ITask<T>

    /**发起一个异步指定请求方法的请求，获取指定类型的结果*/
    fun <T> requestSync(method: HttpMethod, params: IHttpRequestParams, resultClazz: Class<T>): T?

    /**发起一个异步指定请求方法的请求，获取指定类型的List结果*/
    fun <T> requestSyncForList(method: HttpMethod, params: IHttpRequestParams, resultClazz: Class<T>): List<T>?

    /**配置网络请求的通用配置*/
    fun config(config: IConfig)

    /**取消当前所有未完成的请求*/
    fun cancelAll()

    /**当有相同request发起时的策略方式*/
    enum class SameRequestStrategy {
        /**当有相同两个request的时候，取消前一个request*/
        CANCEL_PRE_REQUEST,
        /**当有相同两个request的时候，取消当前发起的request*/
        CANCEL_CURR_REQUEST
    }
}