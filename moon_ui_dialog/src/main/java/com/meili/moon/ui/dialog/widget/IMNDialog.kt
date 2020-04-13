package com.meili.moon.ui.dialog.widget

/**
 * Author： fanyafeng
 * Date： 18/3/26 下午6:37
 * Email: fanyafeng@live.cn
 */
interface IMNDialog {
    /**
     * 显示dialog
     */
    fun show()

    /**
     * 隐藏dialog
     */
    fun hide()

    /**
     * 消失dialog
     */
    fun dismiss()

    /**
     * 取消dialog
     */
    fun cancel()

    /**
     * dialog是否显示
     */
    fun isShowing(): Boolean

    /**
     * 是否拦截返回事件
     */
    fun setResponseBackPress(isResponseBackPress: Boolean)
}