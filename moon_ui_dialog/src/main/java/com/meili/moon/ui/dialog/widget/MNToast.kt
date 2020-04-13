package com.meili.moon.ui.dialog.widget

import android.app.Application
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.meili.moon.ui.dialog.R
import com.meili.moon.ui.dialog.util.Preconditions
import org.w3c.dom.Text

/**
 * Author： fanyafeng
 * Date： 18/1/15 下午5:48
 * Email: fanyafeng@live.cn
 *
 * Toast工具类
 */
object MNToast {

    var toast: Toast? = null

    const val GRAVITY_DEFAULT = -100

    /**
     *
     * context:上下文对象，非内部匿名工具类可直接使用this
     * charSequence:和用户交互的文字
     * view:自定义toast的view，如果使用自定义view，charSequence失效，可以设置为空串
     * duration:toast显示时间
     * gravity:toast显示位置
     * xOffset:toast x轴偏移量
     * yOffset:toast y轴偏移量
     */
    @JvmStatic
    @JvmOverloads
    fun show(context: Context, charSequence: CharSequence = "", view: View? = null, duration: Int = Toast.LENGTH_SHORT, gravity: Int = GRAVITY_DEFAULT, xOffset: Int = 0, yOffset: Int = 0) {
        toast?.cancel()
        Preconditions.checkNotNull(charSequence, "charSequence is null")
        toast = Toast.makeText(context, charSequence, duration)
        if (gravity != GRAVITY_DEFAULT) {
            toast!!.setGravity(gravity, xOffset, yOffset)
        }
        if (view != null) {
            toast!!.view = view
        }
        toast!!.show()
    }

    @JvmStatic
    fun show(context: Context, view: View?) {
        show(context, "", view)
    }

    @JvmStatic
    fun show(context: Context, icon: Int, message: CharSequence) {
        var toastView = LayoutInflater.from(context).inflate(R.layout.mn_toast_notification, null)
        toastView.findViewById<ImageView>(R.id.mnIvToast).setImageResource(icon)
        toastView.findViewById<TextView>(R.id.mnTvToast).text = message
        show(context, toastView)
    }

    @JvmStatic
    fun showCenter(context: Context, charSequence: CharSequence) {
        show(context, charSequence, null, Toast.LENGTH_SHORT, Gravity.CENTER)
    }

    @JvmStatic
    fun showCenter(context: Context, icon: Int, message: CharSequence) {
        var toastView = LayoutInflater.from(context).inflate(R.layout.mn_toast_notification, null)
        if (icon != 0)
            toastView.findViewById<ImageView>(R.id.mnIvToast).setImageResource(icon)
        if (message != null)
            toastView.findViewById<TextView>(R.id.mnTvToast).text = message
        show(context, "", toastView, Toast.LENGTH_SHORT, Gravity.CENTER)
    }

}