package com.meili.moon.sdk.app.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.meili.moon.sdk.base.util.DensityUtil;
import com.meili.moon.sdk.log.LogUtil;


public class ProgressBarCircularIndicator extends View {

    private final static String SCHEMAS = "http://schemas.android.com/apk/res/android";

    private volatile int backgroundColor = Color.WHITE;

    private Paint mPaint = new Paint();
    private Paint mPaintTransparent = new Paint();
    private Paint mPaintBg = new Paint();


    public ProgressBarCircularIndicator(Context context) {
        super(context);
        initView(null);
    }

    public ProgressBarCircularIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public ProgressBarCircularIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ProgressBarCircularIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        setMinimumHeight(DensityUtil.dip2px(10));
        setMinimumWidth(DensityUtil.dip2px(10));

        if (attrs != null) {
            try {
                int bgColor = attrs.getAttributeResourceValue(SCHEMAS, "background", backgroundColor);
                if (bgColor != backgroundColor) {
                    setBackgroundColor(getResources().getColor(bgColor));
                } else {
                    int background = attrs.getAttributeIntValue(SCHEMAS, "background", backgroundColor);
                    if (background != backgroundColor) {
                        setBackgroundColor(background);
                    }
                }
            } catch (Throwable ignored) {
                LogUtil.e(ignored.getMessage(), ignored);
            }
        }


        mPaintTransparent.setAntiAlias(true);
        mPaintTransparent.setColor(Color.TRANSPARENT);
        mPaintTransparent.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        mPaintBg.setAntiAlias(true);
        mPaintBg.setColor(backgroundColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawAnimation(canvas);
        if (isShown()) {
            invalidate();
        }
    }

    int startAngle = 1;
    int sweepAngle = 0;
    float rotateAngle = 0;
    int limit = 0;

    private void drawAnimation(Canvas canvas) {
        if (sweepAngle == limit)
            startAngle += 6;
        if (startAngle >= 290 || sweepAngle > limit) {
            sweepAngle += 6;
            startAngle -= 6;
        }
        if (sweepAngle > limit + 290) {
            limit = sweepAngle;
            sweepAngle = limit;
            startAngle = 1;
        }
        rotateAngle += 4;
        canvas.rotate(rotateAngle, getWidth() / 2, getHeight() / 2);

        Bitmap bitmap;
        if (isInEditMode()) {
            bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas temp = new Canvas(bitmap);

        temp.drawArc(new RectF(0, 0, getWidth(), getHeight()), sweepAngle, startAngle, true, mPaintBg);

        temp.drawCircle(getWidth() / 2, getHeight() / 2, (getWidth() / 2) - DensityUtil.dip2px(2F), mPaintTransparent);

        canvas.drawBitmap(bitmap, 0, 0, mPaint);
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    @Override
    public void setBackground(Drawable background) {
        if (background instanceof ColorDrawable) {
            this.backgroundColor = ((ColorDrawable) background).getColor();
        } else {
            super.setBackground(background);
        }
    }

    @Override
    public void setBackgroundDrawable(Drawable background) {
        if (background instanceof ColorDrawable) {
            this.backgroundColor = ((ColorDrawable) background).getColor();
        } else {
            super.setBackgroundDrawable(background);
        }
    }
}
