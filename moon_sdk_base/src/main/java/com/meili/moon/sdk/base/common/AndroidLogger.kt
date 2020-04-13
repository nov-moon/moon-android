package com.meili.moon.sdk.base.common

import com.meili.moon.sdk.log.ILogger
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.Logger
import com.orhanobut.logger.PrettyFormatStrategy

/**
 * 默认的Android日志打印
 * Created by imuto on 17/12/15.
 */
class AndroidLogger : ILogger {
    /**当前打印器是否可用*/
    override var enable: Boolean = true
    /**日志打印器的id*/
    override var id: String = ""
    /**默认的Tag*/
    override var defaultTag: String? = null

    /**
     * 打印日志
     *
     * [msg] 日志内容
     * [level] 日志级别，参见[Level]
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     */
    override fun log(msg: Any?, level: ILogger.Level, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
    }

    /**
     * 打印json格式日志，使用Debug级别
     *
     * [json] 日志内容
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     */
    override fun json(json: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
    }

    /**
     * 打印xml格式日志，使用Debug级别
     *
     * [xml] 日志内容
     * [tag] 日志的tag
     * [headerInfo] 日志的头信息，用来扩展日志信息
     * [fixedMethodCount] 用来获取调用代码位置的修正数据，取值可：±
     */
    override fun xml(xml: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
    }

    init {
        val newBuilder = PrettyFormatStrategy.newBuilder()
        newBuilder.methodCount(4)
                .methodOffset(5)
                .showThreadInfo(false)
                .tag("MEI_LI")
        Logger.addLogAdapter(AndroidLogAdapter(newBuilder.build()))
    }
}