package com.meili.moon.sdk.page.internal.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.meili.moon.sdk.page.internal.animators.EdgeTouchHolder;


public class SdkActivityContainerView extends RelativeLayout {

    public SdkActivityContainerView(Context context) {
        super(context);
    }

    public SdkActivityContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SdkActivityContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return EdgeTouchHolder.INSTANCE.intercept(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return EdgeTouchHolder.INSTANCE.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public void removeView(View view) {
        doBefore();
        super.removeView(view);
        doAfter();
    }

    @Override
    public void removeViewAt(int index) {
        doBefore();
        super.removeViewAt(index);
        doAfter();
    }

    @Override
    public void removeViewInLayout(View view) {
        doBefore();
        super.removeViewInLayout(view);
        doAfter();
    }

    @Override
    public void removeViews(int start, int count) {
        doBefore();
        super.removeViews(start, count);
        doAfter();
    }

    @Override
    public void removeAllViewsInLayout() {
        doBefore();
        super.removeAllViewsInLayout();
        doAfter();
    }

    private void doBefore() {
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
    }

    private void doAfter() {
        setDescendantFocusability(FOCUS_BEFORE_DESCENDANTS);
    }
}
