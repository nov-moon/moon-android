package com.meili.moon.sdk.app.base.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.app.base.adapter.attachment.Attachment;
import com.meili.moon.sdk.app.base.adapter.attachment.AttachmentViewHolder;
import com.meili.moon.sdk.app.base.adapter.dataset.IDataSet;
import com.meili.moon.sdk.app.base.adapter.holders.AbsViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.BaseViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolder;
import com.meili.moon.sdk.app.base.adapter.holders.IViewHolderAttach;
import com.meili.moon.sdk.app.base.adapter.listener.ChangeFundViewHolderListener;
import com.meili.moon.sdk.app.base.adapter.listener.ItemTouchHelperAdapter;
import com.meili.moon.sdk.app.base.adapter.listener.OnItemClickListener;
import com.meili.moon.sdk.app.base.adapter.listener.ViewHolderListener;
import com.meili.moon.sdk.base.util.AndroidUtilsKt;
import com.meili.moon.ui.refresh.callback.IRefreshLayout;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;


/**
 * Created by imuto on 15/12/1.
 */
public class AbsAdapter<Item> extends RecyclerView.Adapter<BaseViewHolder> implements IAdapter, ItemTouchHelperAdapter, IRefreshLayout.IFirstTouchView {

    private final Context mContext;
    private final IDataSet<Item> mDataSet;
    private final ViewHolderCreator<Item> mVHCreator;
    private final Attachment mAttachment;

    private ViewHolderListener mViewHolderListener;
    private ChangeFundViewHolderListener mChangeFundViewHolderListener;
    private OnItemClickListener<Item> mOnItemClickListener;

    private boolean isHandleTouch;

    public AbsAdapter(Context ctx, IDataSet<Item> dataSet, ViewHolderCreator<Item> vhCreator) {
        if (dataSet == null) {
            throw new RuntimeException("友情提示:你的 dataSet 是空的");
        }
        if (vhCreator == null) {
            throw new RuntimeException("友情提示:你的 vhCreator 是空的");
        }
        this.mContext = ctx;
        this.mDataSet = dataSet;
        this.mVHCreator = vhCreator;
        this.mAttachment = new Attachment(ctx);
    }

    public AbsAdapter(Context ctx, IDataSet<Item> dataSet, final Class<? extends AbsViewHolder> vhClass) {
        this(ctx, dataSet, new ViewHolderCreator<Item>() {
            @Override
            public int getItemViewType(int position, Item data) {
                return 0;
            }

            @Override
            public Class<? extends AbsViewHolder> getItemViewHolder(int viewType) {
                return vhClass;
            }
        });
    }

    public final Context getContext() {
        return mContext;
    }

    @Override
    public IDataSet<Item> getDataSet() {
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

    protected int getItemViewTypeDataSet(int position) {
        position = position - mAttachment.getHeaderCount();
        Item data = mDataSet.getItem(position);
        return mVHCreator.getItemViewType(position, data);
    }
    // --------------- /ItemType

    @Override
    public final int getItemCount() {
        return mDataSet.getCount() + mAttachment.getCount();
    }

    @Override
    public final BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseViewHolder viewHolder;
        if (mAttachment.isAttachmentByType(viewType)) {
            AttachmentViewHolder vh = mAttachment.getItemViewHolder(viewType);
            viewHolder = vh;
        } else {
            Class<? extends AbsViewHolder> vhClass = mVHCreator.getItemViewHolder(viewType);
            if (IViewHolder.class.isAssignableFrom(vhClass)) {
                try {
                    AbsViewHolder vh = vhClass.getConstructor(Context.class, ViewGroup.class).newInstance(getContext(), parent);
                    vh.setOnViewHolderListener(mViewHolderListener);
                    vh.setOnChangeFundViewHolderListener(mChangeFundViewHolderListener);
                    viewHolder = vh;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new RuntimeException("友情提示:你的 vh 没有实现 IViewHolder 接口");
            }
        }
        viewHolder.bindAdapter(this);
        viewHolder.bindDataSet(mDataSet);
        new ItemClickListener(viewHolder);
        return viewHolder;
    }

    // --------------- BindViewHolder
    @Override
    public final void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bindDataSet(mDataSet);
        if (mAttachment.isAttachment(position, mDataSet.getCount())) {
            onBindViewHolderAttachment((AttachmentViewHolder) holder, position);
        } else if (!mDataSet.isEmpty()) {
            onBindViewHolderDataSet(holder, position - mAttachment.getHeaderCount());
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected void onBindViewHolderAttachment(AttachmentViewHolder holder, int position) {
        holder.onBindViewHolderAttachment(position);
    }

    protected void onBindViewHolderDataSet(BaseViewHolder holder, int position) {
        holder.setItemPosition(position);
        IViewHolder<Item> vh = (IViewHolder<Item>) holder;
        Item data = mDataSet.getItem(position);
        vh.onBindViewHolder(data);
    }
    // --------------- /BindViewHolder

    @Override
    public void setViewHolderListener(ViewHolderListener listener) {
        this.mViewHolderListener = listener;
    }

    @Override
    public void setChangeFundViewHolderListener(ChangeFundViewHolderListener mChangeFundViewHolderListener) {
        this.mChangeFundViewHolderListener = mChangeFundViewHolderListener;
    }

    public void setOnItemClickListener(OnItemClickListener<Item> listener) {
        this.mOnItemClickListener = listener;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        mDataSet.itemMove(getDataPosition(fromPosition), getDataPosition(toPosition));
        notifyItemMoved(fromPosition, toPosition);
        return false;
    }

    @Override
    public void onItemDismiss(int position) {
        mDataSet.removeData(getDataPosition(position));
        notifyItemRemoved(position);
    }

    private int getRealPosition(int dataPosition) {
        return dataPosition + getAttachment().getHeaderCount();
    }

    /** 根据view的position获取view相应的数据对象在DataSet中的position */
    public int getDataPosition(int positionReal) {
        return positionReal - getAttachment().getHeaderCount();
    }

    @Override
    public boolean isHandleTouch(MotionEvent me) {
        return isHandleTouch;
    }

    private class ItemClickListener implements  View.OnLongClickListener, View.OnTouchListener ,Function1<View, Unit>{
        private final BaseViewHolder mHolder;

        public ItemClickListener(BaseViewHolder holder) {
            mHolder = holder;
            AndroidUtilsKt.onClick(mHolder.itemView,null, this);
            mHolder.itemView.setOnLongClickListener(this);
        }


        @Override
        public Unit invoke(View view) {
            int position = mHolder.getAdapterPosition();
            if (position < RecyclerView.NO_POSITION) {
                return null;
            }
            if (mHolder instanceof IViewHolder) {
                int pos = mHolder.getItemPosition();
                Item item = mDataSet.getItem(pos);
                if (item != null) {
                    boolean handler = false;
                    if (mOnItemClickListener != null) {
                        handler = mOnItemClickListener.onItemClick(pos, item);
                    }
                    if (!handler) {
                        ((IViewHolder<Item>) mHolder).onClick(pos, item);
                    }
                }
            } else if (mHolder instanceof IViewHolderAttach) {
                ((IViewHolderAttach) mHolder).onClick(position);
            }
            return null;
        }

        @Override
        public boolean onLongClick(View v) {
            isHandleTouch = true;
            int position = mHolder.getAdapterPosition();
            if (position < RecyclerView.NO_POSITION) {
                return false;
            }
            if (mHolder instanceof IViewHolder) {
                int pos = mHolder.getItemPosition();
                Item item = mDataSet.getItem(pos);
                if (item != null)
                    return ((IViewHolder<Item>) mHolder).onLongClick(pos, item);
            } else if (mHolder instanceof IViewHolderAttach) {
                return ((IViewHolderAttach) mHolder).onLongClick(position);
            }
            return false;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_UP
                    || event.getAction() == MotionEvent.ACTION_CANCEL) {
                isHandleTouch = false;
            }
            return false;
        }

    }

}
