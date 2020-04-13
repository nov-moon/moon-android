package com.meili.moon.sdk.app.base.adapter;


import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder;

/**
 * Created by imuto on 15/12/1.
 */
public interface ViewHolderCreator<T> {

    public int getItemViewType(int position, T data);

    public Class<? extends AbsViewHolder> getItemViewHolder(int viewType);

}
