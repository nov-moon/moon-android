package com.meili.moon.sdk.app.base.page.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.meili.moon.sdk.app.R;


/**
 * 图标menu
 * Created by imuto on 16/5/20.
 */
public class MenuIcon extends Menu {

    final ImageView imageView;

    public MenuIcon(Context context) {
        ViewGroup group = (ViewGroup) LayoutInflater.from(context).
                inflate(R.layout.moon_sdk_app_title_bar_menu_right_icon, null, false);
        layout = group;
        imageView = group.findViewById(R.id.iv_title_bar_icon);
    }

    public void setImageResource(int resId) {
        this.imageView.setImageResource(resId);
    }

}
