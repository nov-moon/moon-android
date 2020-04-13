package com.meili.moon.sdk.app.base.adapter.attachment;

import android.content.Context;

import com.meili.moon.sdk.util.ArrayUtil;

import java.util.ArrayList;

/**
 * Created by imuto on 15/12/3.
 */
public class Attachment {
    public static final int HEADERS_START = Integer.MIN_VALUE;
    public static final int FOOTERS_START = HEADERS_START + 10000;

    private final Context mContext;

    private ArrayList<AttachmentViewHolder> mHeaders = new ArrayList<>();
    private ArrayList<AttachmentViewHolder> mFooters = new ArrayList<>();
    private AttachmentViewHolder mHeaderGroup;
    private AttachmentViewHolder mFooterGroup;

    public Attachment(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }


    public void setHeaderGroup(AttachmentViewHolder vhHeader) {
        if (vhHeader == null) {
            return;
        }
        mHeaderGroup = vhHeader;
    }

    public void addHeader(AttachmentViewHolder vhHeader) {
        if (vhHeader == null) {
            return;
        }
        this.mHeaders.add(vhHeader);
    }

    public void removeHeader(int index) {
        if (ArrayUtil.largerSize(mHeaders, index)) {
            mHeaders.remove(index);
        }
    }

    public void setFooterGroup(AttachmentViewHolder vhFooter) {
        if (vhFooter == null) {
            return;
        }
        mFooterGroup = vhFooter;
    }

    public void addFooter(AttachmentViewHolder vhFooter) {
        if (vhFooter == null) {
            return;
        }
        this.mFooters.add(vhFooter);
    }

    public AttachmentViewHolder getHeaderGroup() {
        return mHeaderGroup;
    }

    public AttachmentViewHolder getHeader(int index) {
        if (index >= getHeaderCount()) {
            return null;
        }
        return this.mHeaders.get(index);
    }


    public AttachmentViewHolder getFooterGroup() {
        return mFooterGroup;
    }

    public AttachmentViewHolder getFooter(int index) {
        if (index >= getFooterCount()) {
            return null;
        }
        return this.mFooters.get(index);
    }

    public int getItemViewType(int position, int itemCount) {
        if (isHeader(position)) {
            return HEADERS_START + position;
        } else {
            int fIndex = position - getHeaderCount() - itemCount;
            return FOOTERS_START + fIndex;
        }
    }

    public AttachmentViewHolder getItemViewHolder(int type) {
        if (type < FOOTERS_START) { // get header
            int fIndex = type - HEADERS_START;
            if (fIndex >= getHeaderCount()) {
                return null;
            }
            return this.mHeaders.get(fIndex);
        } else { // get footer
            int fIndex = type - FOOTERS_START;
            if (fIndex >= getFooterCount()) {
                return null;
            }
            return this.mFooters.get(fIndex);
        }
    }

    public int getHeaderCount() {
        return mHeaders.size();
    }

    public int getFooterCount() {
        return mFooters.size();
    }

    public int getCount() {
        return getHeaderCount() + getFooterCount();
    }

    public boolean isAttachment(int position, int itemCount) {
        return isHeader(position) || isFooter(position, itemCount);
    }

    public boolean isAttachmentByType(int type) {
        return isHeaderByType(type) || isFooterByType(type);
    }

    public boolean isHeader(int position) {
        return position < getHeaderCount();
    }

    public boolean isHeaderByType(int type) {
        return /*type >= HEADERS_START && */type < HEADERS_START + getHeaderCount();
    }

    public boolean isFooter(int position, int itemCount) {
        return getHeaderCount() + itemCount == 0 || position > getHeaderCount() + itemCount - 1;
    }

    public boolean isFooterByType(int type) {
        return type >= FOOTERS_START && type < FOOTERS_START + getFooterCount();
    }

}
