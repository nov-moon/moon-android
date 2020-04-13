package com.meili.moon.sdk.log

import android.text.TextUtils
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.meili.moon.sdk.log.DefLogger.Companion.end
import com.meili.moon.sdk.log.DefLogger.Companion.top
import com.meili.moon.sdk.util.isArray
import com.meili.moon.sdk.util.isList
import com.meili.moon.sdk.util.isMap

/**
 * 默认的日志打印器
 *
 * 提供标准的[ILogger]规定功能
 *
 * 为了让打印内容更容易区分，做了如下打印内容优化：
 * 1. 会以[top]作为开头，并且在top中插入自定义tag，强化开头，[end]作为结尾
 * 2. 打印的第二行会输出 '调用日志打印的代码位置'，方便代码回溯
 * 3. 提供了headerInfo作为扩展信息，方便对日志添加扩展信息
 * 4. 如果打印对象为集合或者map，则会采用json形式进行打印，方便集合等内容打印
 * 5. 打印内容如果本身有换行，则使用两行进行打印，方便对齐打印内容
 *
 * 根据上述第4条我们可以知道，日志打印对集合和map做了特殊功能处理，这里考虑为什么不对所有对象做json处理呢？
 * 我们考虑到：1.你有可能想定制自己的toString进行日志内容管理。2. 你有可能是想看他的内存地址。3. 过于消耗性能。
 * 所以我们只提供了针对特殊情况的特殊处理
 */
class DefLogger(override var id: String) : ILogger {

    override var defaultTag: String? = ""

    override var enable: Boolean = true

    override fun log(msg: Any?, level: ILogger.Level, tag: String?,
                     headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {
        logInner(msg, tag, level, headerInfo, 5 + (fixedMethodCount ?: 0), traceCount)
    }

    override fun json(json: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {
        val jo = JSON.parseObject(json)
        val result = JSON.toJSONString(jo,
                SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue)

        logInner(result, tag, ILogger.Level.D, headerInfo, 5 + (fixedMethodCount ?: 0), traceCount)
    }

    override fun xml(xml: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {
    }

    private fun logInner(msg: Any?, tag: String?, level: ILogger.Level, headerInfo: String?, offset: Int, count: Int?) {
        if (!enable) {
            return
        }

        val log = fixLog(msg)

        val t = fixTag(tag)

        // 打印一个空行
        printLog(level, t, split)

        // 打印开始分割线
        val topTag = if (defaultTag.isNullOrEmpty()) "Start" else defaultTag
        printLog(level, t, String.format(top, topTag))

        // 打印头部扩展信息
        printLog(level, t, headerInfo)

        if (!TextUtils.isEmpty(headerInfo)) {
            printLog(level, t, split)
        }

        // 打印堆栈信息
        printStackInfo(level, t, offset, count)

        // 打印空行
        printLog(level, t, split)

        // 打印日志
        printLog(level, t, log)

        // 打印结束分割线
        printLog(level, t, end)

        // 打印空行
        printLog(level, t, split)
    }

    private fun printLog(level: ILogger.Level, tag: String, log: String?) {
        if (log == null) {
            return
        }

        log.split("\n").forEach {
            print(level, tag, it)
        }
    }

    private fun print(level: ILogger.Level, tag: String, log: String) {
        val levelAndroid = when (level) {
            ILogger.Level.D -> {
                Log.DEBUG
            }
            ILogger.Level.E -> {
                Log.ERROR
            }
            ILogger.Level.I -> {
                Log.INFO
            }
            ILogger.Level.W -> {
                Log.WARN
            }
            ILogger.Level.V -> {
                Log.VERBOSE
            }
        }
        Log.println(levelAndroid, tag, log)
    }

    private fun printStackInfo(level: ILogger.Level, tag: String, offset: Int, count: Int?) {

        if (count == null || count < 1) {
            return
        }

        val traceSb = StringBuilder(traceSpace)
        traceSb.append(traceSpace)

        val stackTrace = Thread.currentThread().stackTrace

        var forEachCount = count - 1
        val maxCount = stackTrace.size - (offset + 1) - 1
        if (forEachCount > maxCount) {
            forEachCount = maxCount
        }

        (forEachCount downTo 0).forEach {
            val index = offset + 1 + it
            if (index < 0 || stackTrace.size <= index) {
                return@forEach
            }
            val trace = stackTrace[index]
            val logAt = "${trace.className.substringAfterLast(".")}.${trace.methodName} " +
                    "(${trace.fileName}:${trace.lineNumber})"

            traceSb.append(traceSpace)

            if (it == forEachCount) {
                printLog(level, tag, "from $logAt")
            } else {
                printLog(level, tag, traceSb.toString() + logAt)
            }
        }
    }

    private fun fixTag(tag: String?): String {
        return if (TextUtils.isEmpty(tag)) {
            if (defaultTag.isNullOrEmpty()) {
                id
            } else defaultTag!!
        } else tag!!
    }

    private fun fixLog(msg: Any?): String {

        return if (msg == null) {
            "log content is null"
        } else {
            if (msg is CharSequence && msg.isNotEmpty()) {
                return msg.toString()
            }
            val kClass = msg::class
            val m = if (kClass.isArray || kClass.isList || kClass.isMap) {
                JSON.toJSONString(msg,
                        SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue)
            } else msg.toString()

            if (TextUtils.isEmpty(m)) {
                "log content is empty"
            } else m
        }
    }

    private companion object {
        val top = "<------------------------------------- %s ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->"
        val split = " "
        val end = "--------------------------------------- End --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------->"
        val traceSpace = "  "
    }
}