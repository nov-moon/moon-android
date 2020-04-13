package com.meili.moon.sdk.app.base.page.widget;

import android.view.View;
import android.view.ViewGroup;

/**
 * menu的基类
 * Created by imuto on 16/5/20.
 */
public abstract class Menu {
    ViewGroup layout;
    View redTips;

    Menu() {
    }

    Menu(ViewGroup layout, View redTips) {
        this.layout = layout;
        this.redTips = redTips;
    }

    public void setVisibility(int visibility) {
        this.layout.setVisibility(visibility);
    }

    public void setEnabled(boolean enabled) {
        this.layout.setEnabled(enabled);
    }

    public void setClickable(boolean clickable) {
        this.layout.setClickable(clickable);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.layout.setOnClickListener(listener);
    }

    public void setVisibleRedTips(boolean visible) {
        redTips.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public ViewGroup getRootView() {
        return layout;
    }
}
