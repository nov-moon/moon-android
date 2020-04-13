package com.meili.moon.sdk.http

/**
 *Created by jiang
on 2019-10-09
 */
interface IConfig {
    var parser: IResponseParser?
    var extraConvert: IResponseExtraConvert?
    var baseUrl: String?
    var heads: MutableMap<String, String>?
    var strategy: IHttp.SameRequestStrategy?

}

interface IResponseExtraConvert {
    /**去掉统一返回的Json格式*/
    fun convertCommonData(result: String?, response: IHttpResponse)
}