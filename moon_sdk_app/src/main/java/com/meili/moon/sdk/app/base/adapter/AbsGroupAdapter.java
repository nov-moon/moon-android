package com.meili.moon.sdk.app.base.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.app.base.adapter.attachment.Attachment;
import com.meili.moon.sdk.app.base.adapter.attachment.AttachmentGroup;
import com.meili.moon.sdk.app.base.adapter.attachment.AttachmentViewHolder;
import com.meili.moon.sdk.app.base.adapter.dataset.IDataSetGroup;
import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.BaseViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolderAttach;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolderGroup;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolderGroupFooter;
import com.meili.moon.sdk.app.base.adapter.listener.ChangeFundViewHolderListener;
import com.meili.moon.sdk.app.base.adapter.listener.OnItemClickGroupListener;
import com.meili.moon.sdk.app.base.adapter.listener.ViewHolderListener;
import com.meili.moon.sdk.http.common.BaseModel;
import com.tonicartos.superslim.GridSLM;
import com.tonicartos.superslim.LayoutManager;

/**
 * Created by imuto on 15/12/1.
 */
public class AbsGroupAdapter<Group, Child extends BaseModel> extends RecyclerView.Adapter<BaseViewHolder> implements IAdapter {

    private static final int GROUP_START = Integer.MIN_VALUE + 50000;
    private static final int GROUP_FOOTER_START = Integer.MIN_VALUE + 100000;
    private final static int GROUP_DISPLAY = LayoutManager.LayoutParams.HEADER_INLINE | LayoutManager.LayoutParams.HEADER_STICKY;

    private final Context mContext;
    private final IDataSetGroup<Group, Child> mDataSet;
    private final ViewHolderCreatorGroup<Group, Child> mVHCreator;
    private final Attachment mAttachment;

    private ViewHolderListener mViewHolderListener;
    private ChangeFundViewHolderListener mChangeFundViewHolderListener;
    private OnItemClickGroupListener<Group, Child> mOnItemClickListener;

    private static class StaticViewHolder extends AttachmentViewHolder {
        public StaticViewHolder(Context context, ViewGroup group) {
            super(context, group, new View(context));
        }
    }

    private class ItemClickListener implements View.OnClickListener, View.OnLongClickListener {
        private final BaseViewHolder mHolder;

        public ItemClickListener(BaseViewHolder holder) {
            mHolder = holder;
            mHolder.itemView.setOnClickListener(this);
            mHolder.itemView.setOnLongClickListener(this);
        }

        public final ItemClickListener attachClick(AbsViewHolder holder) {
            ItemClickListener listener = new ItemClickListener(holder);
            return listener;
        }

        @Override
        public void onClick(View v) {
            int position = mHolder.getAdapterPosition();
            if (position < RecyclerView.NO_POSITION) {
                return;
            }
            if (mHolder instanceof IViewHolderGroup) {
                int pos = mHolder.getItemPosition();
                int groupIndex = mDataSet.getGroupIndex(pos);
                Group group = mDataSet.getItemGroup(groupIndex);
                if (group != null) {
                    ((IViewHolderGroup) mHolder).onClickGroup(pos, group);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemGroupClick(groupIndex, group, mHolder);
                    }
                }
            } else if (mHolder instanceof IViewHolderGroupFooter) {
                int pos = mHolder.getItemPosition();
                Group group = mDataSet.getItemGroup(mDataSet.getGroupIndex(pos));
                if (group != null) {
                    ((IViewHolderGroupFooter) mHolder).onClickGroupFooter(pos, group);
                }
            } else if (mHolder instanceof IViewHolder) {
                Child item = mDataSet.getItemChild(mHolder.getItemGroupIndex(), mHolder.getItemChildIndex());
                if (item != null) {
                    ((IViewHolder<Child>) mHolder).onClick(mHolder.getItemChildIndex(), item);
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemChildClick(mHolder.getItemChildIndex(), item, mHolder.getItemGroupIndex(), mHolder);
                    }
                }
            } else if (mHolder instanceof IViewHolderAttach) {
                ((IViewHolderAttach) mHolder).onClick(position);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = mHolder.getAdapterPosition();
            if (position < RecyclerView.NO_POSITION) {
                return false;
            }
            if (mHolder instanceof IViewHolderGroup) {
                int pos = mHolder.getItemPosition();
                Group group = mDataSet.getItemGroup(mDataSet.getGroupIndex(pos));
                if (group != null) {
                    ((IViewHolderGroup) mHolder).onLongClickGroup(pos, group);
                }
            } else if (mHolder instanceof IViewHolderGroupFooter) {
                int pos = mHolder.getItemPosition();
                Group group = mDataSet.getItemGroup(mDataSet.getGroupIndex(pos));
                if (group != null) {
                    ((IViewHolderGroupFooter) mHolder).onLongClickGroupFooter(pos, group);
                }
            } else if (mHolder instanceof IViewHolder) {
                Child item = mDataSet.getItemChild(mHolder.getItemGroupIndex(), mHolder.getItemChildIndex());
                if (item != null) {
                    ((IViewHolder<Child>) mHolder).onLongClick(mHolder.getItemChildIndex(), item);
                }
            } else if (mHolder instanceof IViewHolderAttach) {
                ((IViewHolderAttach) mHolder).onLongClick(position);
            }
            return false;
        }
    }

    public AbsGroupAdapter(Context ctx, IDataSetGroup<Group, Child> dataSet, ViewHolderCreatorGroup<Group, Child> vhCreator) {
        this.mContext = ctx;
        this.mDataSet = dataSet;
        this.mVHCreator = vhCreator;
        this.mAttachment = new AttachmentGroup(ctx);
    }

    @Override
    public IDataSetGroup<Group, Child> getDataSet() {
        return this.mDataSet;
    }

    @Override
    public Attachment getAttachment() {
        return mAttachment;
    }

    // --------------- ItemType
    @Override
    public final int getItemViewType(int position) {
        if (mAttachment.isAttachment(position, mDataSet.getCount())) {
            return getItemViewTypeAttachment(position);
        } else {
            return getItemViewTypeDataSet(position);
        }
    }

    protected int getItemViewTypeAttachment(int position) {
        return mAttachment.getItemViewType(position, mDataSet.getCount());
    }

    private int getItemViewTypeDataSet(int position) {
        position = position - mAttachment.getHeaderCount();
        if (mDataSet.isGroup(position)) {
            int gIndex = mDataSet.getGroupIndex(position);
            return getItemViewTypeGroup(position, gIndex);
        } else if (mDataSet.isGroupFooter(position)) {
            int gIndex = mDataSet.getGroupIndex(position);
            return getItemViewTypeGroupFooter(position, gIndex);
        } else {
            IDataSetGroup.Index cIndex = mDataSet.getChildIndex(position);
            return getItemViewTypeChild(position, cIndex.groupIndex, cIndex.childIndex);
        }
    }

    protected int getItemViewTypeGroup(int position, int gIndex) {
        Group group = mDataSet.getItemGroup(gIndex);
        return mVHCreator.getGroupHolder(position, gIndex, group) + GROUP_START;
    }

    protected int getItemViewTypeGroupFooter(int position, int gIndex) {
        if (mVHCreator instanceof IViewHolderCreatorGroupFooter) {
            Group group = mDataSet.getItemGroup(gIndex);
            return ((IViewHolderCreatorGroupFooter<Group, Child>) mVHCreator).getGroupFooterHolder(position, gIndex, group) + GROUP_FOOTER_START;
        } else {
            throw new RuntimeException("你的 vhCreator 没有实现 ViewHolderCreatorGroupFooter 接口");
        }
    }

    protected int getItemViewTypeChild(int position, int gIndex, int cIndex) {
        Child child = mDataSet.getItemChild(gIndex, cIndex);
        return mVHCreator.getChildHolder(position, gIndex, cIndex, child);
    }

    // --------------- /ItemType

    @Override
    public int getItemCount() {
        return mDataSet.getCount() + mAttachment.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder;
        if (mAttachment.isAttachmentByType(viewType)) {
            AttachmentViewHolder vh;
            if (viewType == Attachment.HEADERS_START || viewType == Attachment.FOOTERS_START) {
                if (viewType == Attachment.HEADERS_START) {
                    vh = mAttachment.getHeaderGroup();
                } else {
                    vh = mAttachment.getFooterGroup();
                }
                if (vh == null) {
                    vh = new StaticViewHolder(getContext(), parent);
                }
            } else {
                vh = mAttachment.getItemViewHolder(viewType - 1); // -1 == header
            }
            viewHolder = vh;
        } else {
            AbsViewHolder vh = null;
            // isGroup
            if (viewType >= GROUP_START && viewType < GROUP_START + mDataSet.getGroupCount()) {
                Class<? extends AbsViewHolder> vhClass = mVHCreator.getGroupHolder(viewType - GROUP_START);
                if (checkViewHolder(vhClass, IViewHolderGroup.class)) {
                    vh = createViewHolder(vhClass, parent);
                }
            } else {
                if (viewType >= GROUP_FOOTER_START && viewType < GROUP_FOOTER_START + mDataSet.getGroupCount()) {
                    if (checkViewHolder(mVHCreator.getClass(), IViewHolderCreatorGroupFooter.class)) {
                        Class<? extends AbsViewHolder> vhClass = ((IViewHolderCreatorGroupFooter<Group, Child>) mVHCreator).getGroupFooterHolder(viewType - GROUP_FOOTER_START);
                        if (checkViewHolder(vhClass, IViewHolderGroupFooter.class)) {
                            vh = createViewHolder(vhClass, parent);
                        }
                    }
                } else {
                    Class<? extends AbsViewHolder> vhClass = mVHCreator.getChildHolder(viewType);
                    if (checkViewHolder(vhClass, IViewHolder.class)) {
                        vh = createViewHolder(vhClass, parent);
                    } else {
                        throw new RuntimeException("你的 vh 没有实现 IViewHolder 接口");
                    }
                }
            }
            if (vh == null) {
                throw new RuntimeException("viewholder 创建失败!");
            }
            vh.setIsGroupHolder();
            if (mViewHolderListener != null) {
                vh.setOnViewHolderListener(mViewHolderListener);
                vh.setOnChangeFundViewHolderListener(mChangeFundViewHolderListener);
            }
            viewHolder = vh;
        }
        viewHolder.bindAdapter(this);
        viewHolder.bindDataSet(mDataSet);
        new ItemClickListener(viewHolder);
        return viewHolder;
    }

    private AbsViewHolder createViewHolder(Class<? extends AbsViewHolder> vhClass, ViewGroup parent) {
        try {
            return vhClass.getConstructor(Context.class, ViewGroup.class).newInstance(getContext(), parent);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean checkViewHolder(Class src, Class target) {
        if (!target.isAssignableFrom(src)) {
            throw new RuntimeException(src.getSimpleName() + " 没有实现 " + target.getSimpleName());
        }
        return true;
    }

    // --------------- BindViewHolder
    @Override
    public void onBindViewHolder(BaseViewHolder holder, final int position) {
        int firstPosition;
        holder.bindDataSet(mDataSet);
        if (mAttachment.isAttachment(position, mDataSet.getCount())) {
            onBindViewHolderAttachment((AttachmentViewHolder) holder, position);
            if (mAttachment.isHeader(position)) {
                firstPosition = 0;
            } else /*if (mAttachment.isFooter(position, mDataSet.getCount()))*/ {
                firstPosition = mAttachment.getHeaderCount() + mDataSet.getCount();
            }
        } else {
            final int fixPosition = position - mAttachment.getHeaderCount();
            onBindViewHolderDataSet(holder, fixPosition);
            firstPosition = mDataSet.getGroupPosition(fixPosition) + mAttachment.getHeaderCount();
        }

        final View itemView = holder.itemView;
        final GridSLM.LayoutParams stickyParams = GridSLM.LayoutParams.from(itemView.getLayoutParams());
        stickyParams.headerDisplay = GROUP_DISPLAY;
        stickyParams.isHeader = position == firstPosition;
        stickyParams.setFirstPosition(firstPosition);
        itemView.setLayoutParams(stickyParams);
    }

    protected void onBindViewHolderAttachment(AttachmentViewHolder holder, int position) {
        holder.onBindViewHolderAttachment(position);
        holder.setItemPosition(position);
    }

    private void onBindViewHolderDataSet(BaseViewHolder holder, int position) {
        final boolean isGroup = mDataSet.isGroup(position);
        if (isGroup) {
            onBindViewHolderGroup(holder, position);
        } else if (mDataSet.isGroupFooter(position)) {
            onBindViewHolderGroupFooter(holder, position);
        } else {
            onBindViewHolderChild(holder, position);
        }
    }

    private void onBindViewHolderGroup(BaseViewHolder holder, int position) {
        IViewHolderGroup<Group> vh = (IViewHolderGroup<Group>) holder;
        int gIndex = mDataSet.getGroupIndex(position);
        Group group = mDataSet.getItemGroup(gIndex);
        holder.setItemPosition(position, gIndex, -1);
        vh.onBindViewHolderGroup(group);
    }

    private void onBindViewHolderGroupFooter(BaseViewHolder holder, int position) {
        IViewHolderGroupFooter<Group> vh = (IViewHolderGroupFooter<Group>) holder;
        int gIndex = mDataSet.getGroupIndex(position);
        Group group = mDataSet.getItemGroup(gIndex);
        holder.setItemPosition(position, gIndex, -1);
        vh.onBindViewHolderGroupFooter(group);
    }

    private void onBindViewHolderChild(BaseViewHolder holder, int position) {
        IViewHolder<Child> vh = (IViewHolder<Child>) holder;
        IDataSetGroup.Index cIndex = mDataSet.getChildIndex(position);
        Child child = mDataSet.getItemChild(cIndex.groupIndex, cIndex.childIndex);
        holder.setItemPosition(position, cIndex.groupIndex, cIndex.childIndex);
        vh.onBindViewHolder(child);
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public void setViewHolderListener(ViewHolderListener listener) {
        mViewHolderListener = listener;
    }

    @Override
    public void setChangeFundViewHolderListener(ChangeFundViewHolderListener listener) {
        mChangeFundViewHolderListener = listener;
    }

    public void setOnItemClickListener(OnItemClickGroupListener<Group, Child> listener) {
        this.mOnItemClickListener = listener;
    }
    // --------------- /BindViewHolder


}
