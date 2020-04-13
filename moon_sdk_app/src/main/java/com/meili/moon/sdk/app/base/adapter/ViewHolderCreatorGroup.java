package com.meili.moon.sdk.app.base.adapter;


import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder;

/**
 * Created by imuto on 15/12/1.
 */
public interface ViewHolderCreatorGroup<Group, Child> {

    int getGroupHolder(int position, int groupIndex, Group group);

    Class<? extends AbsViewHolder> getGroupHolder(int viewType);

    int getChildHolder(int position, int groupIndex, int childIndex, Child child);

    Class<? extends AbsViewHolder> getChildHolder(int viewType);
}
