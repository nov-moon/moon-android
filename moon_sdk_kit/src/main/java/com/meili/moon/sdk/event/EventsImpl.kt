package com.meili.moon.sdk.event

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.exception.EventsException
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.assertTrue
import com.meili.moon.sdk.util.isMainThread
import com.meili.moon.sdk.util.sameSize
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 主线程事件订阅管理器
 *
 * 主要包含注册、注销、发送事件
 *
 * 发送：
 *
 * 1. 处理对象对结果处理在主线程进行
 *
 * Created by imuto on 2018/4/13.
 */
object EventsImpl : Events {

    /**每个type对应一组Method和subscriber*/
    private val types = ConcurrentHashMap<Class<*>, ArrayList<EventHolder>>(20)

    private val interceptors = mutableListOf<(Any, Any) -> Boolean>()
    private val ignoredClazz = mutableSetOf<Class<*>>()

    private val QEQ_COUNTER = AtomicInteger(0)

    override fun register(subscriber: Any) {

        isMainThread().assertTrue("register方法必须在主线程调用，暂时不支持子线程注册")

        // 检查当前类别是否已经包含subscriber
        types.values.forEach { list ->
            val find = list.find {
                it.subscriber() == subscriber
            }
            if (find != null) {
                return
            }
        }

        val tempList = mutableListOf<Class<*>>()

        val methods = getMethod(subscriber::class.java)
        methods.forEach {
            val annotation = it.getAnnotation(Event::class.java) ?: return@forEach
            //检查参数列表是否符合要求
            if (!is1ParamSubscriberMethod(it)) {
                throw EventsException(msg = "事件的订阅方法 method = ${it.name} 的入参错误，" +
                        "入参类型只能为单入参 (*)")
            }

            val paramType = it.parameterTypes[0]
            val isBundle = isBundleType(paramType)
            if (isBundle && annotation.value == 0) {
                throw EventsException(msg = "事件的订阅方法 method = ${it.name} 订阅类型为bundle，" +
                        "必须在注解上添加非0的监听key值")
            }
            val h = EventHolder(it, subscriber, annotation.priority, QEQ_COUNTER.getAndIncrement())

            var holders = types[paramType]
            if (holders == null) {
                holders = ArrayList()
                types[paramType] = holders
            }

            tempList.add(paramType)

            holders.add(h)
        }

        tempList.forEach {
            val holders = types[it] ?: return@forEach
            holders.sort()
        }
    }

    override fun post(event: Any, transfer: TransferEnum, ignored: Any?) {

        val eventClazz = event::class.java

        val isBundleType = isBundleType(eventClazz)
        val bundleKeyValue = getBundleKeyValue(event)
        if (isBundleType && bundleKeyValue == 0) {
            throw EventsException(msg = "事件发送类型为bundle类型，必须包含Events.BUNDLE_KEY条目作为接受者区分")
        }

        val holders = types[eventClazz] ?: return

        val lambda = lambda@{
            try {
                holders.indices.reversed().forEach {
                    val eventHolder = holders[it]

                    val subscriber = eventHolder.subscriber() ?: return@forEach

                    if (subscriber == ignored) return@forEach

                    interceptors.forEach interceptor@{ item ->
                        if (item.invoke(event, subscriber)) {
                            return@forEach
                        }
                    }

                    // 如果是bundle类型，则检查key值，如果key值和annotation上注册的value不匹配，则直接返回
                    if (isBundleType) {
                        val annotation = eventHolder.method.getAnnotation(Event::class.java)
                        if (annotation.value != bundleKeyValue) {
                            return@forEach
                        }
                    }
                    eventHolder.method.isAccessible = true
                    when (transfer) {
                        TransferEnum.ONLY_FIRST -> {
                            eventHolder.method.invoke(subscriber, event)
                            return@lambda
                        }
                        TransferEnum.BY_SUBSCRIBER -> {
                            val returnType = eventHolder.method.returnType
                            if (returnType == Boolean::class.java || returnType == Boolean::class.javaPrimitiveType) {
                                val result = eventHolder.method.invoke(subscriber, event) as Boolean
                                if (result) {
                                    return@lambda
                                }
                            } else {
                                eventHolder.method.invoke(subscriber, event)
                            }
                        }
                        TransferEnum.ALL -> {
                            eventHolder.method.invoke(subscriber, event)
                        }
                    }
                }
            } catch (t: Throwable) {
                LogUtil.e(t, null)
            }
        }

        if (Thread.currentThread() == Looper.getMainLooper().thread) {
            lambda.invoke()
        } else {
            CommonSdk.task().post(lambda)
        }
    }


    override fun unregister(subscriber: Any) {
        isMainThread().assertTrue("unregister方法必须在主线程调用，暂时不支持子线程注册")

        val methods = getMethod(subscriber::class.java)

        methods.forEach {
            if (it.getAnnotation(Event::class.java) == null) {
                return@forEach
            }

            val paramType = it.parameterTypes[0]
            val holders = types[paramType] ?: return@forEach

            if (holders.isEmpty()) {
                types.remove(paramType)
                return@forEach
            }

            val filter = holders.filter {
                it.subscriber() == subscriber || it.subscriber() == null
            }
            holders.removeAll(filter)

            if (holders.isEmpty()) {
                types.remove(paramType)
                return@forEach
            }
        }

    }

    override fun addEventInterceptor(interceptor: (Any, Any) -> Boolean) {
        isMainThread().assertTrue("addInterceptor方法必须在主线程调用，暂时不支持子线程注册")

        synchronized(interceptors) {
            if (interceptors.contains(interceptor)) {
                return
            }
            interceptors.add(interceptor)
        }
    }

    override fun addIgnoreSubscriber(vararg ignored: Class<*>) {
        synchronized(ignoredClazz) {
            ignoredClazz.addAll(ignored)
        }
    }

    private fun getMethod(clazz: Class<*>): List<Method> {
        if (ignoredClazz.contains(clazz)) {
            return emptyList()
        }
        val methods = clazz.declaredMethods.toMutableList()
        val superclass = clazz.superclass ?: return methods
        methods.addAll(getMethod(superclass))
        return methods
    }

    private fun is1ParamSubscriberMethod(method: Method): Boolean {
        val parameterTypes = method.parameterTypes
        return sameSize(parameterTypes, 1)
    }

    private fun is2ParamSubscriberMethod(method: Method): Boolean {
        val parameterTypes = method.parameterTypes
        // 有两个参数
        return sameSize(parameterTypes, 2)
                // 第一个参数是int类型的参数
                && (parameterTypes[0] == Int::class.java || parameterTypes[0] == Int::class.javaPrimitiveType)
    }

    private fun matchParams(method: Method, type1: Class<*>): Boolean {
        val parameterTypes = method.parameterTypes
        if (is1ParamSubscriberMethod(method)) {
            return parameterTypes[0] == type1
        }
        if (parameterTypes[0] != type1) return false

        val firstMatch = parameterTypes[0] == Int::class.java || parameterTypes[0] == Int::class.javaPrimitiveType

        return firstMatch && parameterTypes[1] == type1
    }

    private fun isBundleType(eventClazz: Class<out Any>) =
            eventClazz == Bundle::class.java || Intent::class.java.isAssignableFrom(eventClazz)

    private fun getBundleKeyValue(event: Any): Int {
        if (event is Bundle) {
            return event.getInt(BUNDLE_KEY)
        }
        if (event is Intent) {
            return event.getIntExtra(BUNDLE_KEY, 0)
        }
        return 0
    }

}