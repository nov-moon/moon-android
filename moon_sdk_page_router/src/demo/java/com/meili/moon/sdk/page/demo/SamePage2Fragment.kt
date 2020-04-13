package com.meili.moon.sdk.page.demo

import android.os.Bundle
import android.view.View
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.RainbowFragment
import com.meili.moon.sdk.page.annotation.Layout
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.other_group_fragment.*

/**
 * Created by imuto on 2019-08-14.
 */
@Layout(R.layout.other_group_fragment)
class SamePage2Fragment : RainbowFragment() {
    /**
     * 当页面已经创建 回调方法
     */
    override fun onPageCreated(view: View, savedInstanceState: Bundle?) {
        setTitle("SamePage2")

        mBtnGroup.text = "打开Next页面"

        arguments?.getString("url")

        mBtnGroup.onClick {
            gotoPage("samePage1")
        }
    }
}