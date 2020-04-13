package com.meili.moon.sdk.http.impl

import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.http.IThrowableProvider
import com.meili.moon.sdk.http.exception.DEF_COMMON_ERROR_CODE
import com.meili.moon.sdk.http.exception.DEF_COMMON_ERROR_TIP
import com.meili.moon.sdk.http.exception.EXCEPTION_PROVIDER_MAP
import com.meili.moon.sdk.http.exception.HttpException

/**
 * Created by imuto on 2018/12/5.
 */
object DefThrowableMessageProvider : IThrowableProvider {
    override fun onThrowable(throwable: Throwable): BaseException {
        val target = EXCEPTION_PROVIDER_MAP[throwable::class]
        if (target != null) {
            return HttpException(target.first, "${target.second}(${target.first})", throwable)
        }
        return HttpException(DEF_COMMON_ERROR_CODE, DEF_COMMON_ERROR_TIP, throwable)
    }
}