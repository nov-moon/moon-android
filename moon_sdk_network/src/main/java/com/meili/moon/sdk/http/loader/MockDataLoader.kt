package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.http.IResponse
import com.meili.moon.sdk.http.exception.MockException
import com.meili.moon.sdk.util.isEmpty

/**
 * 默认的http请求及解析器
 * Created by imuto on 19/01/09.
 */
class MockDataLoader<out ResultType>(
        //请求入参
        params: IRequestParams.IHttpRequestParams)
    : DefHttpLoader<ResultType>(params) {

    override fun validate(): Boolean = CommonSdk.isMocker()


    override fun request(params: IRequestParams.IHttpRequestParams): IResponse {
        val mocker = CommonSdk.mocker()
        if (mocker.validate()) {
            val mockData = mocker.mockData(params)
            if (!isEmpty(mockData)) {
                return MockResponse(200, mockData!!)
            }
        }
        throw MockException(404, "没有找到mock数据")
    }
}