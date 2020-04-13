package com.meili.moon.sdk.app.base.role

import kotlin.reflect.KClass

/**
 * Author wudaming
 * Created on 2018/9/26
 */
@Target(AnnotationTarget.CLASS)
annotation class TargetRole(val role:KClass<out UserRole>)