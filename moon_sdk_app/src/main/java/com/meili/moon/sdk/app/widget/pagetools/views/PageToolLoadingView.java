package com.meili.moon.sdk.app.widget.pagetools.views;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.WaveView;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;


/**
 * Created by imuto on 15/12/14.
 */
public class PageToolLoadingView implements PageToolView {

    private View view;
    private ImageView mMNPageToolsImgLoading;
    private TextView mMNPageToolsTxtLoading;
    private WaveView mMNWaveLoading;

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        view = LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_page_tool_loading, parent, false);
        mMNPageToolsImgLoading = view.findViewById(R.id.mMNPageToolsImgLoading);
        mMNPageToolsTxtLoading = (TextView) view.findViewById(R.id.mMNPageToolsTxtLoading);
        mMNWaveLoading = (WaveView) view.findViewById(R.id.mWaveView);
        return view;
    }

    @Override
    public void onDataChanged(PageToolsParams params) {
        CharSequence content = params.getContent();
        mMNPageToolsTxtLoading.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
        mMNPageToolsTxtLoading.setText(content);
        int bg = params.getBackground();
        if (bg != 0) {
            view.setBackgroundResource(params.getBackground());
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        if (visible) {
            mMNWaveLoading.startAnimation();
//            ((AnimationDrawable) mMNPageToolsImgLoading.getDrawable()).start();
        } else {
            mMNWaveLoading.stopAnimation();
//            ((AnimationDrawable) mMNPageToolsImgLoading.getDrawable()).stop();
        }
    }

}
