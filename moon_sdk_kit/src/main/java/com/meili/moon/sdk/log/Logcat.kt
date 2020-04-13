package com.meili.moon.sdk.log

import android.text.TextUtils
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.ILogger.Level.*
import com.meili.moon.sdk.log.LoggerQueue.enable
import com.meili.moon.sdk.util.foreachInverse

/**
 * 皓月日志的主入口
 *
 * Logcat提供了日志打印和打印器注册两类功能。
 *
 * 1. 日志打印
 *      提供了分别对应系统打印级别的5种日志级别（v、d、i、w、e）。同时还提供了常用的json格式数据打印。
 *      定义了xml格式的打印，但是暂时并没有实现
 * 2. 打印器注册
 *      为了提供不同lib库可以分别管理自己的库日志打印方式，以及是否需要打印。
 *      我们提供了[register]方法进行日志器注册，[unregister]方法进行日志器解注。
 *      我们为了区分不同的lib日志方，在注册过程中，会从传入对象上解析当前包名，作为日志器的id。
 *      我们大概的认为一个lib会有自己特定的主包名以及子包，所以在注册时需要你传入当前lib主包下的对象，或者包名
 *      而后面的日志打印会优先使用日志器Id匹配的对象进行处理，当然你也可以通过直接注册logger的方式自定义id
 *
 * 最佳使用方式：
 *  你可以直接使用本类作为日志打印的主要入口，当然我们更建议你使用扩展方法的方式进行日志打印。
 *  Logcat的日志打印：
 *      本类中提供的api是最全面的日志，可以定义所有提供的能力。例如：
 *      Logcat.d("hello")
 *      Logcat.d("hello", "tag")
 *      Logcat.d("hello", "tag", "关联来自之前的调用")
 *      Logcat.d("hello", "tag", "关联来自之前的调用", "某个指定的打印器")
 *
 *  扩展打印：
 *      我们提供了Logcat的扩展打印，扩展方法可以后两种实现：log("日志") 或者 T.log()。
 *      但是从使用方便程度考虑，我们选择了T.log()的方式。因为这种方式对你原有代码的入侵会更少，在任意对象上都快速插入日志
 *      例如你原来有一行代码如下：
 *      val user = UserInfoModel()
 *      你现在想看一下这个user的内容，你使用方式1进行实现需要改动代码如下：
 *      val user = log(UserInfoModel())
 *      或者
 *      val user = UserInfoModel()
 *      log(user)
 *      而使用方式2的话，就可以如下：
 *      val user = UserInfoModel().log()
 *      代码会更简便，方便对插入日志
 *
 * Created by imuto on 2019-07-17.
 */
object Logcat {

    private val config = Config()

    /**日志管理队列*/
    internal var log: LoggerQueue = LoggerQueue

    private const val mInvokeNextPath = "com.meili.moon.sdk.log.Logcat"

    init {
        log.enable = enable
    }

    fun config(): Config = config

    /**
     * 打印日志，级别：debug
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun d(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
          loggerSubId: String? = null) {
        log.log(msg, D, tag, headerInfo, loggerSubId, "$mInvokeNextPath.d", traceCount)
    }

    /**
     * 打印日志，级别：error
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun e(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
          loggerSubId: String? = null) {
        log.log(msg, E, tag, headerInfo, loggerSubId, "$mInvokeNextPath.e", traceCount)
    }

    /**
     * 打印日志，级别：info
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun i(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
          loggerSubId: String? = null) {
        log.log(msg, I, tag, headerInfo, loggerSubId, "$mInvokeNextPath.i", traceCount)
    }

    /**
     * 打印日志，级别：verbose
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun v(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
          loggerSubId: String? = null) {
        log.log(msg, V, tag, headerInfo, loggerSubId, "$mInvokeNextPath.v", traceCount)
    }

    /**
     * 打印日志，级别：warn
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun w(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
          loggerSubId: String? = null) {
        log.log(msg, W, tag, headerInfo, loggerSubId, "$mInvokeNextPath.w", traceCount)
    }

    /**
     * 以json格式打印日志，级别：debug
     *
     * 如果msg == null 则会打印："log is null"
     * 对日志内容的不同处理，可能会是不同的，因为真正的打印会分派到对应[loggerSubId]的打印器中。
     * 如果没有自定义打印器，则会使用[DefLogger]作为默认的日志打印器。
     *
     * [msg] 打印的日志信息
     * [tag] 日志的tag信息
     * [headerInfo] 日志的头信息
     * [loggerSubId] 指定的日志器id，默认使用上层调用的包路径
     */
    @JvmStatic
    @JvmOverloads
    fun json(msg: Any?, tag: String? = null, traceCount: Int? = null, headerInfo: String? = null,
             loggerSubId: String? = null) {
        log.json(msg, tag, headerInfo, loggerSubId, "$mInvokeNextPath.json", traceCount)
    }

//
//    @JvmStatic
//    internal fun xml(msg: Any, tag: String? = null, headerInfo: String? = null,
//            loggerSubId: String? = null, fixedMethodIndex: Int? = null, fixedLoggerIdIndex: Int? = null) {
////        log.xml(any ?: "obj = null", tag, loggerSubId)
//    }

    /**
     * 获取当前调用位置的日志头信息。
     *
     * 获取到的格式如下：类名 + . + 调用方法名 + ( + 文件名称 + : + 行号)。
     * 例如：MainActivity$onCreate$5.onClick (MainActivity.kt:30)
     *
     * 一般用来获取代码调用的日志格式信息，在打印日志是进行头信息设置
     *
     * [fixedIndex] 修复的方法堆栈的调用index，默认只取调用此方法的位置
     */
    fun getLogInvokeInfo(fixedIndex: Int = 0): String {
        val trace = Thread.currentThread().stackTrace[4 + fixedIndex]
        return "${trace.className.substringAfterLast(".")}.${trace.methodName} " +
                "(${trace.fileName}:${trace.lineNumber})"
    }

    /**
     * 获取当前调用位置的日志头信息。
     *
     * 获取到的格式如下：类名 + . + 调用方法名 + ( + 文件名称 + : + 行号)。
     * 例如：MainActivity$onCreate$5.onClick (MainActivity.kt:30)
     *
     * 一般用来获取代码调用的日志格式信息，在打印日志是进行头信息设置
     *
     * [invokePath] 目标方法的调用路径，例如：com.meili.moon.sdk.log.Logcat.d，不要有方法的括号
     */
    fun getLogInvokeInfo(invokePath: String): String? {
        val stackTrace = Thread.currentThread().stackTrace
        stackTrace.size.foreachInverse {
            val trace = stackTrace[it]
            if (("${trace.className}.${trace.methodName}").contains(invokePath)) {
                return "${trace.className.substringAfterLast(".")}.${trace.methodName} " +
                        "(${trace.fileName}:${trace.lineNumber})"
            }
        }

        return null
    }

    private fun getLoggerId(entry: Any): String {
        return if (entry is String) {
            entry
        } else {
            var result = entry::class.qualifiedName!!.substringBeforeLast(".")
            if (result == "impl") {
                val split = entry::class.qualifiedName!!.split("\\.")
                result = split[split.size - 3]
            }
            result
        }
    }

    /**
     * 对log进行配置
     */
    class Config {
        /**设置日志打印是否可用，默认根据当前环境是否可debug确定*/
        var enable = CommonSdk.environment().isDebug()
            set(value) {
                field = value
                log.enable = value
            }

        /**
         * 设置全局log中打印trace的数量，默认为1
         *
         * 每个方法都可以自己特殊设置自己的打印trace数量
         */
        var traceCount
            get() = log.traceCount
            set(value) {
                log.traceCount = value
            }

        /**
         * 注册一个logger
         *
         * 入参[entry]支持三种类型：主包下的对象、String类型的主包名、ILogger对象
         *
         * 当为前两种类型时会自动使用[DefLogger]作为日志打印器，使用入参[entry]的包名作为打印器的id。
         * 后续打印只有在此包名以及子包下的调用才会使用此打印器。
         *
         * 当为ILogger对象时，则直接使用此对象作为日志打印器
         *
         * [defTag]参数用来定义此打印器的默认tag，如果不进行设置，则会使用主包名的最后一段作为默认tag
         */
        fun register(entry: Any, defTag: String? = null) {

            if (entry is ILogger) {
                if (!TextUtils.isEmpty(defTag)) {
                    entry.defaultTag = defTag
                }
                log.registerLogger(entry)
                return
            }
            val loggerId = getLoggerId(entry)

            val logger = DefLogger(loggerId)
            if (TextUtils.isEmpty(defTag)) {
                logger.defaultTag = loggerId.substringAfterLast(".")
            } else {
                logger.defaultTag = defTag
            }

            log.registerLogger(logger)
        }

        /**
         * 解注一个logger
         *
         * 入参接收三种类型：主包下的对象、String类型的主包名、ILogger对象
         *
         * 解注以后，对应logger将不能进行日志打印
         */
        fun unregister(entry: Any) {
            val loggerId = if (entry is ILogger) entry.id else getLoggerId(entry)
            log.unregisterLogger(loggerId)
        }

        /**
         * 设置指定logger是否可用
         */
        fun setLoggerEnable(entry: Any, enable: Boolean) {
            if (entry is ILogger) {
                entry.enable = enable
            } else {
                log.getLogger(getLoggerId(entry))?.enable = enable
            }
        }

        /**
         * 获取指定的日志打印器
         */
        fun getLogger(entry: Any): ILogger? {
            if (entry is ILogger) return entry
            return log.getLogger(getLoggerId(entry))
        }
    }
}

private const val mInvokeNextPath = "com.meili.moon.sdk.log.LogcatKt"

/**
 * 打印日志，级别：debug
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.d]、[Logcat]
 */
fun <T> T.log(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.log(this, D, tag, null, null, "$mInvokeNextPath.log", traceCount)
    return this
}

/**
 * 打印日志，级别：error
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.e]、[Logcat]
 */
fun <T> T.logE(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.log(this, E, tag, null, null, "$mInvokeNextPath.logE", traceCount)
    return this
}

/**
 * 打印日志，级别：info
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.i]、[Logcat]
 */
fun <T> T.logI(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.log(this, I, tag, null, null, "$mInvokeNextPath.logI", traceCount)
    return this
}

/**
 * 打印日志，级别：verbose
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.v]、[Logcat]
 */
fun <T> T.logV(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.log(this, V, tag, null, null, "$mInvokeNextPath.logV", traceCount)
    return this
}

/**
 * 打印日志，级别：warn
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.w]、[Logcat]
 */
fun <T> T.logW(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.log(this, W, tag, null, null, "$mInvokeNextPath.logW", traceCount)
    return this
}

/**
 * 打印日志，级别：warn
 *
 * 默认使用当前调用的包名作为loggerSubId
 *
 * 更多定制参考[Logcat.json]、[Logcat]
 */
fun <T> T.logJson(tag: String? = null, traceCount: Int? = null): T {
    Logcat.log.json(this, tag, null, null, "$mInvokeNextPath.logJson", traceCount)
    return this
}