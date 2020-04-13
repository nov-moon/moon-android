package com.meili.moon.sdk.app.base.adapter.holders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.meili.moon.sdk.app.base.adapter.dataset.DataSet


/**
 * 接入了IViewHolder类
 * 这个类只是个壳，主要功能都在BaseViewHolder中
 */
abstract class AbsViewHolder<Item> : BaseViewHolder, IViewHolder<Item> {

    override val containerView: View?
        get() = itemView

    val data: Item
        get() = (dataSet as DataSet<Item>).getItem(dataPosition)

    /**
     * 必须重写此构造器,调用其他构造器来传递 View
     * View 必须传进来,否则会抛出异常
     *
     * @param context
     */
    constructor(context: Context, parent: ViewGroup?) : super(null, parent)

    protected constructor(context: Context, parent: ViewGroup?, layout: Int) :
            this(context, parent, LayoutInflater.from(context).inflate(layout, parent, false))

    protected constructor(context: Context, parent: ViewGroup?, itemView: View) : super(context, parent, itemView)
}
