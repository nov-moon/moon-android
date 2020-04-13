package com.meili.moon.sdk.http.annotation

import com.meili.moon.sdk.http.IResponseParser
import kotlin.reflect.KClass

/**
 * 对网络数据对应的model的注解，自定义解析器
 * Created by imuto on 2018/5/2.
 */
annotation class HttpResponseParser(
        val value: KClass<out IResponseParser>
)