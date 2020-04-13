package com.meili.moon.sdk.demo

import android.app.Application
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.demo.log.LoggerProvider
import com.meili.moon.sdk.log.Logcat

/**
 * Created by imuto on 2019-07-16.
 */
class DemoApp : Application() {
    override fun onCreate() {
        super.onCreate()

        ComponentsInstaller.installEnvironment(this)
//
//        var r = "".log()
//        var r2: String? = ""
//        var r3 = r2.log()


        //第一步：初始化自定义ILogger
        val logger = LoggerProvider()
        logger.id = "com.meili.moon.sdk.demo"
        logger.defaultTag = "Meili"

        //第二步：注册自定义ILogger
        Logcat.register(logger)
    }
}