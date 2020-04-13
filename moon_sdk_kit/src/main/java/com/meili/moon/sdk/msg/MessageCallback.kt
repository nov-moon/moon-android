package com.meili.moon.sdk.msg


import com.meili.moon.sdk.common.Callback

/**
 * messageTask的回调callback
 *
 * Created by imuto on 17/2/14.
 */
open class MessageCallback<in Result> : Callback.SimpleCallback<Result>(), Callback.ProgressCallback {
    override fun onProgress(curr: Long, total: Long) {
    }
}
