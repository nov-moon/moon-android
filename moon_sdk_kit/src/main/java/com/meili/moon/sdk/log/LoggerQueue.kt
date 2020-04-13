package com.meili.moon.sdk.log

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.meili.moon.sdk.util.foreachInverse

/**
 * 日志队列管理
 *
 * 接受日志打印器的注册和解注。
 * 自动根据loggerSubId去匹配管理的日志队列，优先使用匹配到的日志打印器进行处理
 * 提供默认的日志打印器，用来对不匹配的日志进行打印
 *
 * Created by imuto on 2018/5/3.
 */
object LoggerQueue : ILogger {

    override var defaultTag: String? = null

    override var id: String = ""

    /**日志打印器集合*/
    private var loggers: MutableMap<String, ILogger?> = mutableMapOf()

    /**默认的打印器*/
    private var defLogger = DefLogger("default_logger")

    override var enable: Boolean = true

    private val mNonMsg = "log is null"

    internal var traceCount = 1

    init {
        loggers[defLogger.id] = defLogger
    }

    /**
     * 注册一个日志对象，同时初始化他的preMethodCount
     */
    fun registerLogger(logger: ILogger) {
        loggers[logger.id] = logger
    }

    /**
     * 解注一个日志打印器
     */
    fun unregisterLogger(loggerId: String) {
        loggers.remove(loggerId)
    }

    /**
     * 获取指定的日志打印器
     */
    fun getLogger(loggerSubId: String?): ILogger? {
        if (TextUtils.isEmpty(loggerSubId)) {
            return null
        }
        return loggers[loggerSubId]
    }

    /**
     * 打印指定级别的日志
     */
    fun log(msg: Any?, level: ILogger.Level, tag: String?,
            headerInfo: String?, loggerSubId: String? = null, invokeNextPath: String?, traceCount: Int? = null) {
        if (!enable) return

        val fixedIndex = getFixedMethodIndex(invokeNextPath)

        print(loggerSubId, fixedIndex) {
            it.log(msg ?: mNonMsg,
                    level, tag, headerInfo, fixedIndex, traceCount ?: this.traceCount)
        }
    }

    /**
     * 打印指定级别的日志
     */
    override fun log(msg: Any?, level: ILogger.Level, tag: String?,
                     headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {

        if (!enable) return

        print(null, fixedMethodCount) {
            it.log(msg ?: mNonMsg,
                    level, tag, headerInfo, fixedMethodCount, traceCount ?: this.traceCount)
        }
    }

    /**
     * 打印指定级别的json日志
     */
    fun json(json: Any?, tag: String?, headerInfo: String?,
             loggerSubId: String? = null, invokeNextPath: String?, traceCount: Int? = null) {
        if (!enable) return

        val fixedIndex = getFixedMethodIndex(invokeNextPath)


        if (json == null) {
            print(loggerSubId, fixedIndex) { it.json(mNonMsg, tag, headerInfo, fixedIndex, traceCount ?: this.traceCount) }
            return
        }
        if (json is CharSequence) {
            print(loggerSubId, fixedIndex) { it.json(json.toString(), tag, headerInfo, fixedIndex, traceCount ?: this.traceCount) }
        } else {
            val m = JSON.toJSONString(json, SerializerFeature.WriteMapNullValue)
            print(loggerSubId, fixedIndex) { it.json(m, tag, headerInfo, fixedIndex, traceCount ?: this.traceCount) }
        }
    }

    /**
     * 打印指定级别的json日志
     */
    override fun json(json: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {
        if (!enable) return
        print(null, fixedMethodCount) { it.json(json, tag, headerInfo, fixedMethodCount, traceCount ?: this.traceCount) }
    }

    /**
     * 打印指定级别的xml日志
     */
    fun xml(xml: String, tag: String?, headerInfo: String?, loggerSubId: String? = null, fixedMethodCount: Int? = null, traceCount: Int?) {
        if (!enable) return
        print(loggerSubId, fixedMethodCount) { it.xml(xml, tag, headerInfo, fixedMethodCount, traceCount ?: this.traceCount) }
    }

    /**
     * 打印指定级别的xml日志
     */
    override fun xml(xml: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?, traceCount: Int?) {
        if (!enable) return
        print(null, fixedMethodCount) { it.xml(xml, tag, headerInfo, fixedMethodCount, traceCount ?: this.traceCount) }
    }

    private fun print(loggerSubId: String?, fixedMethodCount: Int?, function: (ILogger) -> Unit) {
        if (!enable) return

        try {
            val logId = if (TextUtils.isEmpty(loggerSubId)) {
                getLoggerId(2 + (fixedMethodCount ?: 0))
            } else loggerSubId

            val parentPackage = logId?.substringBeforeLast(".")
            if (parentPackage.isNullOrEmpty()) {
                function(defLogger)
                return
            }
            //使用查找子包的方式进行查找loggerKey，如果没有对应的，则使用默认logger
            val loggerKey = loggers.keys.find {
                parentPackage.contains(it) || logId == it
            }

            val logger = loggers[loggerKey]
            if (logger != null) {
                function(logger)
            } else {
                function(defLogger)
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun getLoggerId(index: Int): String {
        val stackTrace = Thread.currentThread().stackTrace
        val className = stackTrace[index].className
        return className.substringBeforeLast(".")
    }

    private fun getFixedMethodIndex(invokeNextPath: String?): Int {

        invokeNextPath ?: return 0

        val stackTrace = Thread.currentThread().stackTrace
        stackTrace.size.foreachInverse {
            val item = stackTrace[it]
            if (("${item.className}.${item.methodName}").contains(invokeNextPath)) {
                return it
            }
        }

        return 0
    }

}