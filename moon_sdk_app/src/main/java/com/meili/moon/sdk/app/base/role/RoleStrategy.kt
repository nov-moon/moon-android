package com.meili.moon.sdk.app.base.role

import kotlin.reflect.KClass

/**
 * Author wudaming
 * Created on 2018/9/26
 */
@Retention()
@Target(AnnotationTarget.CLASS)
annotation class RoleStrategy(val strategy:Array<KClass<out IRoleStrategy<*>>>)