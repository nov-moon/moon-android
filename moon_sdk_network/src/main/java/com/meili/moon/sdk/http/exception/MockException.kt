package com.meili.moon.sdk.http.exception

import com.meili.moon.sdk.common.BaseException

/**
 * 默认的Mock数据错误
 * Created by imuto on 17/11/28.
 */
class MockException(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)