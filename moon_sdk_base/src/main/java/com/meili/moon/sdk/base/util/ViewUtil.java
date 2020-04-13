package com.meili.moon.sdk.base.util;

import android.graphics.Paint;
import android.graphics.Rect;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.base.Sdk;


/**
 * Created by imuto on 16/05/20.
 */
public class ViewUtil {

    /**
     * 增大view的点击区域(单位dp)
     */
    public static void increaseClickRegion(final View view, final int lAdd, final int tAdd, final int rAdd, final int bAdd) {
        if (view == null) return;
        final View parent = (View) view.getParent();
        if (parent == null) return;
        parent.post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.getHitRect(bounds);
                bounds.left -= DensityUtil.dip2px(lAdd);
                bounds.top -= DensityUtil.dip2px(tAdd);
                bounds.right += DensityUtil.dip2px(rAdd);
                bounds.bottom += DensityUtil.dip2px(bAdd);
                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                parent.setTouchDelegate(touchDelegate);
            }
        });
    }

    /**
     * 屏幕宽度
     */
    public static int getScreenWidth() {
        return Sdk.app().getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 屏幕高度
     */
    public static int getScreenHeight() {
        return Sdk.app().getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取控件宽
     */
    public static int getWidth(View view) {
        int width = view.getWidth();
        if (width > 0) {
            return width;
        }

        ViewGroup.LayoutParams params = view.getLayoutParams();
        int w = params != null ? params.width : 0;
        if (w > 0) {
            return w;
        }
        w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return (view.getMeasuredWidth());
    }

    /**
     * 获取控件高
     */
    public static int getHeight(View view) {
        int height = view.getHeight();
        if (height > 0) {
            return height;
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        int h = params != null ? params.height : 0;
        if (h > 0) {
            return h;
        }
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
        return (view.getMeasuredHeight());
    }

    /** 获取statusBar高度 */
    public static int getStatusBarHeight() {
        int result = 0;
        int resourceId = Sdk.app().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = Sdk.app().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取文字高度
     */
    public static int getFontHeight(float fontSize) {
        Paint paint = new Paint();
        paint.setTextSize(fontSize);
        Paint.FontMetrics fm = paint.getFontMetrics();
        return (int) Math.ceil(fm.descent - fm.ascent);
    }

    public static Rect getRectInWindow(View view) {
        Rect rect = new Rect();
        rect.left = getLeftInWindow(view);
        rect.top = getTopInWindow(view);
        rect.right = rect.left + view.getWidth();// Utils.getRightInWindow(v);
        rect.bottom = rect.top + view.getHeight();// Utils.getBottomInWindow(v);
        return rect;
    }

    public static int getLeftInWindow(View myView) {
        if (myView == null) {
            return 0;
        }
        if (myView.getParent() == myView.getRootView())
            return myView.getLeft();
        else
            return myView.getLeft() + getLeftInWindow((View) myView.getParent());
    }

    public static int getRightInWindow(View myView) {
        if (myView == null) {
            return 0;
        }
        if (myView.getParent() == myView.getRootView())
            return myView.getRight();
        else
            return myView.getRight() + getRightInWindow((View) myView.getParent());
    }

    public static int getTopInWindow(View myView) {
        if (myView == null) {
            return 0;
        }
        if (myView.getParent() == myView.getRootView())
            return myView.getTop();
        else
            return myView.getTop() + getTopInWindow((View) myView.getParent());
    }

    public static int getBottomInWindow(View myView) {
        if (myView == null) {
            return 0;
        }
        if (myView.getParent() == myView.getRootView())
            return myView.getBottom();
        else
            return myView.getBottom() + getBottomInWindow((View) myView.getParent());
    }
}
