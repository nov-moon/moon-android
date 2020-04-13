package com.meili.moon.sdk.demo

import android.app.Activity
import android.os.Bundle
import com.meili.moon.sdk.demo2.TestLog
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.log.Logcat
import com.meili.moon.sdk.log.log
import kotlinx.android.synthetic.main.log_activity.*

class LogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.log_activity)

        mBtnLogUtil.setOnClickListener {
            LogUtil.d("logUtil")
        }
        mBtnLogcat.setOnClickListener {
            Logcat.d("logcat")
        }
        mBtnLogExtra.setOnClickListener {
            "extra".log("好的")
        }
        mBtnLogExtra2.setOnClickListener {
            "extra".log()
        }
        mBtnLogHeaderInfo.setOnClickListener {
            TestLog.log()
        }
    }
}