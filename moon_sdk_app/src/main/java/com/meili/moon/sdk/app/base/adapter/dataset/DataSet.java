package com.meili.moon.sdk.app.base.adapter.dataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imuto on 15/12/1.
 */
public final class DataSet<T> implements IDataSet<T> {

    private List<T> mDataSet = new ArrayList<>();

    @Override
    public T getItem(int position) {
        if (position >= getCount()) {
            return null;
        }
        return mDataSet.get(position);
    }

    @Override
    public boolean isEmpty() {
        return mDataSet.isEmpty();
    }

    @Override
    public int getCount() {
        return mDataSet.size();
    }

    @Override
    public void setDataSet(List<T> dataSet) {
        if (mDataSet.equals(dataSet)) {
            return;
        }
        clear();
        addDataSet(dataSet);
    }

    public void set(int index, T data) {
        mDataSet.set(index, data);
    }

    @Override
    public void addDataSet(List<T> dataSet) {
        if (dataSet == null) {
            return;
        }
        mDataSet.addAll(dataSet);
    }

    @Override
    public void addData(T data) {
        mDataSet.add(data);
    }

    public void addData(int index, T data) {
        mDataSet.add(index, data);
    }


    @Override
    public ArrayList<T> getDataSet() {
        return new ArrayList<>(mDataSet);
    }

    @Override
    public boolean removeData(int position) {
        if (position >= mDataSet.size()) {
            return false;
        }
        mDataSet.remove(position);
        return true;
    }

    @Override
    public boolean removeData(T data) {
        return mDataSet.remove(data);
    }

    @Override
    public boolean contains(T data) {
        if(data == null){
            return false;
        }
        return mDataSet.contains(data);
    }

    @Override
    public void itemMove(int fromPosition, int toPosition) {
        T remove = mDataSet.remove(fromPosition);
        mDataSet.add(toPosition, remove);
    }

    @Override
    public void clear() {
        mDataSet.clear();
    }
}
