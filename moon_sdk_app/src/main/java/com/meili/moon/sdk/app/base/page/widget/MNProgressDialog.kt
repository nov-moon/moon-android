package com.meili.moon.sdk.app.base.page.widget

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import android.view.LayoutInflater
import android.widget.TextView
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.base.util.OnNormalCallback
import com.meili.moon.ui.dialog.config.MNDialogConfig
import com.meili.moon.ui.dialog.widget.MNDialog
import com.meili.moon.widget.MNLoadingView

/**
 * Created by imuto on 2018/6/21.
 */
class MNProgressDialog(val context: Context?) {

    private var progressDialog: MNDialog? = null

    private var mTxtMsg: TextView? = null
    private var mLoadingView: MNLoadingView? = null
    //默认消失延迟时长
    private val mDefault = 1000L

    /**
     * 成功状态
     * @param dismissDelay 单位ms
     */
    fun showSuccess(message: CharSequence = "加载成功", dismissDelay: Long = mDefault) {
        if (progressDialog != null) {
            mLoadingView?.showSuccess()
            mLoadingView?.setOnLoadFinishListener {
                mTxtMsg?.text = message
                mLoadingView?.postDelayed({ dismiss() }, dismissDelay)
            }
        }
    }

    /**
     * 失败状态
     * @param dismissDelay 单位ms
     */
    fun showError(message: CharSequence = "加载失败", dismissDelay: Long = mDefault) {
        mLoadingView?.showError()
        mLoadingView?.setOnLoadFinishListener {
            mTxtMsg?.text = message
            mLoadingView?.postDelayed({ dismiss() }, dismissDelay)
        }
    }

    fun show(message: CharSequence = "请稍等..") {
        if (null == context) {
            return
        }
        val dialog = if (progressDialog == null) {
            val view = LayoutInflater.from(context).inflate(R.layout.mn_dialog_layout_progress, null)
            mTxtMsg = view.findViewById(R.id.tvLoading)
            mLoadingView = view.findViewById(R.id.mnLoadingView)
//            mLoadingView?.statusColor = Color.GRAY

            val config = MNDialogConfig.Builder()
                    .setThemeResId(R.style.MNProgressDialog)
                    .setContext(context)
                    .setContentView(view)
                    .build()

            val innerProgressDialog = MNDialog(config)
            innerProgressDialog.setOnDismissListener(object : MNDialog.OnDismissListener {
                override fun onDismiss() {
                    mLoadingView?.showLoading()
                }
            })
            progressDialog = innerProgressDialog
            innerProgressDialog
        } else progressDialog

        mTxtMsg?.text = message
        if (null == dialog) {
            return
        }
        if (!dialog.isShowing()) {
            dialog.showCenter()
        }
    }

    fun isShowing() = progressDialog?.isShowing() ?: false

    fun dismiss() {
        if (!isShowing()) return

        if (context is Activity) {
            if (!context.isFinishing && !context.isDestroyed) {
                progressDialog?.dismiss()
            }
        } else {
            progressDialog?.dismiss()
        }

    }

    fun setProgressDialogText(msg: CharSequence) {
        mTxtMsg?.text = msg
    }

    fun setOnDismissListener(lis: OnNormalCallback) {
        progressDialog?.setOnDismissListener(object : MNDialog.OnDismissListener {
            override fun onDismiss() {
                lis?.invoke()
            }
        })
    }
}