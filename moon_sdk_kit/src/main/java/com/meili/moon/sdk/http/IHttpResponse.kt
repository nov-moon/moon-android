package com.meili.moon.sdk.http

import com.meili.moon.sdk.common.BaseException
import java.io.Serializable

/**
 * http请求的返回信息处理接口
 * Created by imuto on 17/11/28.
 */
interface IHttpResponse : Serializable {

    /**response字符串*/
    var response: String?
    /**解析的业务数据*/
    var data: String?
    /**状态码*/
    var state: Int
    /**http状态码：200、404、500*/
    var httpState: Int
    /**提示信息*/
    var message: String
    /**是否是list*/
    var isListResult: Boolean
    /**item的Class类型*/
    var itemKClass: Class<*>

    /**response返回的header*/
    var headers: Map<String, String>

    /**默认的response的解析器*/
    var parser: IResponseParser?
    /**转换器*/
    var convert: IResponseExtraConvert?

    /**当前这个response的params*/
    var requestParams: IRequestParams.IHttpRequestParams

    /**自定义处理解析数据，如果返回true，则通用parser不解析数据，直接返回*/
    fun handleParseData(response: String?): Boolean

    /**处理错误信息*/
    fun handleError(throwable: Throwable): BaseException

    /**解析结果的泛型类型的KClass结果*/
    fun parseItemParamType(paramEntity: Any?)

    fun isSuccess() = true
}