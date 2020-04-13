package com.meili.moon.sdk.log

/**
 * 日志标准接口
 *
 * 提供打印器的id设置、是否可用、默认tag。提供日志打印（级别参见[Level]），日志json、xml格式打印
 *
 * Created by imuto on 17/11/27.
 */
interface ILogger {

    /**当前打印器是否可用*/
    var enable: Boolean

    /**日志打印器的id*/
    var id: String

    /**默认的Tag*/
    var defaultTag: String?

    /**
     * 打印日志
     *
     * [msg] 日志内容
     * [level] 日志级别，参见[Level]
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     * [traceCount] 打印触发点代码的trace总量，默认为1
     */
    fun log(msg: Any?, level: Level, tag: String?, headerInfo: String? = null, fixedMethodCount: Int? = null, traceCount: Int? = null)

    /**
     * 打印json格式日志，使用Debug级别
     *
     * [json] 日志内容
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     * [traceCount] 打印触发点代码的trace总量，默认为1
     */
    fun json(json: String, tag: String?, headerInfo: String? = null, fixedMethodCount: Int? = null, traceCount: Int? = null)

    /**
     * 打印xml格式日志，使用Debug级别
     *
     * [xml] 日志内容
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     * [traceCount] 打印触发点代码的trace总量，默认为1
     */
    fun xml(xml: String, tag: String?, headerInfo: String? = null, fixedMethodCount: Int? = null, traceCount: Int? = null)

    /**
     * 日志的打印级别，对应系统的debug、error、warn、info、verbose
     */
    enum class Level {
        D, E, W, I, V
    }
}

/**TAG属性*/
val Any.TAG: String
    get() = this::class.simpleName!!