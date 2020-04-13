package com.meili.moon.sdk.app.base.adapter.holders

/**
 * Created by imuto on 15/12/1.
 */
interface IViewHolderGroup<in Group> {

    fun onBindViewHolderGroup(group: Group)

    fun onClickGroup(position: Int, group: Group)

    fun onLongClickGroup(position: Int, group: Group): Boolean {
        return false
    }

}
