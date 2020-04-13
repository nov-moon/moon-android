/**
 * 本包提供了网络请求的接口定义
 *
 * {@link com.meili.moon.sdk.http.IHttp} 提供了网络请求入口
 *
 * {@link com.meili.moon.sdk.http.IRequestParams} 提供了请求入参标准
 *
 * {@link com.meili.moon.sdk.http.IHttpResponse} 提供了标准response的形式
 *
 * {@link com.meili.moon.sdk.http.IParamsBuilder} 提供了标准param构建器
 *
 * {@link com.meili.moon.sdk.http.IRequestTracker} 提供了标准请求追踪记录定义
 *
 * {@link com.meili.moon.sdk.http.IResponseParser} 提供了标准返回结果解析方式
 *
 * {@link com.meili.moon.sdk.http.IRetryHandler} 提供了标准的重试机制
 *
 * 本包定义的网络强求方式为：
 * 1. 定义一个继承IRequestParams接口的请求对象MRequest，并将请求值作为成员变量定义到类本身
 * 2. MRequest类上可以使用注解@HttpRequest，进行定义请求
 * 3. 定义一个匹配返回结果的接收对象，进行结果接收，并在IHttp的方法调用时，以泛型的方式通知IHttp
 *
 *  TODO 后面的将优化request的实现方式，将不用继承IRequestParams，而是通过注解的方式，将request的实现注入到请求中
 *
 */
package com.meili.moon.sdk.http;