package com.meili.moon.ui.dialog.widget

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.WindowManager
import com.meili.moon.ui.dialog.config.MNDialogConfig
import com.meili.moon.ui.dialog.util.Preconditions

/**
 * Author： fanyafeng
 * Date： 18/1/12 下午2:18
 * Email: fanyafeng@live.cn
 */
class MNBaseDialog(private var mnDialogConfig: MNDialogConfig) : Dialog(mnDialogConfig.context!!, mnDialogConfig.themeResId!!) {

    var isResponseBackPress: Boolean = false

    private var onBackPressListener: OnBackPressListener? = null

    fun setOnBackpressListener(onBackPressListener: OnBackPressListener) {
        this.onBackPressListener = onBackPressListener
    }

    interface OnBackPressListener {
        fun onBackPress()
    }

    private var onKeyDownListener: OnKeyDownListener? = null

    fun setOnKeyDownListener(onKeyDownListener: OnKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener
    }

    interface OnKeyDownListener {
        fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Preconditions.checkNotNull(mnDialogConfig.contentView, "dialogContentView is null")
        this.setContentView(mnDialogConfig.contentView)
        this.window.setGravity(mnDialogConfig.gravity!!)
        this.window.setWindowAnimations(mnDialogConfig.animation!!)
        var windowManager = this.window.attributes
        windowManager.width = ViewGroup.LayoutParams.MATCH_PARENT
        windowManager.height = ViewGroup.LayoutParams.WRAP_CONTENT
        this@MNBaseDialog.setCanceledOnTouchOutside(mnDialogConfig.isCancel)
    }

    override fun onBackPressed() {
        if (isResponseBackPress) {
            onBackPressListener?.onBackPress()
        } else {
            super.onBackPressed()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (onKeyDownListener != null) {
            return onKeyDownListener!!.onKeyDown(keyCode, event)
        }
        return super.onKeyDown(keyCode, event)
    }


}
