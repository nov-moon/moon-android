package com.meili.moon.sdk.demo2

import com.meili.moon.sdk.log.Logcat

/**
 * Created by imuto on 2019-07-19.
 */
object TestLog {

    init {
        Logcat.register(this)
    }

    fun log() {
        log1(Logcat.getLogInvokeInfo())
    }

    fun log1(headerInfo: String?) {
        Logcat.d("TestLog.log()", headerInfo = headerInfo)
    }

}