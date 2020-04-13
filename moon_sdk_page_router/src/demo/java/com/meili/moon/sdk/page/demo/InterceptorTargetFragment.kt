package com.meili.moon.sdk.page.demo

import android.os.Bundle
import android.view.View
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.RainbowFragment
import com.meili.moon.sdk.page.annotation.Layout
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.interceptor_target_fragment.*

/**
 * Created by imuto on 2019-08-14.
 */
@Layout(R.layout.interceptor_target_fragment)
class InterceptorTargetFragment : RainbowFragment() {
    /**
     * 当页面已经创建 回调方法
     */
    override fun onPageCreated(view: View, savedInstanceState: Bundle?) {
        setTitle("PageDemo")

        mBtnInterceptor.onClick {
            setResult(false)
            finish()
        }
    }
}