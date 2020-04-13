package com.meili.moon.sdk.app.widget.pagetools.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;


/**
 * Created by imuto on 16/2/17.
 */
public class FooterButtonTextView implements PageToolView {

    Context context;
    View view;

    TextView mText;

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        this.view = LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_footer_tool_button, parent, false);
        mText = (TextView) this.view.findViewById(R.id.tv_text);
        return view;
    }

    @Override
    public void onDataChanged(PageToolsParams params) {
        mText.setText(params.getContent());
    }

    @Override
    public void onVisibilityChanged(boolean visible) {

    }
}
