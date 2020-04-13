package com.meili.moon.sdk.exception

import com.meili.moon.sdk.common.BaseException

/**
 * Json的错误类
 * Created by imuto on 17/11/23.
 */
class JsonException(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)