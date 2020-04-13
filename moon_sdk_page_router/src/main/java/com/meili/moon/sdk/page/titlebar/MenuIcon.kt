package com.meili.moon.sdk.page.titlebar

import android.content.Context
import com.meili.moon.sdk.page.R
import kotlinx.android.synthetic.main.rainbow_title_bar_menu_right_icon.*


/**
 * 图标menu
 */
class MenuIcon(context: Context) : Menu() {
    override fun getLayoutResId() = R.layout.rainbow_title_bar_menu_right_icon

    fun setImageResource(resId: Int) {
        mImgTitleMenu.setImageResource(resId)
    }

}
