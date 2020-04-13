package com.meili.moon.sdk.common

/**
 * 反射错误
 * Created by imuto on 2019-06-13.
 */
class ReflectException @JvmOverloads constructor(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)