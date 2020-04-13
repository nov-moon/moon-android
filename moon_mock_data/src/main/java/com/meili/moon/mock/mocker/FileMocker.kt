package com.meili.moon.mock.mocker

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.mock.IMOCKER_PRIORITY_FROM_JSON
import com.meili.moon.sdk.mock.MockByFile
import com.meili.moon.sdk.mock.Mocker
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2018/12/14.
 */
class FileMocker : Mocker {
    override fun priority(): Int = IMOCKER_PRIORITY_FROM_JSON

    override fun <T : Any> mock(mockId: String?, mockClz: KClass<T>): T? {
        return null
    }

    override fun <T : Any> mockList(mockId: String?, mockClz: KClass<T>, size: Int): MutableList<T>? {
        return null
    }

    override fun <T : IRequestParams> mockData(param: T): String? {
        if (param !is IRequestParams.IHttpRequestParams) {
            return null
        }

        try {
            /*
            1. 获取param上的注解，如果没有则认为本mocker不能处理，直接返回null
            2. 如果能处理，检查当前注解是否为asset文件，如果是asset文件，则读取文件，并返回数据
            3. 如果不是asset，则暂时不支持，返回null
             */

            val annotation = param::class.findAnnotation<MockByFile>()
                    ?: return null

            val filePath = annotation.filePath
            val isAssets = annotation.isAssets

            if (isAssets) {
                val mockStream = CommonSdk.app().assets.open("mock/$filePath")
                val reader = mockStream.reader()
                val result = reader.readText()
                try {
                    reader.close()
                    mockStream.close()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
                return result
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

}