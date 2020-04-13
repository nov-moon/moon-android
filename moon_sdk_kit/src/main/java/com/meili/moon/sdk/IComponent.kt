package com.meili.moon.sdk

import android.app.Application
import com.meili.moon.sdk.common.DefaultEnvironment
import com.meili.moon.sdk.log.Logcat

/**
 * 皓月组件的入口类。
 *
 * 所有的符合皓月接口设计的组件，都必须提供一个实现了[IComponent]接口的单例类。
 * kit插件会在编译期自动根据[IComponent]接口的实现，去找单例对象**INSTANCE**，并且在kit中进行注册。
 * 如果没有提供单例对象，则会在编译期报错
 * 自动注册的代码逻辑，请参照moon-kit插件（moon_tools项目中）
 *
 * Created by jiang on 2018/5/15.
 */
interface IComponent {

    fun init(app: Application) {

        // 注册临时上下文，如果有全局注册，则此注册无效
        val env = DefaultEnvironment(app)
        ComponentsInstaller.installEnvInternal(env)

        Logcat.config().register(this)

        init(env)
        onAfterInit()
    }

    fun init(env: Environment)

    /**
     * 当初始化完成后的回调，用来做一些依赖系统库初始化完成的操作
     */
    fun onAfterInit() {}

}
