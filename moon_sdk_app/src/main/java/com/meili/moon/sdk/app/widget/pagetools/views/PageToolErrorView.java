package com.meili.moon.sdk.app.widget.pagetools.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;


/**
 * Created by imuto on 15/12/14.
 */
public class PageToolErrorView implements PageToolView {
    Context context;
    View view;
    ImageView mImg;
    TextView mTxtContent;
    TextView mTxtReload;

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_page_tool_default, parent, false);
        mImg = (ImageView) view.findViewById(R.id.iv_image);
        mTxtContent = (TextView) view.findViewById(R.id.tv_content);
        mTxtReload = (TextView) view.findViewById(R.id.mTxtReload);
        return view;
    }

    @Override
    public void onDataChanged(PageToolsParams params) {
        int drawable = params.getDrawable();
        if (drawable == 0) {
            mImg.setImageDrawable(context.getResources().getDrawable(R.drawable.moon_sdk_app_page_tool_reload));
        } else {
            mImg.setImageDrawable(context.getResources().getDrawable(drawable));
        }

        mTxtContent.setText(params.getContent());
        view.setBackgroundResource(params.getBackground());
    }

    @Override
    public void onVisibilityChanged(boolean visible) {

    }
}
