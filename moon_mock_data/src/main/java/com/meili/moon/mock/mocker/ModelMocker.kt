package com.meili.moon.mock.mocker

import com.meili.moon.mock.initializer.*
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.mock.IMOCKER_PRIORITY_FROM_MODEL
import com.meili.moon.sdk.mock.IMOCKER_SIZE_MASK
import com.meili.moon.sdk.mock.Mocker
import com.meili.moon.sdk.util.foreach
import com.meili.moon.sdk.util.toT
import kotlin.reflect.KClass

/**
 * Created by imuto on 2018/12/13.
 */
class ModelMocker : Mocker {

    companion object {
        private val initializerList = listOf(
                PrimitiveInitializer,
                StringInitializer,

                MapInitializer,
                ListInitializer,
                ArrayInitializer,

                DateInitializer,

                BigDecimalInitializer,

                ObjectInitializer,

                ModelInitializer
        )
    }

    override fun priority(): Int = IMOCKER_PRIORITY_FROM_MODEL

    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>): T? {
        val info = AbsInitializer.createInfo(mockClz)
        initializerList.forEach {
            if (it.isMatch(info)) {
                return it.init(info, this)
            }
        }

        return null
    }

    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int): MutableList<T>? {
        val resultSize = if (size == IMOCKER_SIZE_MASK || size <= 0) {
            20
        } else size

        val result = mutableListOf<T>()

        resultSize.foreach {
            val mockItem = mock(mockId, mockClz).toT<T>() ?: return@foreach
            result.add(mockItem)
        }

        return result
    }

    override fun <T : IRequestParams> mockData(param: T): String? {
        return null
    }

    internal fun mock(info: InitializerInfo, mocker: Mocker): Any? {
        initializerList.forEach {
            if (it.isMatch(info)) {
                return it.init(info, mocker)
            }
        }

        return null
    }
}