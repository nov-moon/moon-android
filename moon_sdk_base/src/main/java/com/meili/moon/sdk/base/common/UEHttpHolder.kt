package com.meili.moon.sdk.base.common

import com.meili.moon.sdk.common.IDestroable

/**
 * ui和http的交互接口
 *
 * Created by imuto on 2018/10/23.
 */
interface UEHttpHolder : IDestroable {
    /**获取交互的delay时间，例如页面动画的剩余事件，此时间会影响到http请求的回调时机*/
    fun getUEDelayMills(): Long

    /**显示一个error的message，[ueType]为发起请求是定义的type*/
    fun showUEErrorMessage(msg: String?, ueType: Int)

    /**显示一个加载的进度，[ueType]为发起请求是定义的type*/
    fun showUEProgress(msg: String?, ueType: Int)

    /**取消一个加载的进度，[ueType]为发起请求是定义的type*/
    fun dismissUEProgress(ueType: Int)

    /**当前是否可交互*/
    var isUEnable: Boolean
}
