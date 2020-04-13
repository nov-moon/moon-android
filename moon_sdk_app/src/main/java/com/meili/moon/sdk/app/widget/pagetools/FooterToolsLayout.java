package com.meili.moon.sdk.app.widget.pagetools;

import android.content.Context;
import android.util.AttributeSet;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.pagetools.views.FooterButtonTextView;
import com.meili.moon.sdk.app.widget.pagetools.views.FooterToolLoadingView;
import com.meili.moon.sdk.app.widget.pagetools.views.FooterToolTextView;


/**
 * Created by imuto on 15/12/29.
 */
public class FooterToolsLayout extends PageToolsLayout {

    private boolean hasInit;

    public FooterToolsLayout(Context context) {
        this(context, null);
    }

    public FooterToolsLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FooterToolsLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initViews() {
        if (hasInit) {
            return;
        }
        hasInit = true;

        if (!isContainFlag(FLAG_LOADING)) {
            PageToolsParams loadingParams = new PageToolsParams();
            loadingParams.setContent("数据加载中...");
            addPageToolView(FLAG_LOADING, new FooterToolLoadingView(), loadingParams);
        }

        onAddEmptyView();

        if (!isContainFlag(FLAG_ERROR)) {
            PageToolsParams errorParams = new PageToolsParams();
            errorParams.setImage(R.drawable.moon_sdk_app_page_tool_reload);
            errorParams.setBackground(R.color.page_background);
            errorParams.setContent("出了点问题,点击重试");
            addPageToolView(FLAG_ERROR, new FooterButtonTextView(), errorParams);
        }

    }

    protected void onAddEmptyView() {
        if (!isContainFlag(FLAG_EMPTY)) {
            PageToolsParams emptyParams = new PageToolsParams();
            emptyParams.setContent("--  没有更多了  --");
            addPageToolView(FLAG_EMPTY, new FooterToolTextView(), emptyParams);
        }
    }

    @Override
    protected void onGonCurr(PageToolsLayout.ToolViewState state, boolean isSwitchType) {
        state.gone();
    }

    public boolean isHasInit() {
        return hasInit;
    }

    public void setHasInit(boolean hasInit) {
        this.hasInit = hasInit;
    }
}
