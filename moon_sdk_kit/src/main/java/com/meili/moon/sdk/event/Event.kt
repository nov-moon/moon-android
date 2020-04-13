package com.meili.moon.sdk.event

/**
 * 发起一个订阅
 *
 * 如果子类覆盖父类方法，父类已经有注解，则子类就不要再进行注解，否则子类会收到多次调用。直接重写父类方法就可以
 *
 * Created by imuto on 2018/4/13.
 */
@Target(AnnotationTarget.FUNCTION)
annotation class Event(
        /**订阅事件的订阅key值，当前只在订阅类型为bundle类型时有用，
         * 如果为bundle类型则value必须设置，并且只有匹配此value的事件才能接收到
         */
        val value: Int = 0,
        /**
         * 订阅方法的优先级，优先级高的方法会优先接收到订阅事件
         */
        val priority: Int = 0)