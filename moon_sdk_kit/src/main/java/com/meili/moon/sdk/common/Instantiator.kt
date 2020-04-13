package com.meili.moon.sdk.common

/**
 * 实例化器
 * Created by imuto on 17/11/23.
 */
interface Instantiator<out Type> {
    fun newInstance(vararg var2: Any): Type
}