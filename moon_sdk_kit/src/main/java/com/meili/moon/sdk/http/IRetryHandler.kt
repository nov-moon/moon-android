package com.meili.moon.sdk.http

/**
 * 重试的处理接口
 * Created by imuto on 17/11/28.
 */
interface IRetryHandler<in ReqIns> {
    /**
     * 是否可以重试
     * @param requestInstance 请求重试的实体
     * @param throwable 失败时发生的错误
     * @return true可以重试，false不能重试
     */
    fun retry(requestInstance: ReqIns, throwable: Throwable): Boolean

    /**获取最大重试次数*/
    fun getMaxTimes(): Int

    /**获取当前重试次数*/
    fun getTimes(): Int
}