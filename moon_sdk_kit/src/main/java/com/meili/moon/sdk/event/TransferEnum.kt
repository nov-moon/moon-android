package com.meili.moon.sdk.event

/**
 * 消息的传送方式
 *
 * Created by imuto on 2018/4/17.
 */
enum class TransferEnum {
    /**只有第一个订阅者会接收到消息*/
    ONLY_FIRST,
    /**根据订阅者方法返回值确定是否继续传递，如果没有返回值或者返回值为false，则继续发送下一个订阅者，否则终止发送*/
    BY_SUBSCRIBER,
    /** 所有的都可以接收到*/
    ALL
}