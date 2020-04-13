package com.meili.moon.sdk.util

import com.meili.moon.sdk.CommonSdk

/** 获取statusBar高度  */
val statusBarHeight: Int
    get() {
        var result = 0
        val resourceId = CommonSdk.app().resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = CommonSdk.app().resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

val screenWidth: Int by lazy {
    val width = CommonSdk.app().resources.displayMetrics.widthPixels
    val height = CommonSdk.app().resources.displayMetrics.heightPixels
    return@lazy Math.min(width, height)
}

val screenHeight: Int by lazy {
    val width = CommonSdk.app().resources.displayMetrics.widthPixels
    val height = CommonSdk.app().resources.displayMetrics.heightPixels
    return@lazy Math.max(width, height)
}
