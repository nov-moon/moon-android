package com.meili.moon.sdk.app.widget.pagetools.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;


/**
 * Created by imuto on 15/12/14.
 */
public class PageToolEmptyView implements PageToolView {

    Context context;
    View view;

    ImageView mImg;
    TextView mTxtTitle;
    TextView mTxtContent;
    View mLayoutBtn;
    Button mBtnLeft;
    Button mBtnRight;

    @Override
    public View onCreateView(Context context, ViewGroup parent) {
        this.context = context;
        this.view = LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_page_tool_empty_large, parent, false);
        mImg = (ImageView) view.findViewById(R.id.iv_image);
        mTxtTitle = (TextView) view.findViewById(R.id.tv_title);
        mTxtContent = (TextView) view.findViewById(R.id.tv_content);
        mLayoutBtn = view.findViewById(R.id.layout_btn);
        mBtnLeft = (Button) view.findViewById(R.id.btn_left);
        mBtnRight = (Button) view.findViewById(R.id.btn_right);
        return view;
    }

    @Override
    public void onDataChanged(PageToolsParams params) {
        int drawable = params.getDrawable();
        if (drawable == 0) {
            mImg.setImageResource(R.drawable.moon_sdk_app_blank_empty);
        } else {
            mImg.setImageResource(drawable);
        }

        CharSequence title = params.getTitle();
        mTxtTitle.setVisibility(TextUtils.isEmpty(title) ? View.GONE : View.VISIBLE);
        mTxtTitle.setText(title);

        CharSequence content = params.getContent();
        mTxtContent.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
        mTxtContent.setText(content);

        CharSequence leftBtn = params.getLeftBtnText();
        CharSequence rightBtn = params.getRightBtnText();

        boolean leftBtnVisible = !TextUtils.isEmpty(leftBtn);
        boolean rightBtnVisible = !TextUtils.isEmpty(rightBtn);

        mBtnLeft.setVisibility(leftBtnVisible ? View.VISIBLE : View.GONE);
        mBtnLeft.setText(leftBtn);
        mBtnLeft.setOnClickListener(params.getLeftBtnListener());

        mBtnRight.setVisibility(rightBtnVisible ? View.VISIBLE : View.GONE);
        mBtnRight.setText(rightBtn);
        mBtnRight.setOnClickListener(params.getRightBtnListener());

        mLayoutBtn.setVisibility(leftBtnVisible || rightBtnVisible ? View.VISIBLE : View.GONE);

        if (params.getBackground() != 0) {
            view.setBackgroundResource(params.getBackground());
        }
    }

    @Override
    public void onVisibilityChanged(boolean visible) {

    }
}
