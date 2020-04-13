package com.meili.moon.sdk.page

import android.os.Bundle
import android.view.View

/**
 * 页面的声明周期
 *
 * Created by imuto on 2019-06-19.
 */
interface PageLifeCycle {

    /**
     * 当页面已经创建 回调方法
     */
    fun onPageCreated(view: View, savedInstanceState: Bundle?)

    /**
     * 当页面由后台跑到前台 回调方法
     */
    fun onPageStart()

    /**
     * 当页面由后台跑到前台 回调方法
     */
    fun onPageResume()

    /**
     * 当页面由前台跑到后台 回调方法
     */
    fun onPagePause()

    /**
     * 当页面由前台跑到后台，在[onPagePause]后,[onPageDestroyView]前 回调方法
     */
    fun onPageStop()

    /**
     * 当页面正在回收，在[onPageStop]后,[onPageDestroy]前 回调方法
     *
     * 此方法在page为dirty状态时，会被延迟调用，相对回收来说没有[onPageDestroy]可靠
     */
    fun onPageDestroyView()

    /**
     * 当页面正在回收，在[onPageDestroyView]后,[onPageDetach]前 回调方法
     *
     * 但是如果当前page只是被标记为了dirty，则会在[onPageDestroyView]方法前回调到这里。并且在总的生命周期中，现在会被回调两次
     */
    fun onPageDestroy()

    /**
     * 当页面正在回收，在[onPageDestroy]后 回调方法
     *
     * 此方法在page为dirty状态时，会被延迟调用，相对回收来说没有[onPageDestroy]可靠
     */
    fun onPageDetach()

    /**
     * 内存回收时，进行数据保存
     */
    fun onSaveInstanceState(outState: Bundle)

    /**
     * 进行数据恢复
     */
    fun onViewStateRestored(savedInstanceState: Bundle?)
}