package com.meili.moon.sdk.http.annotation

/**
 * Created by imuto on 2018/5/29.
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class HttpParam(val pathParam: Boolean = false)