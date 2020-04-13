package com.meili.moon.sdk

import android.content.Context

/**
 * 组件初始化入口
 * Created by imuto on 2018/4/8.
 */
interface ComponentInitializer {
    fun init(app: Context)
}