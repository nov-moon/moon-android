package com.meili.moon.sdk.page.annotation

import android.support.annotation.LayoutRes

/**
 * 页面的Layout
 * Created by imuto on 2019-08-14.
 */
@Target(AnnotationTarget.CLASS)
annotation class Layout(@LayoutRes val value: Int)