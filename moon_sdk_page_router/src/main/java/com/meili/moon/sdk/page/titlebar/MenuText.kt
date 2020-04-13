package com.meili.moon.sdk.page.titlebar

import android.content.Context
import android.graphics.Color
import android.support.annotation.ColorInt
import com.meili.moon.sdk.page.R
import kotlinx.android.synthetic.main.rainbow_title_bar_menu_right_text.*


/**
 * 文本menu
 */
class MenuText(context: Context) : Menu() {

    val text: String
        get() = if (null != mTxtTitleMenu.text) {
            mTxtTitleMenu.text.toString()
        } else {
            ""
        }

    init {
        redTips = mImgTitleRedPoint

        val typedArray = context
                .obtainStyledAttributes(null, R.styleable.RainbowTitleBarView, R.attr.rainbowTitleBarView, 0)
        setTextColor(typedArray.getColor(R.styleable.RainbowTitleBarView_rainbowTitleBarMenuTextColor, Color.BLACK))
        typedArray.recycle()
    }

    override fun getLayoutResId() = R.layout.rainbow_title_bar_menu_right_text

    fun setText(text: CharSequence) {
        this.mTxtTitleMenu.text = text
    }

    fun setTextColor(@ColorInt textColor: Int) {
        this.mTxtTitleMenu.setTextColor(textColor)
    }
}
