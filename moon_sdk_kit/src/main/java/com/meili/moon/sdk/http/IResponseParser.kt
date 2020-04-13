package com.meili.moon.sdk.http


/**
 * response的解析类
 * Created by imuto on 17/11/28.
 */
interface IResponseParser {
    /**解析信息*/
    fun <DataType> parse(response: IHttpResponse, dataType: Class<DataType>): List<DataType>?

//    /**
//     * 转换result为resultType类型的对象
//     *
//     * @param response  返回值response
//     * @param resultClass 返回值类型
//     * @param result      字符串数据
//     * @return
//     * @throws Throwable
//     */
//    @Throws(Throwable::class)
//    abstract fun <DataModel> parse(response: IHttpResponse<*>, resultClass: Class<DataModel>, result: String): List<DataModel>
}