package com.meili.moon.sdk

import android.app.Application
import java.io.File

/**
 * sdk的运行环境
 * Created by imuto on 17/12/4.
 */
interface Environment {

    /**app的默认缓存路径*/
    fun appCacheDir(): File

    /**app的默认文件路径*/
    fun appDir(): File

    /**是否是开发模式*/
    fun isDebug(): Boolean

    /**当前的Application对象*/
    fun app(): Application
}