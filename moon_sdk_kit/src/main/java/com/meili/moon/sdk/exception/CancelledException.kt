package com.meili.moon.sdk.exception

import com.meili.moon.sdk.common.BaseException

/**
 * 取消操作的exception
 * Created by imuto on 17/11/23.
 */
class CancelledException(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)