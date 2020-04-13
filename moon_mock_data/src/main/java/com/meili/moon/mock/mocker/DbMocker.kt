package com.meili.moon.mock.mocker

import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.mock.IMOCKER_PRIORITY_FROM_JSON
import com.meili.moon.sdk.mock.Mocker
import kotlin.reflect.KClass

/**
 * Created by imuto on 2018/12/14.
 */
class DbMocker: Mocker {
    override fun priority(): Int = IMOCKER_PRIORITY_FROM_JSON

    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>): T? {
        return null
    }

    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int): MutableList<T>? {
        return null
    }

    override fun <T : IRequestParams> mockData(param: T): String? {
        return null
    }

}