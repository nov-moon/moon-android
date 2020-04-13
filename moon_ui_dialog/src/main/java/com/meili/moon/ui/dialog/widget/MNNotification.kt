package com.meili.moon.ui.dialog.widget

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity.CENTER
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.meili.moon.ui.dialog.R
import kotlinx.android.synthetic.main.mn_notification_layout.view.*

/**
 * Author： fanyafeng
 * Date： 18/3/22 上午10:39
 * Email: fanyafeng@live.cn
 */
class MNNotification @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr) {

    private val DURATION_ANIM: Long = 400

    private val mDismissRunnable = Runnable {
        if (visibility != View.VISIBLE) {
            return@Runnable
        }
        dismiss()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.mn_notification_layout, this)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MNNotification, R.attr.mnNotificationStyle, R.style.mnNotificationStyle)

        val iconResourceId = typedArray.getResourceId(R.styleable.MNNotification_mnNotificationIcon, 0)
        val arrowResourceId = typedArray.getResourceId(R.styleable.MNNotification_mnNotificationArrow, 0)
        val messageText = typedArray.getString(R.styleable.MNNotification_mnNotificationMessage)
        val messageColor = typedArray.getColor(R.styleable.MNNotification_mnNotificationMessageTextColor, Color.WHITE)
        val messageTextSize = typedArray.getDimension(R.styleable.MNNotification_mnNotificationMessageTextSize, 12F)
        val backgroundColor = typedArray.getColor(R.styleable.MNNotification_mnNotificationBackgroundColor, Color.BLACK)

        setIcon(iconResourceId)
        setArrow(arrowResourceId)
        setMessage(messageText)
        setMessageColor(messageColor)
        setMessageTextSize(messageTextSize)
        setNotificationBackgroundColor(backgroundColor)

        typedArray.recycle()

        visibility = View.INVISIBLE
        setArrowVisibility(View.INVISIBLE)
    }

    fun show(msg: CharSequence? = null, delayDismissSecond: Int = 2) {
        if (visibility == View.VISIBLE) {
            return
        }
        if (msg != null) {
            setMessage(msg)
        }

        visibility = VISIBLE
        val translationBackground = ObjectAnimator.ofFloat(layoutNotification, "translationY",
                -layoutNotification.height.toFloat(), 0.0F)
        translationBackground.duration = DURATION_ANIM
        translationBackground.start()
        if (delayDismissSecond > 0) {
            setArrowVisibility(View.INVISIBLE)
            removeCallbacks(mDismissRunnable)
            postDelayed(mDismissRunnable, delayDismissSecond * 1000L + DURATION_ANIM)
        } else {
            setArrowVisibility(View.VISIBLE)
        }
    }

    fun dismiss() {
        if (visibility != View.VISIBLE) {
            return
        }

        val translationBackground = ObjectAnimator.ofFloat(layoutNotification, "translationY",
                0.0F, -layoutNotification.height.toFloat())
        translationBackground.duration = DURATION_ANIM
        translationBackground.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {
            }

            override fun onAnimationEnd(animation: Animator?) {
                visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {
            }

            override fun onAnimationStart(animation: Animator?) {
            }
        })
        translationBackground.start()
    }

    fun setMessageCenter() {
        mnNotificationMessage.gravity = CENTER
    }

    fun setMessage(message: CharSequence?) {
        mnNotificationMessage.text = message
    }

    fun setMessageColor(@ColorInt color: Int) {
        mnNotificationMessage.setTextColor(color)
    }

    fun setMessageTextSize(dp: Float) {
        mnNotificationMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, dp)
    }

    fun setMessageGravity(gravity: Int) {
        mnNotificationMessage.gravity = gravity
    }

    fun setNotificationBackgroundColor(@ColorInt color: Int) {
        layoutNotification.setBackgroundColor(color)
    }

    fun setIconVisibility(visibility: Int) {
        mnNotificationIcon.visibility = visibility
    }

    fun setIcon(@DrawableRes icon: Int) {
        mnNotificationIcon.setImageResource(icon)
    }

    fun setArrowVisibility(visibility: Int) {
        mnNotificationArrow.visibility = visibility
    }

    fun setArrow(@DrawableRes icon: Int) {
        mnNotificationArrow.setImageResource(icon)
    }

    fun setOnNotificationClickListener(lis: (View) -> Unit) {
        layoutNotification.setOnClickListener(lis)
    }
}