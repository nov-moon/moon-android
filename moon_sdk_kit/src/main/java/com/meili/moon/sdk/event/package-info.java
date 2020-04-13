/**
 * 本包提供了消息事件订阅的实现
 * <p>
 * 用户可以通过消息订阅，得到其他消息分发，再处理消息内容。使用方式如下：
 * 首先在需要订阅消息的类中通过{@link com.meili.moon.sdk.event.Events#register(java.lang.Object)}方法注册当前类
 * 再通过{@link com.meili.moon.sdk.event.Event}注解标注方法，此方法的接受参数即为订阅的消息体类型。
 * 在发送方，通过{@link com.meili.moon.sdk.event.Events#post(java.lang.Object, com.meili.moon.sdk.event.TransferEnum, java.lang.Object)}方法发送事件。
 * 在发送消息时，你可以通过{@link com.meili.moon.sdk.event.TransferEnum}指定分发方式。
 *
 * 针对发送消息类型为Bundle类型的，我们强制要求Bundle中必须包含一个key值为{@link com.meili.moon.sdk.event.EventsKt#BUNDLE_KEY}的值用来区分接收方。
 * 强制要求接收方在注解中设置value值订阅bundle类型下的事件id，只有消息体中的值匹配此注解中的value，订阅方法才能收到消息
 *
 */
package com.meili.moon.sdk.event;