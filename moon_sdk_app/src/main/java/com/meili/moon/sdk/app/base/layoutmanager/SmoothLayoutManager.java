package com.meili.moon.sdk.app.base.layoutmanager;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.RecyclerView;

import com.tonicartos.superslim.LayoutManager;

/**
 * Created by imuto on 16/3/2.
 */
public class SmoothLayoutManager extends LayoutManager implements ISmoothLayoutManager {

    private Context mContext;
    private SmoothLMHelper mSmoothLMHelper;

    public SmoothLayoutManager(Context context) {
        super(context);
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

    @Override
    public PointF computeScrollVectorForPosition(int targetPosition) {
        if (getChildCount() == 0) {
            return null;
        }
        final int firstChildPos = getPosition(getChildAt(0));
        final int direction = targetPosition < firstChildPos ? -1 : 1;
        return new PointF(0, direction);
    }

//    private int getDirectionToPosition(int targetPosition) {
//        SectionData sd = new SectionData(this, getChildAt(0));
//        final View startSectionFirstView = getSlm(sd)
//                .getFirstVisibleView(sd.firstPosition, true);
//        return targetPosition < getPosition(startSectionFirstView) ? -1 : 1;
//    }
}
