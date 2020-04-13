package com.meili.moon.sdk.exception

import com.meili.moon.sdk.common.BaseException

/**
 * 事件传递错误
 * Created by imuto on 2018/4/4.
 */
class EventsException @JvmOverloads constructor(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)