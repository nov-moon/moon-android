package com.meili.moon.sdk.app.base.page;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.RelativeLayout;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.base.adapter.AbsAdapter;
import com.meili.moon.sdk.app.base.adapter.AbsGroupAdapter;
import com.meili.moon.sdk.app.base.adapter.IAdapter;
import com.meili.moon.sdk.app.base.adapter.attachment.Attachment;
import com.meili.moon.sdk.app.base.adapter.attachment.AttachmentViewHolder;
import com.meili.moon.sdk.app.base.adapter.listener.ItemTouchHelperAdapter;
import com.meili.moon.sdk.app.base.adapter.listener.SimpleItemTouchHelperCallback;
import com.meili.moon.sdk.app.base.layoutmanager.ISmoothLayoutManager;
import com.meili.moon.sdk.app.base.layoutmanager.OnSmoothListener;
import com.meili.moon.sdk.app.base.layoutmanager.SmoothLinearLayoutManager;
import com.meili.moon.sdk.app.callback.SimpleTypedCallback;
import com.meili.moon.sdk.app.widget.pagetools.FooterToolsLayout;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsLayout;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;
import com.meili.moon.sdk.app.widget.swipe.SwipeChildRefreshLayout;
import com.meili.moon.sdk.base.Sdk;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.common.Callback;
import com.meili.moon.sdk.http.HttpMethod;
import com.meili.moon.sdk.http.IRequestParams;
import com.meili.moon.ui.refresh.MoonRefreshView;
import com.meili.moon.ui.refresh.callback.IRefreshLayout;
import com.tonicartos.superslim.LayoutManager;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * 带分页的fragment
 * <p>
 * Created by imuto on 15/12/28.
 */
@Deprecated
public abstract class PagingOlderFragment<DataType> extends PageFragment implements MoonRefreshView.OnRefreshListener {

    public static final int PAGING_COUNT = 10;

    protected HttpMethod mHttpMethod = HttpMethod.POST;

    IRefreshLayout mRefreshLayout;
    protected RecyclerView mRecycler;

    private PageState mPageState = new PageState();
    private int mScrollY;

    private AttachmentViewHolder mFooterAttachment;
    private RecyclerView.LayoutManager mLayoutManager;
    private FooterToolsLayout mFooterToolsLayout;

    private boolean isGroupList = false;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            PagingOlderFragment.this.onScrolled(recyclerView, dx, dy);
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            PagingOlderFragment.this.onScrollStateChanged(recyclerView, newState);
        }
    };
    private RelativeLayout mContainer;

    protected int getLayoutResId() {
        return R.layout.moon_sdk_app_common_paging;
    }

    protected IRefreshLayout getRefreshLayout() {
        return mRefreshLayout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = (IRefreshLayout) view.findViewById(R.id.mRefreshLayout);
        mRecycler = (RecyclerView) view.findViewById(R.id.mRecycler);
        setMPageTool((PageToolsLayout) view.findViewById(R.id.mPageToolsLayout));

        mRefreshLayout.init();
        mRefreshLayout.setChildRecyclerView(mRecycler);

        IAdapter adapter = getListAdapter();
        if (adapter instanceof SwipeChildRefreshLayout.IFirstTouchView
                && mRefreshLayout instanceof SwipeChildRefreshLayout) {
            ((SwipeChildRefreshLayout) mRefreshLayout).setFirstTouchView(
                    (SwipeChildRefreshLayout.IFirstTouchView) adapter);
        }

        if (isItemCanMove()) {
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback((ItemTouchHelperAdapter) getListAdapter());
            ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
            mItemTouchHelper.attachToRecyclerView(getRecyclerView());
        }

        isGroupList = adapter instanceof AbsGroupAdapter;

        mRecycler.setHasFixedSize(true);
        mRecycler.addOnScrollListener(mScrollListener);
        RecyclerView.LayoutManager lm = getLayoutManager();
        if (lm instanceof ISmoothLayoutManager) {
            ((ISmoothLayoutManager) lm).setOnSmoothListener(new OnSmoothListener() {
                @Override
                public int getRecyclerScrollY() {
                    return mScrollY;
                }
            });
        }
        mRecycler.setLayoutManager(lm);


        Attachment attachment = adapter.getAttachment();
        onAddHeaderView(attachment);
        onAddFooterView(attachment);

        mRecycler.setAdapter((RecyclerView.Adapter) adapter);

        mRefreshLayout.setOnRefreshListener(this);

        getMPageTool().addOnFlagListener(new PageToolsLayout.FlagListener() {
            @Override
            public boolean onChanged(int flag) {
                return false;
            }

            @Override
            public boolean onClick(int flag) {
                if (flag == PageToolsLayout.FLAG_ERROR) {
                    onRefresh(false);
                    return true;
                }
                return false;
            }
        });

        mRecycler.setVisibility(View.INVISIBLE);
    }

    protected void setSwipeRefreshEnable(boolean enable) {
        mRefreshLayout.setEnabled(enable);
    }

    public abstract AbsAdapter getListAdapter();

    public void onAddHeaderView(Attachment attach) {
    }

    /**
     * 添加footer,如果不调用父类的方法,则需要同时重写isListEmpty()方法,
     */
    public void onAddFooterView(Attachment attach) {
        attach.addFooter(getDefaultFooterView());
    }

    public FooterToolsLayout getFooterToolsLayout() {
        if (mFooterToolsLayout == null) {
            mFooterToolsLayout = new FooterToolsLayout(getPageActivity());
        }
        return mFooterToolsLayout;
    }

    protected AttachmentViewHolder getDefaultFooterView() {
        if (mFooterAttachment == null) {
            mFooterAttachment = new AttachmentViewHolder(getContext(), getRecyclerView(), getFooterToolsLayout()) {
                @Override
                public void onBindViewHolderAttachment(int position) {
//                    if (getListAdapter().getDataSet().isEmpty()) {
//                        return;
//                    }
                    if (getMPageTool().getCurrentFlag() != PageToolsLayout.FLAG_NONE) { // 大 tools 在工作
                        return;
                    }

                    if (mPageState.isDead()) {
                        getFooterToolsLayout().show(FooterToolsLayout.FLAG_EMPTY);
                    } else if (mPageState.isStart()) {
                        getFooterToolsLayout().show(FooterToolsLayout.FLAG_LOADING);
                    } else if (mPageState.isError()) {
                        getFooterToolsLayout().show(FooterToolsLayout.FLAG_ERROR);
                    } else if (mPageState.getCurrentPage() == 1 || mPageState.isIdle()) {
                        getFooterToolsLayout().show(FooterToolsLayout.FLAG_NONE);
                    }
                }

                @Override
                public void onClick(int position) {
                    super.onClick(position);
                    getFooterToolsLayout().onFlagClick();
                }
            };

            getFooterToolsLayout().addOnFlagListener(new PageToolsLayout.FlagListener() {
                @Override
                public boolean onChanged(int flag) {
                    return false;
                }

                @Override
                public boolean onClick(int flag) {
                    if (flag == FooterToolsLayout.FLAG_ERROR && mPageState.isError()) {
                        request(mPageState.getCurrentPage());
                    }
                    return false;
                }
            });

        }
        return mFooterAttachment;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        if (mLayoutManager == null || mRecycler.getLayoutManager() != mLayoutManager) {
//            if (isGroupList) {
//                mLayoutManager = new LayoutManager(getAttachedActivity());
//            } else {
            mLayoutManager = new SmoothLinearLayoutManager(getPageActivity(), LinearLayoutManager.VERTICAL, false);
//            }
        }
        return mLayoutManager;
    }

    /**
     * set http method , default GET
     *
     * @param method
     */
    protected void setHttpMethod(HttpMethod method) {
        mHttpMethod = method;
    }

    protected void clearList() {
        getListAdapter().getDataSet().clear();
    }

    public RecyclerView getRecyclerView() {
        return mRecycler;
    }

    /**
     * @param isPullDown 是不是由下拉手势引起的刷新，是则返回true
     */
    @Override
    public void onRefresh(boolean isPullDown) {
        if (mPageState.isIdle() || mPageState.isDead() || mPageState.isError()) {
            mPageState.relive();
            request(0);
            if (isUseRefreshLayoutLoading() && !getListAdapter().getDataSet().isEmpty()) {
                mRefreshLayout.setRefreshing(true);
            }
        }
    }

    public boolean isUseRefreshLayoutLoading() {
        return true;
    }

    public boolean isUsePageToolsLoading() {
        return isListEmpty();
    }

    public abstract IRequestParams.IHttpRequestParams getRequestParams(int page, int count);

    protected abstract Type getResType();

    private void request(int page) {

        mRecycler.setEnabled(false);
        page = page + 1;
        mPageState.start();
        if (page == 1) {
            if (isUsePageToolsLoading()) {
                getMPageTool().show(PageToolsLayout.FLAG_LOADING);
            }
        } else {
            getMPageTool().gone();
        }

        request(page, getRequestParams(page, getPageSize()));
    }

    private void request(final int page, IRequestParams.IHttpRequestParams params) {
        final Type type = getResType();
        Callback.IHttpCallback<DataType> callback = getDataRequestCallback(page, type);

        if (mHttpMethod == HttpMethod.GET) {
            Sdk.http().get(params, callback);
        } else if (mHttpMethod == HttpMethod.POST) {
            Sdk.http().post(params, callback);
        } else {
            throw new RuntimeException("unknow http method");
        }
    }

    /**
     * 获取请求数据的Callback
     *
     * @param page 当前请求页数
     * @param type 解析到的泛型，可以不使用
     */
    protected Callback.IHttpCallback<DataType> getDataRequestCallback(final int page, final Type type) {
        return new SimpleTypedCallback<DataType>(type) {
            @Override
            public void onStarted() {
                if (isFinishing()) {
                    return;
                }
                onRequestStart(page);
            }

            @Override
            public void onSuccess(final DataType result) {
                postOnPageAnimationEnd(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        if (isFinishing()) {
                            return Unit.INSTANCE;
                        }
                        try {
                            onRequestSuccess(page, result);
                        } catch (Exception e) {
                            e.printStackTrace();
                            onError(new BaseException(0, "数据加载失败", e));
                            onFinished(false);
                        }
                        return Unit.INSTANCE;
                    }
                });
            }

            @Override
            public void onError(@NotNull BaseException exception) {
                if (isFinishing()) {
                    return;
                }
                onRequestError(page, exception);
            }

            @Override
            public void onFinished(boolean isSuccess) {
                if (isFinishing()) {
                    return;
                }
                postOnPageAnimationEnd(new Function0<Unit>() {
                    @Override
                    public Unit invoke() {
                        if (isFinishing()) {
                            return Unit.INSTANCE;
                        }
                        onRequestFinished(page);
                        return Unit.INSTANCE;
                    }
                });
            }
        };
    }

    private class SimpleTypedCacheCallback extends SimpleTypedCallback implements Callback.CacheCallback {

        public SimpleTypedCacheCallback(Type resClass) {
            super(resClass);
        }


        @Override
        public boolean isTrustCache(String s) {
            if (!((IAdapter) mRecycler.getAdapter()).getDataSet().isEmpty()) {
                return false;
            }
            return true;
        }
    }

    private void onRequestStart(int page) {
        onPagingStart(page);
    }

    private Object onRequestPrepare(Object rawData) {
        return onPagingPrepare(rawData);
    }

    private void onRequestSuccess(int page, DataType result) {
        mPageState.setCurrentPage(page);
        final boolean firstPage = page == 1;
        if (page == 1) {
            clearList();
        }

        final int count = getListAdapter().getDataSet().getCount();
        onPagingSuccess(page, result);
        final int newCount = getListAdapter().getDataSet().getCount();
        if (!isListEmpty() && mRecycler.getVisibility() != View.VISIBLE) {
            mRecycler.setVisibility(View.VISIBLE);
        }
        if (!hasNextPage(count, newCount)) {
            mPageState.dead();
        }

    }

    protected boolean hasNextPage(int oldCount, int newCount) {
        return !(oldCount == newCount || (newCount - oldCount) < getPageSize());
    }

    protected int getPageSize() {
        return PAGING_COUNT;
    }

    protected PageState getPageState() {
        return mPageState;
    }

    private void onRequestError(int page, final Throwable ex) {
        int code;
        String msg;
        if (ex instanceof BaseException) {
            code = ((BaseException) ex).getCode();
            msg = ex.getMessage();
        } else {
            code = -1;
            msg = ex.getMessage();
        }

        mPageState.error();
        onPagingError(page, code, msg, ex);
    }

    public final void dead() {
        mPageState.dead();
    }

    private void onRequestFinished(int page) {
        mRecycler.setEnabled(true);
        final boolean firstPage = page == 1;
        if (mPageState.isError()) {
            if (firstPage && isListEmpty()) {
                getMPageTool().show(PageToolsLayout.FLAG_ERROR);
            } else {
                getMPageTool().gone();
                String msg = getMPageTool().getParams(PageToolsLayout.FLAG_ERROR).getContent().toString();
                showToast(msg);
            }
        } else {
            if (mPageState.isDead()) {
                if (isListEmpty()) {
                    if (!onPageEmpty()) {
                        getMPageTool().show(PageToolsLayout.FLAG_EMPTY);
                    }
                } else {
                    getMPageTool().gone();
                    // TODO Footer View Tools show end
                }
            } else {
                mPageState.idle();
                getMPageTool().gone();
            }
        }
        mRefreshLayout.setRefreshing(false);

        onPagingFinished(page);

        getListAdapter().notifyDataSetChanged();
    }

    protected boolean onPageEmpty() {
        return false;
    }

    protected void onPagingStart(int page) {
    }

    protected Object onPagingPrepare(Object result) {
        return result;
    }

    protected abstract void onPagingSuccess(int page, DataType result);

    protected void onPagingError(int page, int code, String msg, Throwable ex) {
        PageToolsParams params = getMPageTool().getParams(PageToolsLayout.FLAG_ERROR);
        params.setContent(msg);
        getMPageTool().setParams(PageToolsLayout.FLAG_ERROR, params);
    }

    protected void onPagingFinished(int page) {
    }

    @Override
    public void onTitleDoubleClick() {
        super.onTitleDoubleClick();
        if (getListAdapter() != null && !getListAdapter().getDataSet().isEmpty()) {
            getRecyclerView().smoothScrollToPosition(smoothScrollFirstPosition());
        }
    }

    /**
     * 当使用 expandable group 时 第一个(group)永远在0的位置(悬浮在x:0y:0),所依应该返回1
     *
     * @return
     */
    protected int smoothScrollFirstPosition() {
        return 0;
    }

    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        mScrollY += dy;
    }

    public int getScrollY() {
        return mScrollY;
    }

    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (getListAdapter().getDataSet().isEmpty() || !mPageState.isIdle()) {
            return;
        }

        RecyclerView.LayoutManager lm = recyclerView.getLayoutManager();
        int firstVisibleItem;
        int visibleItemCount;
        if (lm instanceof LinearLayoutManager) {
            firstVisibleItem = ((LinearLayoutManager) lm).findFirstVisibleItemPosition();
            visibleItemCount = ((LinearLayoutManager) lm).findLastVisibleItemPosition() - firstVisibleItem + 1;
        } else if (lm instanceof LayoutManager) {
            firstVisibleItem = ((LayoutManager) lm).findFirstVisibleItemPosition();
            visibleItemCount = ((LayoutManager) lm).findLastVisibleItemPosition() - firstVisibleItem + 1;
        } else {
            throw new RuntimeException("不支持的LayoutManager类型");
        }

        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if ((firstVisibleItem + visibleItemCount) + 2 >= adapter.getItemCount()) {
            if (mPageState.getCurrentPage() != PageState.STATE_IDLE) {
                request(mPageState.getCurrentPage());
            }
        }
    }

    /**
     * list是否为空,只计算dataSet,如果要计算头和尾,请重写此方法
     */
    protected boolean isListEmpty() {
        IAdapter listAdapter = getListAdapter();
        return listAdapter.getDataSet().isEmpty();
    }

    protected boolean isError() {
        return mPageState.isError();
    }

    protected boolean isStart() {
        return mPageState.isStart();
    }

    protected boolean isIdle() {
        return mPageState.isIdle();
    }

    protected boolean isDead() {
        return mPageState.isDead();
    }

    protected boolean isItemCanMove() {
        return false;
    }

    protected boolean isItemCanDissmiss() {
        return false;
    }

    public void setRefreshing(boolean refreshing) {
        mRefreshLayout.setRefreshing(refreshing);
    }

    private final class PageState {
        private int mCurrentPage;
        private int mState;

        public void setCurrentPage(int page) {
            this.mCurrentPage = page;
        }

        /**
         * 空闲
         */
        public static final int STATE_IDLE = 0;
        /**
         * 分页开始
         */
        public static final int STATE_START = 1;
        /**
         * 发生错误
         */
        public static final int STATE_ERROR = 2;
        /**
         * 所有分页结束
         */
        public static final int STATE_DEAD = -1;

        public void idle() {
            setState(STATE_IDLE);
        }

        public boolean isIdle() {
            return this.mState == STATE_IDLE;
        }

        public void start() {
            setState(STATE_START);
        }

        public boolean isStart() {
            return this.mState == STATE_START;
        }

        public void error() {
            setState(STATE_ERROR);
        }

        public boolean isError() {
            return this.mState == STATE_ERROR;
        }


        public void dead() {
            setState(STATE_DEAD);
        }

        public boolean isDead() {
            return this.mState == STATE_DEAD;
        }

        private void setState(int state) {
            if (mState == STATE_DEAD) {
                return;
            }
            mState = state;
        }

        public void relive() {
            mState = STATE_IDLE;
        }

        public int getCurrentPage() {
            return mCurrentPage;
        }
    }
}
