package com.meili.moon.sdk.app.base.adapter.dataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by imuto on 15/12/1.
 */
public interface IDataSet<T> extends IBaseDataSet{

    T getItem(int position);

    void setDataSet(List<T> dataSet);

    void addDataSet(List<T> dataSet);

    void addData(T data);

    void addData(int index, T data);

    ArrayList<T> getDataSet();

    boolean removeData(int position);

    boolean removeData(T data);

    boolean contains(T data);

    void itemMove(int fromPosition, int toPosition);
}
