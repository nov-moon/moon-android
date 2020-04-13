package com.meili.moon.sdk.app.widget.swipe;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.ui.refresh.MoonRefreshView;
import com.meili.moon.ui.refresh.callback.IRefreshLayout;
import com.tonicartos.superslim.LayoutManager;

/**
 * Created by imuto on 14-10-3.
 */
public class SwipeChildRefreshLayout extends SwipeRefreshLayout implements IRefreshLayout {

    private ListView mChildListView;
    private RecyclerView mRecyclerView;
    private IFirstTouchView mFirstTouchView;

    public SwipeChildRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeChildRefreshLayout(Context context) {
        super(context);
    }

    @Override
    public void setChildListView(ListView listView) {
        mChildListView = listView;
    }

    @Override
    public void setChildRecyclerView(RecyclerView view) {
        mRecyclerView = view;
    }

    @Override
    public void setHeaderView(View headerView) {

    }

    @Override
    public void setFooterView(View footerView) {

    }

    @Override
    public void setEmptyView(View emptyView) {

    }

    @Override
    public boolean canChildScrollUp() {
        if (mChildListView != null || mRecyclerView != null) {
            // In order to scroll a StickyListHeadersListView up:
            // Firstly, the wrapped ListView must have at least one item
            return (getLayoutChildCount() > 0) &&
                    // And then, the first visible item must not be the first item
                    ((getLayoutFirstVisiblePosition() > 0) ||
                            // If the first visible item is the first item,
                            // (we've reached the first item)
                            // make sure that its top must not cross over the padding top of the wrapped ListView
                            (getLayoutFirstTop() < 0));

            // If the wrapped ListView is empty or,
            // the first item is located below the padding top of the wrapped ListView,
            // we can allow performing refreshing now
        } else {
            // Fall back to default implementation
            return super.canChildScrollUp();
        }
    }

    private int getLayoutChildCount() {
        if (mChildListView != null) {
            return mChildListView.getChildCount();
        } else {
            return mRecyclerView.getChildCount();
        }
    }

    private int getLayoutFirstVisiblePosition() {
        if (mChildListView != null) {
            return mChildListView.getFirstVisiblePosition();
        } else {
            RecyclerView.LayoutManager llm = mRecyclerView.getLayoutManager();
            if (llm instanceof LinearLayoutManager) {
                return ((LinearLayoutManager) llm).findFirstVisibleItemPosition();
            } else if (llm instanceof LayoutManager) {
                return ((LayoutManager) llm).findFirstVisibleItemPosition();
            }
            throw new RuntimeException("需要实现自己的方法");
        }
    }

    private int getLayoutFirstTop() {
        if (mChildListView != null) {
            return mChildListView.getChildAt(0).getTop();
        } else {
            return mRecyclerView.getChildAt(0).getTop();
        }
    }

    @Override
    public void setOnRefreshListener(final OnRefreshListener listener) {
        if (listener != null) {
            super.setOnRefreshListener(null);
            super.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (listener != null) {
                        listener.onRefresh();
                    }
                }
            });
        } else {
            super.setOnRefreshListener(null);
        }
    }

    @Override
    public void setOnRefreshListener(MoonRefreshView.OnRefreshListener listener) {

    }

    @Override
    public void setOnLoadMoreListener(MoonRefreshView.OnLoadMoreListener listener) {

    }

    @Override
    public void init() {
        setColorSchemeResources(R.color.moon_sdk_app_swipe_refresh_colors_01, R.color.moon_sdk_app_swipe_refresh_colors_02, R.color.moon_sdk_app_swipe_refresh_colors_03, R.color.moon_sdk_app_swipe_refresh_colors_04);
    }

    @Override
    public void refresh() {

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mFirstTouchView != null && mFirstTouchView.isHandleTouch(event)) {
            return false;
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public void setFirstTouchView(IFirstTouchView touchView) {
        mFirstTouchView = touchView;
    }

}
