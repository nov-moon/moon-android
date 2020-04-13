package com.meili.moon.sdk.page.demo

import android.os.Bundle
import android.view.View
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.RainbowFragment
import com.meili.moon.sdk.page.annotation.Layout
import com.meili.moon.sdk.page.internal.utils.showNotification
import com.meili.moon.sdk.page.internal.utils.showNotificationFailed
import com.meili.moon.sdk.page.internal.utils.showNotificationSuccess
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.page_notification_fragment.*

/**
 * Created by imuto on 2019-08-14.
 */
@Layout(R.layout.page_notification_fragment)
class PageNotificationTestFragment : RainbowFragment() {
    /**
     * 当页面已经创建 回调方法
     */
    override fun onPageCreated(view: View, savedInstanceState: Bundle?) {
        setTitle("测试页面通知")
        var notificationIndex = 0

        mBtnPageNotification.onClick {
            when (notificationIndex) {
                0 -> showNotification("页面内通知示例", 0)
                1 -> showNotificationFailed("页面内通知示例")
                2 -> showNotificationSuccess("页面内通知示例")
            }
            notificationIndex ++
            if (notificationIndex == 3) {
                notificationIndex = 0
            }
        }
    }
}