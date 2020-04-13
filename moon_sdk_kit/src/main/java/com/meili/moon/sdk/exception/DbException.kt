package com.meili.moon.sdk.exception

import com.meili.moon.sdk.common.BaseException

/**
 * Created by imuto on 17/12/26.
 */
class DbException(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)