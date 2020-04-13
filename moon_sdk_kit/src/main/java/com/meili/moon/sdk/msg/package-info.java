/**
 * 本包定义了跨组件的message类型，当前主要用来启动一个MessageTask类型
 * <p>
 * 使用本包，首先new一个BaseMessage对象A，A可以设置参数、回调等
 * <p>
 * 调用A的send方法发送一个消息。
 * <p>
 * 通过 {@link com.meili.moon.sdk.msg.MessageRegistry} 注册message对象。
 * 注册时，在debug环境会检查taskId是否已经被占用，如果占用则报错。不是debug时将使用新的注册覆盖之前的task
 */
package com.meili.moon.sdk.msg;