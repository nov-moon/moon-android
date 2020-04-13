@file:JvmName("LogUtil")

package com.meili.moon.sdk.log

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.ILogger.Level.*

/**
 * 本类已被弃用，请参见[Logcat]进行使用
 *
 * Created by imuto on 17/11/27.
 */
@Deprecated(message = "当前日志打印已被弃用", replaceWith = ReplaceWith("Logcat"))
object LogUtil {

    // 默认的日志Tag，如果不设置则为空
    var defaultTag = ""

    private var isDebug = CommonSdk.environment().isDebug()

    private var log: LoggerQueue = Logcat.log

    private const val mInvokeNextPath = "com.meili.moon.sdk.log.LogUtil"

    @JvmStatic
    fun isDebug(d: Boolean) {
        isDebug = d
        Logcat.config().enable = d
    }

    @JvmStatic
    fun d(any: Any?) {
        log.log(any, D, null, null, null, "$mInvokeNextPath.d")
    }

    @JvmStatic
    fun d(tag: String?, any: Any?) {
        log.log(any, D, tag, null, null, "$mInvokeNextPath.d")
    }

    @JvmStatic
    fun e(any: Any?) {
        log.log(any,
                E, null, null, null, "$mInvokeNextPath.e")
    }

    @JvmStatic
    fun e(tag: String, any: Any?) {
        log.log(any, E, tag, null, null, "$mInvokeNextPath.e")
    }

    @JvmStatic
    fun e(throwable: Throwable, any: Any?) {
        log.log(any, E, null, null, null, "$mInvokeNextPath.e")
        throwable.printStackTrace()
    }

    @JvmStatic
    fun e(tag: String, throwable: Throwable?, any: Any?) {
        log.log(any, E, tag, null, null, "$mInvokeNextPath.e")
        throwable?.printStackTrace()
    }

    @JvmStatic
    fun i(any: Any?) {
        log.log(any, I, null, null, null, "$mInvokeNextPath.i")
    }

    @JvmStatic
    fun i(tag: String, any: Any?) {
        log.log(any, I, tag, null, null, "$mInvokeNextPath.i")
    }

    @JvmStatic
    fun v(any: Any?) {
        log.log(any, V, null, null, null, "$mInvokeNextPath.v")
    }

    @JvmStatic
    fun v(tag: String, any: Any?) {
        log.log(any, V, tag, null, null, "$mInvokeNextPath.v")
    }

    @JvmStatic
    fun w(any: Any?) {
        log.log(any, W, null, null, null, "$mInvokeNextPath.w")
    }

    @JvmStatic
    fun w(tag: String, any: Any?) {
        log.log(any, W, tag, null, null, "$mInvokeNextPath.w")
    }

    @JvmStatic
    fun array(title: String, vararg message: Any?) {
    }

    @JvmStatic
    fun array(tag: String, title: String, vararg message: Any?) {
    }

    @JvmStatic
    fun json(json: Any?) {
        log.json(json, null, null, null, "$mInvokeNextPath.json")

    }

    @JvmStatic
    fun json(tag: String, json: Any?) {
        log.json(json, tag, null, null, "$mInvokeNextPath.json")
    }

    @JvmStatic
    fun xml(xml: String) {
    }

    @JvmStatic
    fun xml(tag: String, xml: String) {

    }
}
