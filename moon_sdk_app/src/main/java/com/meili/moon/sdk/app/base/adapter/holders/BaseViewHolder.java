package com.meili.moon.sdk.app.base.adapter.holders;

import android.content.Context;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.app.base.adapter.IAdapter;
import com.meili.moon.sdk.app.base.adapter.dataset.IBaseDataSet;
import com.meili.moon.sdk.app.base.adapter.listener.ChangeFundViewHolderListener;
import com.meili.moon.sdk.app.base.adapter.listener.ItemTouchHelperViewHolder;
import com.meili.moon.sdk.app.base.adapter.listener.ViewHolderListener;
import com.meili.moon.sdk.base.Sdk;

import kotlinx.android.extensions.LayoutContainer;


/**
 * 如果你要使用:
 * 需要实现 {@link IViewHolder} 或者 {@link IViewHolderGroup}
 * 这个类只是个壳
 */
public abstract class BaseViewHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder, LayoutContainer {

    private Context mContext;
    private RecyclerView.Adapter<BaseViewHolder> mAdapter;
    private IBaseDataSet mDataSet;

    private int mItemPosition = -1;
    private int mGroupIndex = -1;
    private int mChildIndex = -1;

    private boolean isGroupHolder = false;

    /**
     * 必须重写此构造器,调用其他构造器来传递 View
     * View 必须传进来,否则会抛出异常
     * @param context
     */
    public BaseViewHolder(Context context, @Nullable ViewGroup parent) {
        super(null);
    }

    protected BaseViewHolder(Context context, @Nullable ViewGroup parent, int layout) {
        this(context, parent, LayoutInflater.from(context).inflate(layout, parent, false));
    }

    protected BaseViewHolder(Context context, @Nullable ViewGroup parent, View itemView) {
        super(itemView);
        this.mContext = context;
        onViewInject(itemView);
        onViewInit(itemView);
    }

    void onViewInject(View itemView) {
        Sdk.view().inject(this, itemView);
    }

    protected void onViewInit(View itemView) {
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * 不要自己用,
     */
    @Deprecated
    public final void setIsGroupHolder() {
        isGroupHolder = true;
    }

    public void setItemPosition(int position) {
        this.mItemPosition = position;
    }

    public void setItemPosition(int position, int groupIndex, int childIndex) {
        setItemPosition(position);
        this.mGroupIndex = groupIndex;
        this.mChildIndex = childIndex;
    }
    /***
     * 获取item所在数据的位置，也就是删除头的位置
     */
    public final int getDataPosition() {
        int adapterPosition = getAdapterPosition();
        if (getAdapter() != null && getAdapter() instanceof IAdapter) {
            adapterPosition -= ((IAdapter) getAdapter()).getAttachment().getHeaderCount();
        }
        return adapterPosition;
    }

    /***
     * 获取当前item的position，已经弃用
     * @see #getDataPosition()
     * @see #getAdapterPosition()
     */
    @Deprecated
    public final int getItemPosition() {
        return mItemPosition;
    }

    public final int getItemGroupIndex() {
        if (!isGroupHolder) {
            throw new RuntimeException("这个只能用在 AbsGroupAdapter 的时候.");
        }
        return mGroupIndex;
    }

    public final int getItemChildIndex() {
        if (!isGroupHolder) {
            throw new RuntimeException("这个只能用在 AbsGroupAdapter 的时候.");
        }
        return mChildIndex;
    }

    protected boolean isGroupHolder() {
        return isGroupHolder;
    }

    /**
     * 需要自己创建自己的接口判断类型来处理..
     *
     * @param listener
     */
    public void setOnViewHolderListener(ViewHolderListener listener) {

    }

    /**重选资方金融产品item点击事件监听*/
    public void setOnChangeFundViewHolderListener(ChangeFundViewHolderListener listener) {

    }

    public final void bindAdapter(RecyclerView.Adapter<BaseViewHolder> adapter) {
        this.mAdapter = adapter;
    }

    public final void bindDataSet(IBaseDataSet dataSet) {
        this.mDataSet = dataSet;
    }

    protected RecyclerView.Adapter<BaseViewHolder> getAdapter() {
        return mAdapter;
    }

    protected IBaseDataSet getDataSet() {
        return mDataSet;
    }

    @Override
    public void onItemSelected() {
        Vibrator vibrator = (Vibrator)getContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(5);
    }

    @Override
    public void onItemClear() {
    }

}
