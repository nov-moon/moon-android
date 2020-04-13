package com.meili.moon.sdk.app.base.adapter.holders

/**
 * Created by imuto on 15/12/1.
 */
interface IViewHolder<in Item> {

    fun onBindViewHolder(data: Item)

    fun onClick(position: Int, data: Item) {
    }

    fun onLongClick(position: Int, data: Item): Boolean {
        return false
    }
}
