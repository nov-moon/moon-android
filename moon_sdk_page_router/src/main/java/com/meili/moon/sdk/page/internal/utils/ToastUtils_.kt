package com.meili.moon.sdk.page.internal.utils

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.util.app
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.ui.dialog.widget.MNToast

/**
 * Created by imuto on 2019-08-14.
 */


fun showToast(msg: String?) {
    msg ?: return
    show(msg)
}

fun showToastNormal(msg: String?, len: Int = Toast.LENGTH_SHORT) {
    msg ?: return
    show(msg, len)
}

fun showToastSuccess(msg: String?, len: Int = Toast.LENGTH_SHORT) {
    msg ?: return
    show(msg, len)
}

fun showToastWarning(msg: String?, len: Int = Toast.LENGTH_SHORT) {
    msg ?: return
    show(msg, len)
}

fun showToastFailed(msg: String?, len: Int = Toast.LENGTH_SHORT) {
    msg ?: return
    show(msg, len)
}


fun show(msg: String?, statusResId: Int = View.NO_ID, len: Int = Toast.LENGTH_SHORT, gravity: Int = Gravity.CENTER) {
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
        MNToast.show(app, statusResId, msg)

        val toastView = LayoutInflater.from(app).inflate(R.layout.mn_toast_notification, null)
        toastView.findViewById<ImageView>(R.id.mnIvToast).setImageResource(statusResId)
        toastView.findViewById<TextView>(R.id.mnTvToast).text = msg

        MNToast.show(app, view = toastView, duration = showTime, gravity = gravity)
    } else {
        val toastView = LayoutInflater.from(app).inflate(R.layout.mn_toast_notification, null)
        toastView.findViewById<TextView>(R.id.mnTvToast).text = msg
        MNToast.show(app, view = toastView, duration = showTime, gravity = gravity)
    }
}