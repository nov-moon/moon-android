package com.meili.moon.sdk.app.util

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.ui.dialog.widget.MNToast

object ToastUtil {
    const val STATUS_Normal: Int = 0
    const val STATUS_SUCCESS: Int = 1
    const val STATUS_Warning: Int = 2
    const val STATUS_Failed: Int = 3

    private var mConfig: Config = Config()

    fun initialize(config: Config) {
        mConfig = config
    }


    fun showNormal(msg: String, len: Int = Toast.LENGTH_SHORT) {
        show(msg, mConfig.statusNormalRes, len)
    }

    fun showSuccess(msg: String, len: Int = Toast.LENGTH_SHORT) {
        show(msg, mConfig.statusSuccessRes, len)
    }

    fun showWarning(msg: String, len: Int = Toast.LENGTH_SHORT) {
        show(msg, mConfig.statusWarningRes, len)
    }

    fun showFailed(msg: String, len: Int = Toast.LENGTH_SHORT) {
        show(msg, mConfig.statusFailedRes, len)
    }

    fun show(msg: String?, statusResId: Int = View.NO_ID, len: Int = Toast.LENGTH_SHORT, gravity: Int = mConfig.gravity) {
        var showTime: Int
        if (isEmpty(msg)) {
            return
        } else {
            showTime = (msg!!.length * 0.06).toInt()
            if (Math.max(showTime, 5) > 5) {
                showTime = 5
            }

            if (Math.min(showTime, 2) < 2) {
                showTime = 2
            }
        }

        if (statusResId != View.NO_ID) {
            MNToast.show(Sdk.app(), statusResId, msg)

            val toastView = LayoutInflater.from(Sdk.app()).inflate(R.layout.mn_toast_notification, null)
            toastView.findViewById<ImageView>(R.id.mnIvToast).setImageResource(statusResId)
            toastView.findViewById<TextView>(R.id.mnTvToast).text = msg

            MNToast.show(Sdk.app(), view = toastView, duration = showTime, gravity = gravity)
        } else {
            val toastView = LayoutInflater.from(Sdk.app()).inflate(R.layout.mn_toast_notification, null)
            toastView.findViewById<TextView>(R.id.mnTvToast).text = msg
            // MNToast.show(Sdk.app(), msg, duration = len, gravity = gravity)
            MNToast.show(Sdk.app(), view = toastView, duration = showTime, gravity = gravity)
        }
    }

    class Config {
        val statusNormalRes: Int = View.NO_ID
        val statusSuccessRes: Int = View.NO_ID
        val statusWarningRes: Int = View.NO_ID
        val statusFailedRes: Int = View.NO_ID
        //        val gravity: Int = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        val gravity: Int = Gravity.CENTER
    }
}
