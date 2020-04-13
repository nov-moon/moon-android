package com.meili.moon.sdk.http.annotation

/**
 * 注解httpRequest，配置部分特性
 * Created by imuto on 17/11/28.
 */
@Target(AnnotationTarget.CLASS)
annotation class HttpRequest(
        //设置path路径，为了方便填写，所以使用默认的名称
        val value: String,
        //设置host，如果为空，则使用默认host
        val host: String = "",
        //设置当前接口版本号
        val version: String = "")
//       ) //设置请求参数的构建方式，如果不设置，则使用默认构建方式
////        val builder: KClass<out IParamsBuilder<IHttpRequestParams>> = SdkHttpParamsBuilder::class