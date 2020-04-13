package com.meili.moon.sdk.page.internal

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.ViewGroup
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.internal.utils.TranslucentStatusBarUtils

/**
 * Created by imuto on 2019-08-13.
 */
class RainbowActivity : SdkActivity() {
    /**容器ID*/
    override var containerId: Int = R.id.mRainbowLayoutContainer

    override lateinit var container: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        window.setWindowAnimations(R.style.rainbowActivityAnimation)

        setContentView(R.layout.rainbow_container_activity)

        container = findViewById(containerId)
    }

    override fun onResume() {
        super.onResume()
        TranslucentStatusBarUtils.tryOpenTranslucentStatusBarStyle(this)
    }

    override fun onStartPage(intent: PageIntent): Boolean {
        return false
    }

    override fun onGotoPage(intent: PageIntent): Boolean {
        return false
    }


}