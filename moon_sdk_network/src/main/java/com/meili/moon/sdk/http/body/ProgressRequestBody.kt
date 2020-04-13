package com.meili.moon.sdk.http.body

import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * 带有进度的progressBody
 * Created by imuto on 17/12/5.
 */
class ProgressRequestBody(private val original: RequestBody, private val progress: (Long, Long) -> Unit) : RequestBody() {

    private var bufferedSink: BufferedSink? = null

    override fun contentType() = original.contentType()!!

    override fun contentLength() = original.contentLength()

    override fun writeTo(sink: BufferedSink?) {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(sink(sink))
        }
        original.writeTo(bufferedSink)
        bufferedSink?.flush()
    }

    /**
     * 写入，回调进度接口
     */
    private fun sink(sink: Sink?): Sink {
        return object : ForwardingSink(sink) {
            //当前写入字节数
            private var bytesWritten = 0L
            //总字节长度，避免多次调用contentLength()方法
            private var contentLength = 0L

            @Throws(IOException::class)
            override fun write(source: Buffer, byteCount: Long) {
                super.write(source, byteCount)
                if (contentLength == 0L) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength()
                }
                bytesWritten += byteCount

                //LogUtil.e("byteCount = " + byteCount + "  bytesWritten = " + bytesWritten + " contentLength = " + contentLength);
                progress(contentLength, bytesWritten)
            }
        }
    }
}