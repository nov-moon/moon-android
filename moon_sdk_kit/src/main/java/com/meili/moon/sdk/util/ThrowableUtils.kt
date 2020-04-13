package com.meili.moon.sdk.util

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.log

/**
 * 错误的工具类
 * Created by imuto on 2018/4/4.
 */

/**当debug模式的时候会抛出错误，否则只用日志系统打印[throwable]的message信息*/
fun throwOnDebug(throwable: Throwable?) {
    if (!CommonSdk.environment().isDebug() || throwable == null) {
        if (throwable?.message != null) {
            LogUtil.e(throwable.message)
        }
        return
    }

    throw throwable
}


/**当debug模式的时候会抛出错误，否则只用日志系统打印[msg]信息*/
fun throwOnDebug(msg: String, code: Int = 0) {
    if (!CommonSdk.environment().isDebug()) {
        msg.log()
        return
    }

    throw BaseException(code, msg)
}