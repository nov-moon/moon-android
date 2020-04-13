package com.meili.moon.sdk.common

/**
 * 当前对象是否已经被销毁，可销毁对象的标准化接口
 *
 * Created by imuto on 2018/7/3.
 */
interface IDestroable {
    /**
     * 当前对象是否已经被销毁，true 已销毁，false 未销毁
     */
    var hasDestroyed: Boolean
}