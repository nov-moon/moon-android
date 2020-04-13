package com.meili.moon.sdk.page.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.interceptor_activity.*

/**
 * Created by imuto on 2019-08-15.
 */
class InterceptorActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.interceptor_activity)

        mBtnInterceptor.onClick {
            val result = Intent()
            result.putExtra("isOk", true)
            result.putExtra("isResult", "I'm error invoke~")
            setResult(RESULT_OK, result)
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        "I'm from Interceptor!!".log()
        if (data != null) {
            data.getStringExtra("isResult").log()
        }
    }
}