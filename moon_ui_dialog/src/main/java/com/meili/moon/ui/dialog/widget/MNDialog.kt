package com.meili.moon.ui.dialog.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.meili.moon.ui.dialog.R
import com.meili.moon.ui.dialog.config.MNDialogConfig

/**
 * Author： fanyafeng
 * Date： 18/3/26 下午5:53
 * Email: fanyafeng@live.cn
 */
class MNDialog : IMNDialog {

    private var mnBaseDialog: MNBaseDialog? = null
    private var mnDialogConfig: MNDialogConfig? = null
    public var contentView: View? = null
        private set(value) {
            field = value
        }

    private var onBackPressListener: OnBackPressListener? = null
        private set(value) {
            field = value
        }

    private var ondissmissListener: OnDismissListener? = null
        private set(value) {
            field = value
        }

    fun setOnBackpressListener(onBackPressListener: OnBackPressListener) {
        this.onBackPressListener = onBackPressListener
    }

    interface OnDismissListener {
        fun onDismiss()
    }

    interface OnBackPressListener {
        fun onBackPress()
    }

    private var onKeyDownListener: OnKeyDownListener? = null

    interface OnKeyDownListener {
        fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean
    }

    @Deprecated(message = "下个版本废弃，请及时修改", replaceWith = ReplaceWith("constructor(context: Context, view: View)"))
    constructor()

    constructor(context: Context, view: View) {
        this.mnDialogConfig = MNDialogConfig.Builder()
                .setContext(context)
                .setThemeResId(R.style.MNDialogStyle)
                .setContentView(view)
                .build()
        mnBaseDialog = MNBaseDialog(mnDialogConfig!!)
        contentView = mnDialogConfig!!.contentView
    }

    constructor(mnDialogConfig: MNDialogConfig) {
        this.mnDialogConfig = mnDialogConfig
        mnBaseDialog = MNBaseDialog(this.mnDialogConfig!!)
        contentView = mnDialogConfig.contentView
    }

    override fun show() {
        mnBaseDialog?.show()
    }

    fun setGravity(gravity: Int): MNDialog {
        mnDialogConfig?.gravity = gravity
        return this
    }

    fun setCancel(isCancel: Boolean): MNDialog {
        mnDialogConfig?.isCancel = isCancel
        return this
    }

    fun setAnimation(animation: Int): MNDialog {
        mnDialogConfig?.animation = animation
        return this
    }

    @Deprecated(message = "下个版本废弃，请及时修改", replaceWith = ReplaceWith("showActionSheet()"))
    fun showActionSheet(context: Context, view: View): MNDialog {
        contentView = view
        mnDialogConfig = MNDialogConfig.Builder()
                .setContext(context)
                .setGravity(Gravity.BOTTOM)
                .setContentView(view)
                .setCancel(true)
                .setAnimation(R.style.MNMenuAnimation)
                .setThemeResId(R.style.MNDialogStyle)
                .build()
        mnBaseDialog = MNBaseDialog(mnDialogConfig!!)
        return this
    }

    fun showActionSheet() {
        mnDialogConfig?.gravity = Gravity.BOTTOM
        mnDialogConfig?.isCancel = true
        mnDialogConfig?.animation = R.style.MNMenuAnimation
        show()
    }

    @Deprecated(message = "下个版本废弃，请及时修改", replaceWith = ReplaceWith("showCenter()"))
    fun showCenter(context: Context, view: View): MNDialog {
        contentView = view
        mnDialogConfig = MNDialogConfig.Builder()
                .setContext(context)
                .setGravity(Gravity.CENTER)
                .setContentView(view)
                .setCancel(false)
                .setThemeResId(R.style.MNDialogStyle)
                .build()
        mnBaseDialog = MNBaseDialog(mnDialogConfig!!)
        return this
    }

    fun showCenter() {
        mnDialogConfig?.gravity = Gravity.CENTER
        mnDialogConfig?.isCancel = false
        show()
    }

    override fun hide() {
        mnBaseDialog?.hide()
    }

    override fun dismiss() {
        val context = (mnBaseDialog?.context as ContextWrapper).baseContext
        if (context is Activity) {
            if (!context.isFinishing && !context.isDestroyed){
                mnBaseDialog?.dismiss()
            }
        }else{
            mnBaseDialog?.dismiss()
        }
//        mnBaseDialog?.dismiss()
    }

    override fun cancel() {
        mnBaseDialog?.cancel()
    }

    override fun isShowing(): Boolean {
        return mnBaseDialog!!.isShowing
    }

    override fun setResponseBackPress(isResponseBackPress: Boolean) {
        mnBaseDialog!!.isResponseBackPress = isResponseBackPress

        mnBaseDialog!!.setOnBackpressListener(object : MNBaseDialog.OnBackPressListener {
            override fun onBackPress() {
                onBackPressListener?.onBackPress()
            }
        })
    }

    fun setOnKeyDownListener(onKeyDownListener: OnKeyDownListener) {
        this.onKeyDownListener = onKeyDownListener

        mnBaseDialog!!.setOnKeyDownListener(object : MNBaseDialog.OnKeyDownListener {
            override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
                return onKeyDownListener.onKeyDown(keyCode, event)
            }
        })
    }

    fun setOnDismissListener(onDismissListener: OnDismissListener) {
        this.ondissmissListener = onDismissListener
        mnBaseDialog!!.setOnDismissListener {
            if (ondissmissListener != null) {
                ondissmissListener!!.onDismiss()
            }
        }
    }


}