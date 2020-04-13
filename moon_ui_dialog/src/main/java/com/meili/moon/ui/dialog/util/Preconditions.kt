package com.meili.moon.ui.dialog.util

/**
 * Author： fanyafeng
 * Date： 17/12/27 上午10:51
 * Email: fanyafeng@live.cn
 */
object Preconditions {
    @JvmStatic
    fun <T> checkNotNull(reference: T?, errorMessage: Any): T {
        if (reference == null) {
            throw NullPointerException(errorMessage.toString())
        }
        return reference
    }
}
