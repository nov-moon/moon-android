package com.meili.moon.sdk.http.annotation

/**
 * 生成http请求时，忽略的字段和类
 * Created by imuto on 17/11/28.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class HttpIgnoreBuildParam