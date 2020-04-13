package com.meili.moon.sdk.app.base.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class AbsBaseAdapter<T> extends android.widget.BaseAdapter {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<T> mDataSet;
	private int[] mLayoutRes;
	//layout数组的空位置的占位
	public final static int LAYOUT_NULL = -1;

	public AbsBaseAdapter(Context mContext, List<T> mDataSet, int... layout) {
		this.mContext = mContext;
		this.mDataSet = mDataSet;
		this.mInflater = LayoutInflater.from(mContext);
		this.mLayoutRes = layout;
	}

	public List<T> getDataSet() {
		return mDataSet;
	}

	public void setDataSet(List<T> mDataSet) {
		this.mDataSet = mDataSet;
	}

	public Context getContext() {
		return mContext;
	}

	@Override
	public int getCount() {
		if (mDataSet == null) {
			return 0;
		}
		return mDataSet.size();
	}

	@Override
	public T getItem(int position) {
		if (mDataSet == null || mDataSet.isEmpty() || mDataSet.size() <= position || position < 0) {
			return null;
		}
		return mDataSet.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		if (getViewTypeCount() > 1) {
			throw new RuntimeException("You must override this method!");
		}
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return mLayoutRes.length;
	}

	public int[] getLayouts() {
		return mLayoutRes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = initView(getItemViewType(position), position);
			if (convertView == null) {
				convertView = mInflater.inflate(mLayoutRes[getItemViewType(position)], parent, false);
			}
		}
		getView(position, getItem(position), convertView);
		return convertView;
	}

	/**
	 * 要使用此方法，构造函数的layoutId需要对应的index设置为0，主要是为了layout的数组占位，并不做实际使用
	 */
	public View initView(int itemType, int position) {
		return null;
	}

	public abstract void getView(int position, T data, View convertView);

}
