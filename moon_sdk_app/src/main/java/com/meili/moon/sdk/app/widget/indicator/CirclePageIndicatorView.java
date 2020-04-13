package com.meili.moon.sdk.app.widget.indicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by imuto on 15/3/23.
 */
public class CirclePageIndicatorView extends View implements PageIndicator {

    private ViewPager mViewPager;
    private int mScrollState;

    private Paint paintSelected;
    private Paint paint;

    private int pointerMargin;
    private int itemSize;
    private int current;

    public CirclePageIndicatorView(Context context) {
        this(context, null);
    }

    public CirclePageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CirclePageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paintSelected = new Paint();
        paintSelected.setAntiAlias(true);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public void setPointerSelected(@ColorRes int colorResId) {
        paintSelected.setColor(getResources().getColor(colorResId));
    }

    public void setPointerDef(@ColorRes int colorResId) {
        paint.setColor(getResources().getColor(colorResId));
    }

    public void setPointerMargin(int pixel) {
        pointerMargin = pixel;
    }

    public void setPointerSize(int pixel) {
        itemSize = pixel;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mViewPager == null) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (MeasureSpec.getSize(widthMeasureSpec) == ViewGroup.LayoutParams.MATCH_PARENT) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        int count = mViewPager.getAdapter().getCount();
        if (count == 1) {
            pointerMargin = 0;
        }
        int width = itemSize * count + (count + 1) * pointerMargin;

        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(itemSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mViewPager == null) {
            return;
        }
        for (int i = 0; i < mViewPager.getAdapter().getCount(); i++) {
            int radius = itemSize / 2;
            int x = i * (itemSize + pointerMargin) + radius + pointerMargin;
            canvas.drawCircle(x, radius, radius, paint);
        }

        int radius = itemSize / 2;
        int x = current * (itemSize + pointerMargin) + radius + pointerMargin;
        canvas.drawCircle(x, radius, radius, paintSelected);
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException(
                    "ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        current = item;
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            onPageScrolled(position, 0, 0);
        }
        current = position;
        invalidate();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }
}
