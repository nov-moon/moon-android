package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.common.Cancelable
import com.meili.moon.sdk.http.IHttpResponse
import com.meili.moon.sdk.http.IResponseParser

/**
 * 同步的资源加载器标准
 * Created by imuto on 17/11/30.
 */
interface IResourceLoader<out ResultType> : Cancelable {

    /**验证当前loader是否可用*/
    fun validate(): Boolean

    /**设置loader的进度回调, lambda：第一个参数为当前进度，第二个参数为总大小*/
    var progressHandler: ((Long, Long) -> Unit)?

    var parser: IResponseParser?

    var response: IHttpResponse?

    /**加载资源并返回结果*/
    fun load(): ResultType

    /**当前loader是否正在加载*/
    fun isLoading(): Boolean

    fun progress(curr: Long, total: Long) {
        progressHandler?.invoke(curr, total)
    }
}