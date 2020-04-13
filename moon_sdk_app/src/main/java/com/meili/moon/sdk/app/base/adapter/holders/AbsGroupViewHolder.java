package com.meili.moon.sdk.app.base.adapter.holders;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * 接入了IViewHolderGroup类
 * 这个类只是个壳，主要功能都在BaseViewHolder中
 */
public abstract class AbsGroupViewHolder<Group> extends BaseViewHolder implements IViewHolderGroup<Group> {

    /**
     * 必须重写此构造器,调用其他构造器来传递 View
     * View 必须传进来,否则会抛出异常
     * @param context
     */
    public AbsGroupViewHolder(Context context, @Nullable ViewGroup parent) {
        super(null, parent);
    }

    protected AbsGroupViewHolder(Context context, @Nullable ViewGroup parent, int layout) {
        this(context, parent, LayoutInflater.from(context).inflate(layout, parent, false));
    }

    protected AbsGroupViewHolder(Context context, @Nullable ViewGroup parent, View itemView) {
        super(context, parent, itemView);
    }
}
