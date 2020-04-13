package com.meili.moon.sdk.app.base.page.tab

import com.meili.moon.sdk.app.base.page.PageFragment
import kotlin.reflect.KClass

/**
 * Created by imuto on 2018/5/17.
 */
@Target(AnnotationTarget.CLASS)
annotation class TabContent(vararg val value: KClass<out PageFragment>, val checkedIndex: Int = 0)