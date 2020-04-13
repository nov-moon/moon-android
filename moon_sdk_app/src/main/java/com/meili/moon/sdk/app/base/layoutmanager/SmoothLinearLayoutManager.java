package com.meili.moon.sdk.app.base.layoutmanager;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * 用于修改 recyclerview smooth 滑动执行时间问题
 * <p/>
 * Created by imuto on 15/10/21.
 */
public class SmoothLinearLayoutManager extends LinearLayoutManager implements ISmoothLayoutManager {

    private Context mContext;
    private SmoothLMHelper mSmoothLMHelper;

    public SmoothLinearLayoutManager(Context context) {
        super(context);
        mContext = context;
        mSmoothLMHelper = new SmoothLMHelper(context, this);
    }

    public SmoothLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mContext = context;
        mSmoothLMHelper = new SmoothLMHelper(context, this);
    }

    public SmoothLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mSmoothLMHelper = new SmoothLMHelper(context, this);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        // A good idea would be to create this instance in some initialization method, and just set the target position in this method.
        if (mSmoothLMHelper.isIntercept()) {
            mSmoothLMHelper.smoothScrollToPosition(recyclerView, state, position);
        } else {
            super.smoothScrollToPosition(recyclerView, state, position);
        }
    }

    @Override
    public void setOnSmoothListener(OnSmoothListener listener) {
        mSmoothLMHelper.setOnSmoothListener(listener);
    }
}
