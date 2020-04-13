package com.meili.moon.sdk.app.base.adapter.attachment;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.app.base.adapter.holders.BaseViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolderAttach;

import org.jetbrains.annotations.Nullable;


/**
 * Created by imuto on 15/12/3.
 */
public class AttachmentViewHolder extends BaseViewHolder implements IViewHolderAttach {

    public AttachmentViewHolder(Context context, ViewGroup parent) {
        super(context, parent);
    }

    public AttachmentViewHolder(Context context, ViewGroup parent, int layout) {
        super(context, parent, layout);
    }

    public AttachmentViewHolder(Context context, ViewGroup parent, View itemView) {
        super(context, parent, itemView);
    }

    @Override
    public void onBindViewHolderAttachment(int position) {

    }

    @Override
    public void onClick(int position) {

    }

    @Override
    public boolean onLongClick(int position) {
        return false;
    }

    @Nullable
    @Override
    public View getContainerView() {
        return itemView;
    }
}
