package com.meili.moon.sdk.common

import android.app.Application
import android.content.pm.ApplicationInfo
import com.meili.moon.sdk.Environment

/**
 * app的运行环境
 * Created by imuto on 18/1/12.
 */
open class DefaultEnvironment(private val app: Application) : Environment {

    private val isDebug: Boolean

    init {
        val info = app.applicationInfo
        isDebug = if (info != null) {
            (info.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } else {
            false
        }
    }

    override fun app(): Application = app

    override fun appCacheDir() = app.cacheDir!!

    override fun appDir() = app.filesDir!!

    override fun isDebug() = isDebug
}