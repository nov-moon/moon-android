package com.meili.moon.sdk

import java.util.concurrent.Executor


/**开始回调*/
typealias RunnableLambda = (() -> Unit)

/**
 * 线程池管理接口，提供各种常用线程池。
 *
 * 一个app只能有一个当前实例，用来管理整体线程
 *
 * 必须使用对应的线程池，便于线程统一管理
 *
 * Created by imuto on 18/3/15.
 */
interface IExecutorPool {
    /**用于本地IO操作的线程池*/
    fun io(): Executor

    /**用于文件下载上传的线程池，限于网速原因，默认线程池大小会设置的比较小*/
    fun download(): Executor

    /**用于api请求的线程池对象*/
    fun http(): Executor

    /**用于CPU密集型任务，例如对bitmap做处理*/
    fun common(): Executor
}