/**
 * String的工具类
 * Created by imuto on 17/11/27.
 */
@file:JvmName("StringUtil")

package com.meili.moon.sdk.util

import android.content.ClipData
import android.content.Context
import android.text.TextUtils
import com.meili.moon.sdk.CommonSdk


private val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f')

/**转换数组为16进制文本*/
fun ByteArray.toHexString(): String {
    val sb = StringBuilder()
    this.forEach {
        with(hexDigits) {
            sb.append(this[(it.toInt() shr 4) and 0x0F])
            sb.append(this[it.toInt() and 0x0F])
        }
    }
    return sb.toString()
}

/**
 * 字符串半角转换为全角
 *
 * 半角空格为32,全角空格为12288.
 * 其他字符半角(33-126)与全角(65281-65374)的对应关系是：均相差65248
 */
fun String.half2Full(): String {
    val c = toCharArray()
    for (i in c.indices) {
        if (c[i].toInt() == 32) { //半角空格
            c[i] = 12288.toChar()
            continue
        }
        if (c[i].toInt() in 33..126) {
            c[i] = (c[i].toInt() + 65248).toChar()
        }
    }
    return String(c)
}

/**
 * 字符串全角转换为半角
 *
 * 全角空格为12288，半角空格为32.
 * 其他字符全角(65281-65374)与半角(33-126)的对应关系是：均相差65248
 *
 */
fun String.fullToHalf(): String {
    val c = toCharArray()
    for (i in c.indices) {
        if (c[i].toInt() == 12288) { //全角空格
            c[i] = 32.toChar()
            continue
        }
        if (c[i].toInt() in 65281..65374) {
            c[i] = (c[i].toInt() - 65248).toChar()
        }
    }
    return String(c)
}

/**
 * 是否是以给定的字符串开头，只要满足一个就返回true
 */
fun CharSequence.startWith(vararg prefix: CharSequence, ignoreCase: Boolean = false): Boolean {
    prefix.forEach {
        if (startsWith(it, ignoreCase)) {
            return true
        }
    }
    return false
}

/***
 * 复制text到黏贴板
 * @return true 复制成功， false 复制失败
 */
fun CharSequence?.copy2Clipboard(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }
//    val app = context ?: CommonSdk.app()
//
//    if (app == null) return false

    try {
        val clipboardManager = CommonSdk.app().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clipData = ClipData.newPlainText(this, this)
        clipboardManager.primaryClip = clipData
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return true
}


/**
 * 当前字符串是否在粘贴板
 */
fun CharSequence?.inClipboard(): Boolean {
    if (TextUtils.isEmpty(this)) {
        return false
    }

    val clipboardManager = CommonSdk.app().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    // 获取剪贴板的剪贴数据集
    val clipData: ClipData? = clipboardManager.primaryClip

    if (clipData != null && clipData.itemCount > 0) {
        // 从数据集中获取（粘贴）第一条文本数据
        for (i in 0 until clipData.itemCount) {
            val text = clipData.getItemAt(i).text
            if (TextUtils.equals(this, text)) {
                return true
            }
        }
    }
    return false
}
