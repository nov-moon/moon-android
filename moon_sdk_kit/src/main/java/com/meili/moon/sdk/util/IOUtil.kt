/**
 * io操作工具
 * Created by imuto on 17/11/24.
 */
@file:JvmName("IOUtil")

package com.meili.moon.sdk.util

import java.io.*
import java.nio.charset.Charset

/**读取inputStream为字符串*/
fun InputStream.readText(charset: String = "UTF-8"): String {
    val sb = StringBuilder()
    this.bufferedReader(Charset.forName(charset))
            .forEachLine {
                sb.append(it)
            }
    return sb.toString()
}

/**
 * 在给定的Stream中读取指定字节数
 *
 * 在给定的inputStream中读取[size]个字节，也可以指定跳过[skip]个字节后开始读取
 */
@Throws(IOException::class)
fun InputStream.readBytes(size: Long, skip: Long = 0): ByteArray {
    var skipVar = skip
    var out: ByteArrayOutputStream? = null
    try {
        if (skipVar > 0) {
            var skipSize: Long = this.skip(skipVar)
            while (skipVar > 0 && skipSize > 0) {
                skipVar -= skipSize
                skipSize = this.skip(skipVar)
            }
        }
        out = ByteArrayOutputStream()
        for (i in 0 until size) {
            out.write(this.read())
        }
    } finally {
        closeQuietly(out)
    }
    return out!!.toByteArray()
}

/**关闭对象，带有try-catch*/
fun closeQuietly(closeable: Closeable?) {
    if (closeable == null) {
        return
    }
    try {
        closeable.close()
    } catch (ignored: Throwable) {
        ignored.printStackTrace()
    }
}

/**关闭对象，带有try-catch*/
fun Closeable?.closeIt() {
    this ?: return
    try {
        this.close()
    } catch (ignored: Throwable) {
        ignored.printStackTrace()
    }
}

/**获取文件的大小，如果是文件夹则将子文件循环加到一块*/
fun File.lengthRecursively(fileFilter: ((File) -> Boolean)? = null): Long {
    if (!exists()) return 0
    if (!isDirectory) return length()

    val listFiles = listFiles(fileFilter) ?: return 0
    return listFiles
            .map { it.lengthRecursively(fileFilter) }
            .sum()
}

/**创建一个文件*/
@Throws(IOException::class)
fun String.createFile(): File {
    val targetFile = toFile()
    if (targetFile.isDirectory) {
        targetFile.deleteRecursively()
    }
    if (!targetFile.exists()) {
        if (targetFile.parentFile.exists() || targetFile.parentFile.mkdirs()) {
            targetFile.createNewFile()
        } else {
            throw IOException("无法创建文件")
        }
    }
    return targetFile
}


/**创建一个文件，不做文件检查*/
@Throws(IOException::class)
fun String.toFile() = File(this)

