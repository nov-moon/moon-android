package com.meili.moon.sdk.app.base.layoutmanager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Created by imuto on 16/3/2.
 */
public class SmoothLMHelper {

    private Context mContext;
    private OnSmoothListener mOnSmoothListener;
    private RecyclerView.LayoutManager mLayoutManager;
    private ISmoothLayoutManager mISmoothLayoutManager;

    public SmoothLMHelper(Context context, RecyclerView.LayoutManager lm) {
        this.mContext = context;
        this.mLayoutManager = lm;
        this.mISmoothLayoutManager = (ISmoothLayoutManager) lm;
    }

    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
        final int scrollY = mOnSmoothListener.getRecyclerScrollY();
        LinearSmoothScroller smoothScroller = new LinearSmoothScroller(mContext) {
            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return 250f / scrollY;
            }

            @Override
            public PointF computeScrollVectorForPosition(int i) {
                return mISmoothLayoutManager.computeScrollVectorForPosition(i);
            }

        };
        smoothScroller.setTargetPosition(position);
        mLayoutManager.startSmoothScroll(smoothScroller);
    }

    public void setOnSmoothListener(OnSmoothListener listener) {
        this.mOnSmoothListener = listener;
    }

    public boolean isIntercept() {
        return this.mOnSmoothListener != null;
    }
}
