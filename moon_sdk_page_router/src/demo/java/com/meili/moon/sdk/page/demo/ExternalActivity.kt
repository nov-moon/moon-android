package com.meili.moon.sdk.page.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.internal.utils.gotoPage
import com.meili.moon.sdk.util.post

/**
 * Created by imuto on 2019-08-30.
 */
class ExternalActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.external_activity)
        intent.log()

        val uri = intent.dataString ?: ""
        if (uri.isNotEmpty()) {
            gotoPage(uri)
        }
        post { finish() }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent.log()
    }
}