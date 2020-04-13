package com.meili.moon.sdk.base.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.common.FinishLambda
import com.meili.moon.sdk.common.StartLambda
import com.meili.moon.sdk.util.isEmpty
import java.io.File

/**
 * Created by imuto on 2018/10/5.
 */

/**
 * 是否参数中有一个和当前对象相同，可通过canBeNull参数控制当前参数是否可为null。默认不可为null
 */
fun String?.equalOne(vararg items: String?, canBeNull: Boolean = false): Boolean {

    if (this == null && !canBeNull) {
        return false
    }

    items.forEach {
        if (this == it) return true
    }
    return false
}

/**转化为一个file文件*/
fun String?.toFile(): File? {
    return if (isEmpty(this)) {
        null
    } else {
        File(this)
    }
}

/**将文件当做html，抛给系统打开*/
fun File?.openAsHtml(onError: FinishLambda = null) {
    if (!isAvailable()) {
        onError?.invoke(false)
        return
    }
    val uri = Uri.Builder()
            .encodedAuthority("com.android.htmlfileprovider")
            .scheme("content")
            .encodedPath(this!!.absolutePath)
            .build()

    openFileByExtraApp("text/html", uri, onError)
}

/**将文件当做图片，抛给系统打开*/
fun File?.openAsImg(onError: FinishLambda = null) {
    openAsIntent("image/*", onError)
}

/**将文件当做pdf，抛给系统打开*/
fun File?.openAsPDF(onError: FinishLambda = null) {
    openAsIntent("application/pdf", onError)
}

/**将文件当做文本，抛给系统打开*/
fun File?.openAsTxt(onError: FinishLambda = null) {
    openAsIntent("text/plain", onError)
}

/**将文件当做音频，抛给系统打开*/
fun File?.openAsAudio(onError: FinishLambda = null) {
    openAsIntent("audio/*", onError)
}

/**将文件当做视频，抛给系统打开*/
fun File?.openAsVideo(onError: FinishLambda = null) {
    openAsIntent("video/*", onError)
}

/**将文件当做word，抛给系统打开*/
fun File?.openAsWord(onError: FinishLambda = null) {
    openAsIntent("application/msword", onError)
}

/**将文件当做excel，抛给系统打开*/
fun File?.openAsExcel(onError: FinishLambda = null) {
    openAsIntent("application/vnd.ms-excel", onError)
}

/**将文件当做ppt，抛给系统打开*/
fun File?.openAsPPT(onError: FinishLambda = null) {
    openAsIntent("application/vnd.ms-powerpoint", onError)
}

/**将文件当做apk，抛给系统打开*/
fun File?.openAsAPK(onError: FinishLambda = null) {
    openAsIntent("application/vnd.android.package-archive", onError)
}

fun File?.openAsIntent(type: String, onError: FinishLambda = null) {
    if (!isAvailable()) {
        onError?.invoke(false)
        return
    }
    openFileByExtraApp(type, Uri.fromFile(this), onError)
}

/**当当前进程是主进程时，执行[callback]*/
fun Context?.runOnMainProcess(callback: StartLambda) {
    this ?: return
    callback ?: return
    if (!Utils.isMainProcess(this)) {
        return
    }
    callback.invoke()
}

private fun File?.openFileByExtraApp(type: String, uri: Uri, onError: FinishLambda = null) {
    val intent = Intent("android.intent.action.VIEW")
    intent.addCategory(Intent.CATEGORY_DEFAULT)
    intent.setDataAndType(uri, type)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (!intent.isAvailable()) {
        onError?.invoke(false)
        return
    }
    try {
        Sdk.app().startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
        onError?.invoke(false)
    }
}

/**当前intent是否有效，也就是：是否可以匹配到activity*/
fun Intent?.isAvailable(): Boolean {
    if (this == null) return false
    val manager = Sdk.app().packageManager
    val result: MutableList<ResolveInfo>? = manager.queryIntentActivities(this, PackageManager.MATCH_DEFAULT_ONLY)
    return !isEmpty(result)
}

/**文件是否有效，不为null，存在，大小大于0*/
fun File?.isAvailable(): Boolean {
    return this != null && exists() && length() > 0
}