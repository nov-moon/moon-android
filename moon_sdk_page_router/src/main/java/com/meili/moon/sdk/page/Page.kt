package com.meili.moon.sdk.page

import android.app.Activity
import com.meili.moon.sdk.common.Interactive


/**
 * 页面
 * Created by imuto on 2018/3/30.
 */
interface Page : BasePage, Interactive {

    companion object {
        const val RESULT_OK = Activity.RESULT_OK
    }

    /**持有本页面的containerId*/
    var containerId: Long

    /**请求返回数据的code*/
    var requestCode: Int

    /**页面名称*/
    val pageName: String

    /**页面昵称*/
    val nickName: String

    /**页面动画管理器*/
    var pageAnimators: PageAnimators?

    /**是否是子页面，如果是子页面一般情况下会禁用页面动画相关内容*/
    val isChildPage: Boolean

    /**获取上一个页面的引用*/
    fun getPrePage(isSameContainer: Boolean): Page?

    /**设置成功结果，会在启动页面的回调中获得*/
    fun setResult(result: Any = "")

    /**清除结果，启动页面将不会得到回调*/
    fun clearResult()

    /**获取成功结果*/
    fun getResult(): Any?
}