package com.meili.moon.sdk.demo.log

import com.meili.moon.sdk.log.ILogger
import com.meili.moon.sdk.log.ILogger.Level.*
import com.orhanobut.logger.Logger

/**
 * Created by imuto on 2019-07-22.
 */
class LoggerProvider : ILogger{
    override var enable: Boolean = true
    override var id: String = ""
    override var defaultTag: String? = ""

    override fun log(msg: Any?, level: ILogger.Level, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
        val p = when (level) {
            D -> Logger.DEBUG
            V -> Logger.VERBOSE
            I -> Logger.INFO
            W -> Logger.WARN
            E -> Logger.ERROR
        }
        Logger.log(p, tag, msg?.toString(), null)
    }

    override fun json(json: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
        Logger.json(json)
    }

    override fun xml(xml: String, tag: String?, headerInfo: String?, fixedMethodCount: Int?) {
    }
}