package com.meili.moon.sdk.http

import android.app.Application
import com.meili.moon.sdk.ComponentsInstaller
import com.meili.moon.sdk.Environment
import com.meili.moon.sdk.common.DefaultEnvironment
import com.meili.moon.sdk.http.impl.HttpImpl

/**
 * http包的主要功能提供类
 * Created by imuto on 17/12/6.
 */
object HttpSdk {
    @JvmStatic
    fun init(env: Environment) {
        ComponentsInstaller.installEnvironment(env)
    }

    @JvmStatic
    fun init(app: Application) {
        ComponentsInstaller.installEnvironment(DefaultEnvironment(app))
    }

    @JvmStatic
    fun http(): IHttp = HttpImpl
}