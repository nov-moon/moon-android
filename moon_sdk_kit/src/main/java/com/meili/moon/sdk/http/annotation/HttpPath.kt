package com.meili.moon.sdk.http.annotation

/**
 * 注解httpRequest，配置部分特性
 * Created by imuto on 17/11/28.
 */
@Target(AnnotationTarget.FIELD)
annotation class HttpPath(
        //设置path路径，为了方便填写，所以使用默认的名称
        val value: String = ""
       )