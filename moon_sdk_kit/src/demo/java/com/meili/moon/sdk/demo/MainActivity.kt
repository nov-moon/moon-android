package com.meili.moon.sdk.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.util.DefComponentInstaller
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mBtnOpenLog.setOnClickListener {
            val intent = Intent(this@MainActivity, LogActivity::class.java)
            startActivity(intent)
        }

        mBtnOpenAop.setOnClickListener {
            DefComponentInstaller.printList()
        }

    }
}
