package com.meili.moon.mock.initializer

import com.meili.moon.mock.mocker.ModelMocker
import com.meili.moon.sdk.mock.MockMap
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.foreach
import com.meili.moon.sdk.util.isMap
import com.meili.moon.sdk.util.toT
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2018/12/22.
 */
internal object MapInitializer : AbsInitializer() {

    override fun isMatch(info: InitializerInfo): Boolean {
        if (info.annotated is KClass<*>) {
            return false
        }
        return info.clazz.isMap
    }

    override fun <T : Any> init(info: InitializerInfo, mocker: Mocker): T? {
        val kParameter = info.annotated as KProperty<*>
        val args = kParameter.returnType.arguments
        val keyTypeProjection = args[0]
        val valueTypeProjection = args[1]
        val keyClassifier = keyTypeProjection.type?.classifier
        val valueClassifier = valueTypeProjection.type?.classifier

        val keyClz: KClass<*> = keyClassifier.toT()!!
        val valueClz: KClass<*> = valueClassifier.toT()!!

        val listAnnotation = kParameter.findAnnotation<MockMap>()
        val size = listAnnotation?.size ?: 20

        val resultMap = mutableMapOf<Any, Any>()

        size.foreach {
            val keyInfo = createInfo(keyClz)
            val valueInfo = createInfo(valueClz)
            val key = (mocker as ModelMocker).mock(keyInfo, mocker)
            val value = mocker.mock(valueInfo, mocker)
            if (key != null && value != null) {
                resultMap[key] = value
            }
        }

        return resultMap.toT()
    }

}