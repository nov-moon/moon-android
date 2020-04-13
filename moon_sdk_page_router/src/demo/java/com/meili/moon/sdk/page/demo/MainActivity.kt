package com.meili.moon.sdk.page.demo

import android.app.Activity
import android.os.Bundle
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.internal.PageManagerImpl

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        "pageStart -> 刚开始".log()
        val pageIntent = PageIntent("home")
        PageManagerImpl.gotoPage(pageIntent)
//        post(100) {
//            finish()
//            overridePendingTransition(R.anim.splash_enter, R.anim.splash_in)
//        }
    }

    override fun onResume() {
        super.onResume()
        finish()
        overridePendingTransition(R.anim.splash_enter, R.anim.splash_in)
    }

}