package com.meili.moon.sdk.msg

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.common.BaseException
import com.meili.moon.sdk.common.Cancelable
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.throwOnDebug
import java.util.concurrent.ConcurrentHashMap

/**
 * message的登记处
 * Created by imuto on 2018/5/4.
 */
object MessageRegistry {

    private val taskMap = ConcurrentHashMap<String, Class<out MessageTask<*>>>()

    /**启动一个task*/
    fun start(message: BaseMessage): Cancelable? {
        val taskClass = taskMap[message.msgId] ?: return null

        val task = taskClass.getConstructor(BaseMessage::class.java).newInstance(message)
        return CommonSdk.task().start(task)
    }

    /**注册task*/
    fun register(taskId: String, task: Class<out MessageTask<*>>) {
        if (isEmpty(taskId)) {
            return
        }
        val clazz = taskMap[taskId]
        if (clazz != null) {
            throwOnDebug(BaseException(msg = "注册的taskId已经被占用，占用task类为：$clazz"))
        }
        taskMap[taskId] = task
    }

    /**是否存在指定的task*/
    fun exist(taskId: String): Boolean {
        if (isEmpty(taskId)) return false
        return taskMap.contains(taskId)
    }
}