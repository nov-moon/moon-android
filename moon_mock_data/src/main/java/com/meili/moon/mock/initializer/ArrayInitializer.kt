package com.meili.moon.mock.initializer

import com.meili.moon.mock.mocker.ModelMocker
import com.meili.moon.sdk.mock.MockList
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.foreach
import com.meili.moon.sdk.util.isArray
import com.meili.moon.sdk.util.toT
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2018/12/22.
 */
internal object ArrayInitializer : AbsInitializer() {
    override fun isMatch(info: InitializerInfo): Boolean {
        return info.annotated.isArray
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val jClass = info.clazz?.java ?: return null

        val listAnnotation = info.annotated.findAnnotation<MockList>()
        val size = listAnnotation?.size ?: 20

        val componentType = jClass.componentType
        val arrays = java.lang.reflect.Array.newInstance(componentType, size)
        val itemInfo = createInfo(componentType.kotlin)

        fixInfoAnnotations(itemInfo, info.annotations)

        size.foreach {
            val item = initItem<T>(mocker, itemInfo)
            java.lang.reflect.Array.set(arrays, it, item)
        }

        return arrays.toT()
    }

    private fun <T> initItem(mocker: Mocker, itemInfo: InitializerInfo) =
            (mocker as ModelMocker).mock(itemInfo, mocker).toT<T>()!!
}