package com.meili.moon.sdk.event

/**用于Bundle作为分发参数的key关键字，入参必须为int值*/
const val BUNDLE_KEY = "meili_#event_key_"

/**
 * 只能在主线程传递的事件管理器
 * Created by imuto on 2018/4/13.
 */
interface Events {
    /**
     * 注册一个订阅者
     *
     * [subscriber] 处理事件的对象
     */
    fun register(subscriber: Any)

    /**
     * 注销一个订阅者
     *
     * [subscriber] 已经订阅的订阅者
     */
    fun unregister(subscriber: Any)

    /**
     * 发送一个事件，根据[transfer] 策略分发事件
     *
     * [ignored] 添加一个忽略此事件的对象，一般情况发起类本身需要忽略消息
     */
    fun post(event: Any, transfer: TransferEnum = TransferEnum.ONLY_FIRST, ignored: Any? = null)

    /**
     * 添加一个事件拦截器
     *
     * [interceptor]第一个参数为当前事件实体，第二个参数为当前订阅者实体
     *
     * 返回值为true，则拦截事件，否则继续传递
     */
    fun addEventInterceptor(interceptor: (event: Any, subscriber: Any) -> Boolean)

    /**
     * 添加一个忽略的subscriber基类型，这个类型以及这个类型的父类型直接忽略
     *
     */
    fun addIgnoreSubscriber(vararg ignored: Class<*>)
}