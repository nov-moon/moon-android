package com.meili.moon.sdk.page.demo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.meili.moon.sdk.log.log
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.RainbowFragment
import com.meili.moon.sdk.page.annotation.Layout
import com.meili.moon.sdk.page.internal.utils.addMenuText
import com.meili.moon.sdk.page.internal.utils.onResult
import com.meili.moon.sdk.util.onClick
import kotlinx.android.synthetic.main.home_fragment.*


/**
 * Created by imuto on 2019-08-14.
 */
@Layout(R.layout.home_fragment)
class HomeFragment : RainbowFragment() {
    override fun getLayoutResId(): Int {
        return super.getLayoutResId()
    }
    /**
     * 当页面已经创建 回调方法
     */
    override fun onPageCreated(view: View, savedInstanceState: Bundle?) {
        setTitle("PageDemo")
        setBackText("返回")


        addMenuText("测试") {

        }

        mBtnInterceptorFragment.onClick {
            gotoPage<Boolean>("target") {
                it.log()
            }

            gotoPage("page") {
                putExtra("from", "test")
                onResult<Boolean> { result ->
                    // 回调结果
                }
            }
        }

        mBtnInterceptorActivity.onClick {
            gotoActivityForResult(InterceptorTargetActivity::class) {
                it.getStringExtra("isResult").log()
            }
        }

        mBtnPageStates.onClick {
            gotoPage("pageState")
        }

        mBtnHttp.onClick {
            gotoPage("http://www.baidu.com")
        }

        mBtnAppUrl.onClick {
            gotoPage("meili://target")
        }

        mBtnOpenBrowser.onClick {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            val contentUrl = Uri.parse("meili://target")
            intent.data = contentUrl
            startActivity(intent)
        }

        mBtnOpenOtherGroup.onClick {
            gotoPage("otherGroup")
        }

        mBtnOpenSamePage.onClick {
            gotoPage("samePage1")
        }

        mBtnOpenSamePageInSuccession.onClick {
            gotoPage("samePage3")
        }

        mBtnShowNotification.onClick {
            gotoPage("pageNotification")
        }

    }
}