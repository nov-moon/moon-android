package com.meili.moon.sdk.http.loader

import com.meili.moon.sdk.common.ProgressLambda
import com.meili.moon.sdk.http.IRequestParams.IDownloadFeatures
import com.meili.moon.sdk.http.IRequestParams.IHttpRequestParams
import com.meili.moon.sdk.http.IResponse
import com.meili.moon.sdk.http.exception.HttpException
import com.meili.moon.sdk.http.util.OkHttpClientDelegate
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.TAG
import com.meili.moon.sdk.util.ProcessLock
import com.meili.moon.sdk.util.closeQuietly
import com.meili.moon.sdk.util.createFile
import com.meili.moon.sdk.util.isEmpty
import okhttp3.Call
import okhttp3.Request
import okhttp3.Response
import java.io.*
import java.net.URLDecoder
import java.util.*

//断点的最小尺寸，如果已下载小于checkSize，则删除重新下载
private const val CHECK_SIZE = 512

/**
 * Created by imuto
 * <p/>
 * 下载参数策略:
 * <p/>
 * 1. {@link IDownloadFeatures#downloadFileSavePath}不为空时, 目标文件保存在DownloadFileSavePath;
 * 否则抛出错误
 * 2. 下载时临时目标文件路径为tempSaveFilePath, 下载完后进行:重命名等操作.
 * 断点下载策略:
 * 1. 要下载的目标文件不存在或小于 CHECK_SIZE 时删除目标文件, 重新下载.
 * 2. 若文件存在且大于 CHECK_SIZE, range = fileLen - CHECK_SIZE , 校验check_buffer, 相同: 继续下载;
 * 不相同: 删掉目标文件, 并抛出RuntimeException(HttpRetryHandler会使下载重新开始).
 */
class DefDownLoader<T>(params: T, progress: ProgressLambda = null) : SdkHttpLoader<File>(params, progress) where T : IHttpRequestParams {
    private val feature: IDownloadFeatures
        get() {
            return if (params is IDownloadFeatures) {
                params
            } else {
                object : IDownloadFeatures {
                    override var downloadFileSavePath: String = ""
                }
            }
        }
    private var tempSaveFilePath = "${feature.downloadFileSavePath}.tmp"
    private var lockName = "${feature.downloadFileSavePath}_lock"
    private var responseFileName: String? = null
    private var isAutoResume: Boolean = feature.isAutoResume()

    private var mCall: Call? = null
    private var httpRequest: Request? = null

    override fun request(params: IHttpRequestParams): IResponse {
        val processLock: ProcessLock? = ProcessLock.tryLock(lockName, true)
        val response: Response
        if (isEmpty(feature.downloadFileSavePath)) {
            throw HttpException(msg = "download stopped! file path can not be null")
        }

        progress(0, 0)

        //尝试获取文件锁,如果不能获取说明有其他线程在下载此文件
        try {
            if (processLock == null || !processLock.isValid) {
                throw HttpException(msg = "download exists: " + feature.downloadFileSavePath)
            }

            // 处理[断点逻辑1](见文件头doc)
            var range: Long = 0
            if (feature.isAutoResume()) {
                val tempFile = File(tempSaveFilePath)
                val fileLen = tempFile.length()
                range = if (fileLen <= CHECK_SIZE) {
                    tempFile.deleteRecursively()
                    0
                } else {
                    fileLen - CHECK_SIZE
                }
            }
            // retry 时需要覆盖RANGE参数
            params.addHeader("RANGE", "bytes=$range-")

            progress(0, 0)

            response = OkHttpClientDelegate.get(params, this) { call, request ->
                mCall = call
                httpRequest = request
            }

            if (feature.isAutoRename()) {
                responseFileName = getResponseFileName(response)
            }

            if (isAutoResume) {
                isAutoResume = isSupportRange(response)
            }
            progress(0, 0)
        } catch (throwable: Throwable) {
            throw throwable
        } finally {
            processLock?.release()
        }

        return OkHttpDownloadResponse(response)
    }

    override fun parseResult(rsp: IResponse): File {
        val input = rsp.bodyByStream ?: throw HttpException(msg = "从网络获取的inputStream为null")
        val targetFile: File = tempSaveFilePath.createFile()
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        try {
            // 处理[断点逻辑2](见文件头doc)
            val targetFileLen = targetFile.length()
            if (isAutoResume && targetFileLen > 0) {
                var fis: FileInputStream? = null
                try {
                    val filePos = targetFileLen - CHECK_SIZE
                    if (filePos > 0) {
                        fis = FileInputStream(targetFile)
                        val fileCheckBuffer = ByteArray(CHECK_SIZE)
                        val checkBuffer = ByteArray(CHECK_SIZE)
                        fis.read(fileCheckBuffer, filePos.toInt(), CHECK_SIZE)
                        input.read(checkBuffer, 0, CHECK_SIZE)
                        if (!Arrays.equals(checkBuffer, fileCheckBuffer)) {
                            closeQuietly(fis) // 先关闭文件流, 否则文件删除会失败.
                            targetFile.deleteRecursively()
                            throw RuntimeException("need retry")
                        }
                    } else {
                        targetFile.deleteRecursively()
                        throw RuntimeException("need retry")
                    }
                } catch (e: Exception) {
                    targetFile.deleteRecursively()
                } finally {
                    closeQuietly(fis)
                }
            }

            // 开始下载
            var current: Long = 0
            val fileOutputStream = if (isAutoResume) {
                current = targetFileLen
                FileOutputStream(targetFile, true)
            } else {
                FileOutputStream(targetFile)
            }

            val total = rsp.contentLength + current
            bis = BufferedInputStream(input)
            bos = BufferedOutputStream(fileOutputStream)

            progress(current, total)

            val tmp = ByteArray(512)
            var len = bis.read(tmp)

            LogUtil.e(TAG, "rsp.contentLength = ${rsp.contentLength}")

//            var p = tmp.copyOfRange(0, 50)
//            LogUtil.e(TAG, p.joinToString())

            while (len != -1) {
                // 防止父文件夹被其他进程删除, 继续写入时造成父文件夹变为0字节文件的问题.
                if (!targetFile.parentFile.exists()) {
                    targetFile.parentFile.mkdirs()
                    throw IOException("parent be deleted!")
                }

                bos.write(tmp, 0, len)
                current += len.toLong()

                progress(current, total)
                len = bis.read(tmp)

//                if (10757938 - current in 1..511) {
//                    var p = tmp
//                    if (len > 50) {
//                        p = p.copyOfRange(len - 50, len)
//                    }
//                    LogUtil.e(TAG, p.joinToString())
//                }
            }
            bos.flush()

            progress(current, total)
        } finally {
            closeQuietly(bis)
            closeQuietly(bos)
        }
        return autoRename(targetFile)
    }

    override fun cancel(immediately: Boolean) {
        if (!hasCancelled() && immediately) {
            mCall?.cancel()
        }
    }

    /**尝试从response中获取文件名称*/
    private fun getResponseFileName(request: Response?): String? {
        if (request == null) return null
        val disposition = request.header("Content-Disposition")
        if (isEmpty(disposition)) {
            return null
        }
        var startIndex = disposition!!.indexOf("filename=")
        if (startIndex > 0) {
            startIndex += 9 // "filename=".length()
            var endIndex = disposition.indexOf(";", startIndex)
            if (endIndex < 0) {
                endIndex = disposition.length
            }
            if (endIndex > startIndex) {
                try {
                    return URLDecoder.decode(
                            disposition.substring(startIndex, endIndex),
                            params.getCharset())
                } catch (ex: UnsupportedEncodingException) {
                    LogUtil.e(ex, ex.message)
                }
            }
        }
        return null
    }

    /**是否支持断点续传*/
    private fun isSupportRange(response: Response?): Boolean {
        if (response == null) return false
        var ranges: String? = response.header("Accept-Ranges")
        if (ranges != null) {
            return ranges.contains("bytes")
        }
        ranges = response.header("Content-Range")
        return ranges != null && ranges.contains("bytes")
    }

    // 处理[下载逻辑2.b](见文件头doc)
    private fun autoRename(loadedFile: File): File {
        return if (feature.isAutoRename() && loadedFile.exists() && !isEmpty(responseFileName)) {
            var newFile = File(loadedFile.parent, responseFileName)
            while (newFile.exists()) {
                newFile = File(loadedFile.parent, "${System.currentTimeMillis()}$responseFileName")
            }
            if (loadedFile.renameTo(newFile)) newFile else loadedFile
        } else if (feature.downloadFileSavePath != tempSaveFilePath) {
            val newFile = File(feature.downloadFileSavePath)
            if (loadedFile.renameTo(newFile)) newFile else loadedFile
        } else {
            loadedFile
        }
    }
}