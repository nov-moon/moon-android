package com.meili.moon.sdk.app.base.role

/**
 * Author wudaming
 * Created on 2018/9/26
 */
interface IRoleStrategy<T> : LifecycleBridge, UIBridge {
    fun verifyData(data: Any?=null): Boolean = true

    fun bindData(data:Any){}
}