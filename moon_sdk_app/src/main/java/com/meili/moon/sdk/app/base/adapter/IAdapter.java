package com.meili.moon.sdk.app.base.adapter;


import com.meili.moon.sdk.app.base.adapter.attachment.Attachment;
import com.meili.moon.sdk.app.base.adapter.dataset.IBaseDataSet;
import com.meili.moon.sdk.app.base.adapter.listener.ChangeFundViewHolderListener;
import com.meili.moon.sdk.app.base.adapter.listener.ViewHolderListener;

/**
 * Created by imuto on 15/12/14.
 */
public interface IAdapter {

    IBaseDataSet getDataSet();

    Attachment getAttachment();

    void setViewHolderListener(ViewHolderListener listener);

    void setChangeFundViewHolderListener(ChangeFundViewHolderListener listener);

    void notifyDataSetChanged();

    void notifyItemChanged(int position);
}
