package com.meili.moon.sdk.app.base.adapter;


import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder;

/**
 * Created by imuto on 15/12/1.
 */
public interface IViewHolderCreatorGroupFooter<Group, Child> extends ViewHolderCreatorGroup<Group, Child> {

    public int getGroupFooterHolder(int position, int groupIndex, Group group);

    public Class<? extends AbsViewHolder> getGroupFooterHolder(int viewType);
}
