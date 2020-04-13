package com.meili.moon.sdk.util

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.TAG
import java.io.Serializable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by imuto on 2018/6/21.
 */
object Trace {

    init {
//        LogUtil.addLogger(DefLogger(TAG))
    }

    private val traceMap = ConcurrentHashMap<String, LinkedList<TraceInfo>>()

    fun traceBegin(tag: String, msg: String? = null) {
        if (!checkEnv(tag)) return

        val info = newTraceInfo(tag, msg)

        printPreTrace(tag, info)

        var list = traceMap[tag]
        if (list == null) {
            list = LinkedList()
            traceMap[tag] = list
        }
        list.addLast(info)
    }

    fun traceEnd(tag: String, msg: String? = null) {
        if (!checkEnv(tag)) return

        traceBegin(tag, msg)

        LogUtil.e(TAG, traceInfo(tag))
        remove(tag)
    }

    private fun traceInfo(tag: String): String? {
        if (!checkEnv(tag)) return null

        val list = traceMap[tag] ?: return null
        if (list.isEmpty()) return null

        val sb = StringBuilder()
        sb.append("TAG = ").append(tag).append(" { ")

        var info: TraceInfo? = null

        ((list.size - 1) downTo 0).forEach {
            val pre = list[it]
            if (info == null) {
                info = pre
                return@forEach
            }

            val infoNew = info ?: return@forEach

            sb.append("差值(ms) = ").append(infoNew.millis - pre.millis)
            sb.append(", ")
            sb.append("currMsg = ").append(infoNew.msg)
            sb.append(", ")
            sb.append("preMsg = ").append(pre.msg)
            sb.append("\n")
            info = pre
        }

        sb.append(" }")
        return sb.toString()
    }

    fun remove(tag: String) {
        traceMap.remove(tag)
    }

    fun clear() {
        traceMap.clear()
    }

    private fun checkEnv(tag: String): Boolean {
        return !tag.isEmpty() && CommonSdk.environment().isDebug()
    }

    private fun printPreTrace(tag: String, info: TraceInfo): LinkedList<TraceInfo>? {
        val list = traceMap[tag] ?: return null
        val pre = list.peekLast() ?: return list

        val sb = StringBuilder()
        sb.append("TAG = ").append(tag)
                .append(" (")
                .append(info.index)
                .append(")")
                .append(" { ")
        sb.append("差值(ms) = ").append(info.millis - pre.millis)
        sb.append(", ")
        sb.append("currMsg = ").append(info.msg)
        sb.append(", ")
        sb.append("preMsg = ").append(pre.msg)
        sb.append(" }")

        LogUtil.d(TAG, sb.toString())

        return list
    }

    private fun newTraceInfo(tag: String, msg: String?): TraceInfo {
        var index = 0
        val linkedList = traceMap[tag]
        if (linkedList != null) {
            index = linkedList.size
        }

        val info = TraceInfo(System.currentTimeMillis(), msg, System.nanoTime(), index)
        return info
    }

    private data class TraceInfo(val millis: Long, val msg: String?, val nano: Long, val index: Int) : Serializable
}
