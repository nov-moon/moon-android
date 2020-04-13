package com.meili.moon.sdk.app.widget.pagetools;

import android.content.Context;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.RelativeLayout;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.util.ViewUtilsKt;
import com.meili.moon.sdk.app.widget.pagetools.views.PageToolEmptyView;
import com.meili.moon.sdk.app.widget.pagetools.views.PageToolErrorByServerView;
import com.meili.moon.sdk.app.widget.pagetools.views.PageToolErrorView;
import com.meili.moon.sdk.app.widget.pagetools.views.PageToolLoadingView;
import com.meili.moon.sdk.app.widget.pagetools.views.PageToolView;
import com.meili.moon.sdk.log.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 页面工具集Layout
 * <p>
 * Created by imuto on 15/12/14.
 */
public class PageToolsLayout extends RelativeLayout {

    /** 页面状态flag：隐藏 */
    public static final int FLAG_NONE = -1;
    /** 页面状态flag：loading */
    public static final int FLAG_LOADING = 0;
    /** 页面状态flag：empty */
    public static final int FLAG_EMPTY = 1;
    /** 页面状态flag：错误 */
    public static final int FLAG_ERROR = 2;
    /** 页面状态flag：错误，从服务端返回的错误 */
    public static final int FLAG_ERROR_FROM_SERVER = 3;

    /** 页面状态对应的view，index为对应的flag */
    private SparseArray<ToolViewState> mToolsViews = new SparseArray<>();

    /** 当前flag */
    private int mFlag = FLAG_NONE;
    /**
     * 页面状态监听器
     */
    private List<FlagListener> mFlagListeners = new ArrayList<>();

    private String emptyText = "目前啥也没有哦";

    private boolean mIsSetup;
    private OnClickListener mOnClickListener;

    private int mLoadingCount = 0;

    public PageToolsLayout(Context context) {
        this(context, null);
    }

    public PageToolsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                onFlagClick();
            }
        };
    }

    protected void initViews() {
        if (mIsSetup) {
            return;
        }
        mIsSetup = true;
        if (!isContainFlag(FLAG_LOADING)) {
            PageToolsParams loadingParams = new PageToolsParams();
            loadingParams.setBackground(R.color.page_background);
            loadingParams.setClickable(true);
            addPageToolView(FLAG_LOADING, new PageToolLoadingView(), loadingParams);
        }

        if (!isContainFlag(FLAG_EMPTY)) {
            PageToolsParams emptyParams = new PageToolsParams();
            emptyParams.setContent(emptyText);
            emptyParams.setClickable(false);
            emptyParams.setBackground(R.color.page_background);
            addPageToolView(FLAG_EMPTY, new PageToolEmptyView(), emptyParams);
        }

        if (!isContainFlag(FLAG_ERROR)) {
            PageToolsParams errorParams = new PageToolsParams();
            errorParams.setImage(R.drawable.moon_sdk_app_page_tool_reload);
            errorParams.setBackground(R.color.page_background);
            errorParams.setClickable(true);
            errorParams.setContent("开小差了，请稍后重试");
            addPageToolView(FLAG_ERROR, new PageToolErrorView(), errorParams);
        }

        if (!isContainFlag(FLAG_ERROR_FROM_SERVER)) {
            PageToolsParams emptyParams = new PageToolsParams();
            emptyParams.setContent(emptyText);
            emptyParams.setClickable(false);
            emptyParams.setBackground(R.color.page_background);
            addPageToolView(FLAG_ERROR_FROM_SERVER, new PageToolErrorByServerView(), emptyParams);
        }
    }

    public int getCurrentFlag() {
        return mFlag;
    }

    public boolean isContainFlag(int flag) {
        return mToolsViews.indexOfKey(flag) >= 0;
    }

    public void addPageToolView(int flag, PageToolView toolsView, PageToolsParams params) {
        mToolsViews.put(flag, new ToolViewState(toolsView));
        setParams(flag, params);
    }

    public void removePageToolView(int flag) {
        ToolViewState toolViewState = mToolsViews.get(flag);
        if (toolViewState != null) {
            toolViewState.remove();
        }
        mToolsViews.remove(flag);
    }

    public PageToolsParams getParams(int flag) {
        if (!isLegalFlag(flag)) {
            return null;
        }
        ToolViewState toolsState = mToolsViews.get(flag);
        return toolsState.getParams();
    }

    public void setParams(int flag, PageToolsParams params) {
        if (!isLegalFlag(flag)) {
            return;
        }
        ToolViewState toolsState = mToolsViews.get(flag);
        toolsState.setParams(params);
    }

    public void showEmpty() {
        show(FLAG_EMPTY);
    }

    public void showEmpty(String emptyText) {
        PageToolsParams params = getParams(FLAG_EMPTY);
        params.content = emptyText;
        show(FLAG_EMPTY, params);
    }

    public void showLoading() {
        show(FLAG_LOADING);
    }

    public void showError() {
        show(FLAG_ERROR);
    }

    public void showServerError(String errorMsg) {
        PageToolsParams params = getParams(FLAG_ERROR_FROM_SERVER);
        params.content = errorMsg;
        show(FLAG_ERROR_FROM_SERVER);
    }

    public void show(int flag) {
        this.bringToFront();
        show(flag, getParams(flag));
    }


    public void show(int flag, PageToolsParams params) {
        if (!isLegalFlag(flag)) {
            return;
        }
        if (getVisibility() != VISIBLE) {
            setVisibility(VISIBLE);
        }

        if (flag == FLAG_LOADING) {
            mLoadingCount++;
        } else {
            mLoadingCount = 0;
        }

        if (!goneCurrent(true)) {
            return;
        }

        // next
        ToolViewState toolsState = mToolsViews.get(flag);
        toolsState.visible();
        setParams(flag, params);
        if (mFlag == flag) {
            return;
        }
        mFlag = flag;
        onFlagChanged(mFlag);
    }

    public void gone() {
        if (!goneCurrent(false)) {
            return;
        }
        if (this.getVisibility() == View.VISIBLE) {
            ViewUtilsKt.fadeOut(this, 300);
        }
    }

    public void gone(int flag) {
        if (mFlag != flag) {
            return;
        }
        gone();
    }

    private boolean goneCurrent(boolean isSwitchType) {
        if (mFlag == FLAG_LOADING) {
            mLoadingCount--;
            if (mLoadingCount > 1) {
                return false;
            }
        }
        ToolViewState lastToolsState = mToolsViews.get(mFlag);
        if (lastToolsState != null) {
            onGonCurr(lastToolsState, isSwitchType);
        }
        mFlag = FLAG_NONE;
        return true;
    }

    protected void onGonCurr(final ToolViewState state, boolean isSwitchType) {
        if (!isSwitchType) {
            state.gone();
        }
//        CommonSdk.task().post(new Runnable() {
//            @Override
//            public void run() {
//                state.gone();
//            }
//        }, 5000);

    }

    public boolean isLegalFlag(int flag) {
        final int index = mToolsViews.indexOfKey(flag);
        if (index < 0) {
            LogUtil.e("FLAG错误 ? flag = " + flag);
            return false;
        }
        return true;
    }

    protected void onFlagChanged(int flag) {
        if (this.mFlagListeners.isEmpty()) {
            return;
        }
        for (int i = mFlagListeners.size() - 1; i >= 0; i--) {
            boolean handler = mFlagListeners.get(i).onChanged(flag);
            if (handler) {
                break;
            }
        }
    }

    public void onFlagClick() {
        onFlagClick(getCurrentFlag());
    }

    protected void onFlagClick(int flag) {
        if (this.mFlagListeners.isEmpty()) {
            return;
        }
        for (int i = mFlagListeners.size() - 1; i >= 0; i--) {
            boolean handler = mFlagListeners.get(i).onClick(flag);
            if (handler) {
                break;
            }
        }
    }

    public void addOnFlagListener(FlagListener listener) {
        if (listener == null) {
            return;
        }
        this.mFlagListeners.add(listener);
    }

    public void addOnFlagListener(OnStateListener listener) {
        if (listener == null) {
            return;
        }
        addOnFlagListener((FlagListener) listener);
    }

    public void removeOnFlagListener(FlagListener listener) {
        this.mFlagListeners.remove(listener);
    }

    public static class OnStateListener implements FlagListener {
        @Override
        public boolean onChanged(int flag) {
            return false;
        }

        @Override
        public boolean onClick(int flag) {
            switch (flag) {
                case FLAG_EMPTY:
                    onClickEmpty();
                    break;
                case FLAG_ERROR:
                    onClickError();
                    break;
            }
            return false;
        }

        public boolean onClickError() {
            return false;
        }

        public boolean onClickEmpty() {
            return false;
        }

    }

    public interface FlagListener {
        boolean onChanged(int flag);

        boolean onClick(int flag);
    }

    protected class ToolViewState {

        PageToolView mToolView;
        View mContentView;
        PageToolsParams mParams;

        PageToolsParams.DataSetObserver mObserver = new PageToolsParams.DataSetObserver() {
            @Override
            public void onChanged() {
                if (mContentView == null || mParams == null) {
                    return;
                }
                mContentView.setOnClickListener(mParams.isClickable() ? mOnClickListener : null);
                mContentView.setClickable(mParams.isClickable());
                mToolView.onDataChanged(mParams);
            }
        };

        private ToolViewState(PageToolView toolView) {
            this.mToolView = toolView;
        }

        private void setParams(PageToolsParams params) {
            if (this.mParams != null) {
                this.mParams.unregisterDataSetObserver(mObserver);
            }
            this.mParams = params;
            if (this.mParams == null) {
                return;
            }
            this.mParams.registerDataSetObserver(mObserver);
            notifyParams();
        }

        private PageToolsParams getParams() {
            return this.mParams;
        }

        private PageToolView getToolView() {
            return this.mToolView;
        }

        private View getContentView() {
            if (this.mContentView == null) {
                this.mContentView = this.mToolView.onCreateView(getContext(), PageToolsLayout.this);
                PageToolsLayout.this.addView(this.mContentView);
                this.mContentView.setOnClickListener(mParams.isClickable() ? mOnClickListener : null);
            }
            return this.mContentView;
        }

        private void visible() {
            getContentView().setVisibility(View.VISIBLE);
            mToolView.onVisibilityChanged(true);
            notifyParams();
        }

        public void gone() {
            mToolView.onVisibilityChanged(false);
            getContentView().setVisibility(View.GONE);
        }

        private void remove() {
            if (this.mContentView != null) {
                PageToolsLayout.this.removeView(mContentView);
            }
            mContentView = null;
            setParams(null);
        }

        private void notifyParams() {
            if (this.mContentView != null && this.mContentView.getVisibility() == View.VISIBLE) {
                if (this.mParams != null)
                    this.mParams.notifyDataSetChanged();
            }
        }

    }
}
