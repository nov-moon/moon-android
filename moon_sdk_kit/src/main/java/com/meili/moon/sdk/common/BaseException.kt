package com.meili.moon.sdk.common

/**
 * 错误的基类
 * Created by imuto on 17/11/23.
 */
open class BaseException : RuntimeException {
    val code: Int

    constructor(code: Int = 0, msg: String? = null, cause: Throwable? = null) : super(msg, cause) {
        this.code = code
    }

    constructor() : this(0, null, null)
    constructor(code: Int) : this(code, null, null)
    constructor(code: Int, msg: String) : this(code, msg, null)
    constructor(msg: String) : this(0, msg, null)
    constructor(cause: Throwable) : this(0, null, cause)
}