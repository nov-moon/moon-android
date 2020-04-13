package com.meili.moon.sdk.page.exception

import com.meili.moon.sdk.common.BaseException

/**
 * 页面打开错误
 * Created by imuto on 2018/4/4.
 */
class StartPageException @JvmOverloads constructor(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)
class PageCallbackException @JvmOverloads constructor(code: Int = 0, msg: String? = null, cause: Throwable? = null) : BaseException(code, msg, cause)