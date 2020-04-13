package com.meili.moon.sdk.app.base.layoutmanager;

import android.graphics.PointF;
import android.support.v7.widget.RecyclerView;

/**
 * Created by imuto on 16/3/2.
 */
public interface ISmoothLayoutManager {

    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position);

    public void setOnSmoothListener(OnSmoothListener listener);

    public PointF computeScrollVectorForPosition(int targetPosition);
}
