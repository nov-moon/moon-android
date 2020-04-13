package com.meili.moon.sdk.util

import android.os.Looper
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.LogUtil
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * 用来存放app相关工具类
 * Created by imuto on 2019-08-06.
 */

/** 当前代码调用是否处于主进程  */
fun isMainProcess(): Boolean {
    val pid = android.os.Process.myPid()

    // 通过进程的虚拟文件读取
    var fis: FileInputStream? = null
    try {
        val file = File("/proc/$pid/cmdline")
        if (file.exists()) {
            fis = FileInputStream(file)
            val cmdLine = fis.readText().trim { it <= ' ' }
            return CommonSdk.app().packageName == cmdLine
        }
    } catch (ex: IOException) {
        LogUtil.e("isMainProcess", ex.message)

    } finally {
        closeQuietly(fis)
    }

    return false
}

/**当前代码调用是否处于主线程*/
fun isMainThread(): Boolean {
    return Thread.currentThread() == Looper.getMainLooper().thread
}
