package com.meili.moon.sdk.app.base.adapter.attachment;

import android.content.Context;
import android.view.View;

/**
 * Created by imuto on 15/12/3.
 */
public class AttachmentGroup extends Attachment {

    private AttachmentViewHolder mHeaderGroup;
    private AttachmentViewHolder mFooterGroup;

    private class DefaultGroup extends AttachmentViewHolder {
        public DefaultGroup(Context context) {
            super(context, null, new View(context));
        }

        @Override
        public void onBindViewHolderAttachment(int position) {
        }
    }

    public AttachmentGroup(Context context) {
        super(context);
    }

    public void setHeaderGroup(AttachmentViewHolder headerGroup) {
        this.mHeaderGroup = headerGroup;
    }

    public void setFooterGroup(AttachmentViewHolder footerGroup) {
        this.mFooterGroup = footerGroup;
    }

    public AttachmentViewHolder getHeaderGroup() {
        if (mHeaderGroup == null) {
            mHeaderGroup = new DefaultGroup(getContext());
        }
        return mHeaderGroup;
    }

    public AttachmentViewHolder getFooterGroup() {
        if (mFooterGroup == null) {
            mFooterGroup = new DefaultGroup(getContext());
        }
        return mFooterGroup;
    }

    @Override
    public int getHeaderCount() {
        int count = super.getHeaderCount();
        return count == 0 ? count : count + 1;
    }

    @Override
    public int getFooterCount() {
        int count = super.getFooterCount();
        return count == 0 ? count : count + 1;
    }


}
