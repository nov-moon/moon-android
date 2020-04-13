package com.meili.moon.sdk.app.widget.indicator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Scroller;


/**
 * Created by imuto on 15/3/23.
 */
public class PageIndicatorView extends View implements PageIndicator {

    private ViewPager mViewPager;
    private int mScrollState;
    private float mPageOffset;

    private Bitmap mBarBitmap;

    private int width;
    private int height;

    private Scroller mScroller;

    private Paint paint;

    public PageIndicatorView(Context context) {
        this(context, null);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new Scroller(context);
//        int width = DensityUtil.dip2px(70);
//        Bitmap bitmap = Bitmap.createBitmap(width, DensityUtil.dip2px(4), Bitmap.Config.RGB_565);
//        Canvas c = new Canvas(bitmap);
//        int color = isInEditMode() ? Color.rgb(0xCD, 0x64, 0x56) : getResources().getColor(R.color.c_cd6456);
//        c.drawColor(color);
//        setIndicatorPointer(bitmap);

        paint = new Paint();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    public boolean hasInitIndicatorPointer() {
        return mBarBitmap != null && !mBarBitmap.isRecycled();
    }

    public void setIndicatorPointer(Bitmap bitmap) {
        Bitmap oldBitmap = mBarBitmap;

        this.width = bitmap.getWidth();
        this.height = bitmap.getHeight();
        mBarBitmap = bitmap;

        if (oldBitmap != null && !oldBitmap.isRecycled()) {
            oldBitmap.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBarBitmap == null || mBarBitmap.isRecycled()) {
            return;
        }

        canvas.drawBitmap(mBarBitmap, 0, 0, paint);
    }

    @Override
    public void setViewPager(ViewPager view) {
        if (mViewPager == view) {
            return;
        }
        if (mViewPager != null) {
            mViewPager.setOnPageChangeListener(null);
        }
        if (view.getAdapter() == null) {
            throw new IllegalStateException(
                    "ViewPager does not have adapter instance.");
        }
        mViewPager = view;
        mViewPager.setOnPageChangeListener(this);
//        invalidate();
    }

    @Override
    public void setViewPager(ViewPager view, int initialPosition) {
        setViewPager(view);
        setCurrentItem(initialPosition);
    }

    @Override
    public void setCurrentItem(int item) {
        if (mViewPager == null) {
            throw new IllegalStateException("ViewPager has not been bound.");
        }
        mViewPager.setCurrentItem(item);
    }

    @Override
    public void notifyDataSetChanged() {
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mPageOffset = positionOffset;
        int x = (int) -((position + positionOffset) * width);
        scrollTo(x, 0);
    }

    @Override
    public void onPageSelected(int position) {
        if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
            onPageScrolled(position, 0, 0);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        mScrollState = state;
    }

    @Override
    public void computeScroll() {
        if (!mScroller.computeScrollOffset()) {
            return;
        }

        final int oldX = getScrollX();
        int x = mScroller.getCurrX();
        if (oldX != x) {
            super.scrollTo(x, 0);
        }
        invalidate();
    }
}
