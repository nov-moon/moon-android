package com.meili.moon.sdk.app.base.adapter.listener;


import com.meili.moon.sdk.app.base.adapter.holders.BaseViewHolder;

/**
 * Created by imuto on 16/3/14.
 */
public interface OnItemClickGroupListener<Group, Child> {
    boolean onItemChildClick(int childIndex, Child data, int groupIndex, BaseViewHolder viewHolder);
    boolean onItemGroupClick(int groupIndex, Group data, BaseViewHolder viewHolder);
}