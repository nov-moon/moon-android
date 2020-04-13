package com.meili.moon.sdk.app.base.page.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;


/**
 * 文本menu
 * Created by imuto on 16/5/20.
 */
public class MenuText extends Menu {
    TextView textView;

    public MenuText(Context context) {
        layout = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_title_bar_menu_right_text, null, false);
        textView = layout.findViewById(R.id.tv_title_bar);
        redTips = layout.findViewById(R.id.iv_icon_tips);

        TypedArray typedArray = context
                .obtainStyledAttributes(null, R.styleable.MoonTitleBarView, R.attr.moonTitleBarView, 0);
        setTextColor(typedArray.getColor(R.styleable.MoonTitleBarView_titleBarMenuTextColor, Color.BLACK));
        typedArray.recycle();
    }

    public MenuText(ViewGroup layout, View redTips, TextView textView) {
        super(layout, redTips);
        this.textView = textView;
    }

    public void setText(CharSequence text) {
        this.textView.setText(text);
    }

    public void setTextColor(@ColorInt int textColor) {
        this.textView.setTextColor(textColor);
    }

    public String getText() {
        if (null != textView.getText()) {
            return textView.getText().toString();
        } else {
            return "";
        }
    }
}
