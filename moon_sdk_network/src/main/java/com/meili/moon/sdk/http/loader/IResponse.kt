package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.http.IResponse
import com.meili.moon.sdk.util.foreach
import okhttp3.Response
import java.io.InputStream


/**
 * 默认的OkHttp对象解析
 * Created by imuto on 17/12/4.
 */
class OkHttpResponse(private val resp: Response) : IResponse {

    override var headers: Map<String, String> = getHeaders(resp)

    override var code: Int = resp.code()

    override var contentLength: Long = resp.body()!!.contentLength()

    override var bodyByString: String? = resp.body()!!.string()

    override var bodyByStream: InputStream? = null
}

/**
 * 默认的OkHttp对象解析
 * Created by imuto on 17/12/4.
 */
class OkHttpDownloadResponse(private val resp: Response) : IResponse {

    override var headers: Map<String, String> = getHeaders(resp)

    override var code: Int = resp.code()

    override var contentLength: Long = resp.body()!!.contentLength()

    override var bodyByString: String? = null

    override var bodyByStream: InputStream? = resp.body()!!.byteStream()
}

/**
 * Mock数据的response
 * Created by imuto on 17/12/4.
 */
class MockResponse(httpCode: Int, body: String) : IResponse {
    override var headers: Map<String, String> = getHeaders(null)

    override var code: Int = httpCode

    override var contentLength: Long = body.length.toLong()

    override var bodyByString: String? = body

    override var bodyByStream: InputStream? = null
}

private fun getHeaders(resp: Response?): Map<String, String> {
    val result = mutableMapOf<String, String>()
    resp ?: return result

    val headers = resp.headers()
    headers.size().foreach {
        val name = headers.name(it) ?: return@foreach
        val value = headers.get(name) ?: return@foreach
        result[name] = value
    }
    return result
}