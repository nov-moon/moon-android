package com.meili.moon.sdk.app.base.page.widget;

import android.graphics.drawable.Drawable;
import android.support.annotation.StyleRes;
import android.view.View;

import com.meili.moon.sdk.page.internal.animators.PageAnimator;

/**
 * titleBar的标准接口
 * <p>
 * 主要提供设置title、backText、添加menu
 * Created by imuto on 16/5/20.
 */
public interface ITitleBarView {

    void setTitle(CharSequence text);

    void setTitle(int resId);

    void setH5CloseVisible();

    void setH5CloseGone();

    void setTitleTextColor(int color);

    void setBackIcon(int resId);

    void setBackIcon(Drawable drawable);

    void setBackIconVisible(int visible);

    void setBackText(int resId);

    void setBackText(CharSequence text);

    void setBackTextColor(int resId);

    void setTitleBarBackgroundColor(int color);

    void setTitleBarBackgroundDrawable(int resId);

    void addMenu(String id, Menu menu);

    void removeMenu(Menu menu);

    void setTitleBarVisibility(int visible);

    int getTitleBarVisibility();

    /** 设置是否支持无网络连接的View */
    void setSupportNoNetworkStyle(boolean isSupport);

    /** 设置样式 */
    void setStyle(@StyleRes int style);

    interface ImplView extends ITitleBarView, PageAnimator {

        /** 没有动画 */
        int PAGE_ANIM_FLAG_NONE = 100;
        /** 带有alpha的动画 */
        int PAGE_ANIM_FLAG_ALPHA = 0;
        /** 左右拖拽的动画 */
        int PAGE_ANIM_FLAG_SLID = 1;

        void setBackClickListener(View.OnClickListener lis);
        void setH5BackClickListener(View.OnClickListener lis);


        void setOnTitleDoubleClickListener(View.OnClickListener lis);

        /**获取titleBar的高度*/
        int getTitleBarHeight();

        /** 设置页面动画的标识: 0，透明度渐变动画。1，右划动画。100，无动画 */
        void setPageAnimatorFlag(int flag);

        /** 获取页面动画的标识: PAGE_ANIM_FLAG_ALPHA(0)，透明度渐变动画。PAGE_ANIM_FLAG_SLID(1)，右划动画。NO_PAGE_ANIM_FLAG(100)，无动画 */
        int getPageAnimatorFlag();
    }
}
