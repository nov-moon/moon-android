package com.meili.moon.mock.initializer

import com.meili.moon.mock.mocker.ModelMocker
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KFunction

/**
 * Created by imuto on 2018/12/22.
 */
internal object ModelInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        val clazz = info.clazz
        return !clazz.isPrimitive && !clazz.isMap && !clazz.isList  && !clazz.isArray && clazz != Any::class
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val clazz = info.clazz.toT<KClass<T>>() ?: return null
        val constructors = clazz.constructors
        var creatorConstructors: KFunction<T>? = null

        //找到param最多的构造方法，kotlin已自动过滤了他自己内部使用的构造方法
        constructors.forEach {
            val creatorBackup = creatorConstructors
            if (creatorBackup == null) {
                creatorConstructors = it
            } else {
                if (creatorBackup.parameters.size < it.parameters.size) {
                    creatorConstructors = it
                }
            }
        }

        val creatorBackup = creatorConstructors ?: return null
        val parameters = creatorBackup.parameters
        val args = arrayOfNulls<Any?>(parameters.size)
        parameters.forEachIndexed { index, item ->
            val itemInfo = createInfo(item)
            fixClazzFromHolderInfo(item, itemInfo, info)
            val itemData = (mocker as ModelMocker).mock(itemInfo, mocker)
            args[index] = itemData
        }

        val result = creatorBackup.call(*args)

        return result
    }
}