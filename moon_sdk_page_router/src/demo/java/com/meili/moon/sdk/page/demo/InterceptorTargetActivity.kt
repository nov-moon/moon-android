package com.meili.moon.sdk.page.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.interceptor_target_activity.*

/**
 * Created by imuto on 2019-08-15.
 */
class InterceptorTargetActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.interceptor_target_activity)

        mBtnInterceptor.onClick {
            val result = Intent()
            result.putExtra("isResult", "I'm result!!")
            setResult(RESULT_OK, result)
            finish()
        }
    }
}