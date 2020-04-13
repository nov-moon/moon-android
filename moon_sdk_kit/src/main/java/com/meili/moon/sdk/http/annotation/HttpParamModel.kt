package com.meili.moon.sdk.http.annotation

/**
 * 注解当前属性不作为param入参，而将他的属性作为param入参
 * Created by imuto on 17/11/28.
 */
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
annotation class HttpParamModel