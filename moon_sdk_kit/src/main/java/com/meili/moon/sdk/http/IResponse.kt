package com.meili.moon.sdk.http

import java.io.InputStream

/**
 * response的标准接口
 *
 * Created by imuto on 2019/1/9.
 */
interface IResponse {

    /**当前的httpCode*/
    var code: Int

    /**当前的内容长度*/
    var contentLength: Long

    /**当前的body内容，此字段可为null，比如文件流*/
    var bodyByString: String?

    /**当前的body内容，此字段可为null，比如一般请求的返回结果*/
    var bodyByStream: InputStream?

    /**获取当前请求的header*/
    var headers: Map<String, String>
}