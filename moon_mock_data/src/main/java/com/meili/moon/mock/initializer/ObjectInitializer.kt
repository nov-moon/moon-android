package com.meili.moon.mock.initializer

import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.toT

/**
 * Created by imuto on 2018/12/22.
 */
internal object ObjectInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        return info.clazz == Any::class
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        return Any().toT()
    }
}