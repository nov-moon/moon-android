package com.meili.moon.sdk.http

import com.meili.moon.sdk.common.BaseException

/**
 * Created by imuto on 2018/12/5.
 */
interface IThrowableProvider {
    /**错误信息的提供者*/
    fun onThrowable(throwable: Throwable): BaseException
}
