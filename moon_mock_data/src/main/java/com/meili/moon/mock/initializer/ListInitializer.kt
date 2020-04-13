package com.meili.moon.mock.initializer

import com.meili.moon.mock.mocker.ModelMocker
import com.meili.moon.sdk.mock.MockList
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.foreach
import com.meili.moon.sdk.util.isList
import com.meili.moon.sdk.util.toT
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2018/12/22.
 */
internal object ListInitializer : AbsInitializer() {

    override fun isMatch(info: InitializerInfo): Boolean {
        if (info.annotated is KClass<*>) {
            return false
        }
        return info.clazz.isList
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val kParameter = info.annotated as KProperty<*>
        val args = kParameter.returnType.arguments
        val kTypeProjection = args[0]
        val classifier = kTypeProjection.type?.classifier

        val itemClz = classifier as KClass<*>
        val listAnnotation = kParameter.findAnnotation<MockList>()
        val size = listAnnotation?.size ?: 20

        val resultList = mutableListOf<Any>()

        val itemInfo = createInfo(itemClz)

        fixInfoAnnotations(itemInfo, info.annotations)

        size.foreach {

            val mockItem = (mocker as ModelMocker).mock(itemInfo, mocker)
            if (mockItem != null) {
                resultList.add(mockItem)
            }
        }

        return resultList.toT()
    }
}