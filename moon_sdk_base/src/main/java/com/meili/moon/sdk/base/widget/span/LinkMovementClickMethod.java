package com.meili.moon.sdk.base.widget.span;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.meili.moon.sdk.base.R;
import com.meili.moon.sdk.base.Sdk;


/**
 * Created by imuto on 15-3-26.
 */
public class LinkMovementClickMethod extends LinkMovementMethod {

    private ClickableTouchSpan mPressedSpan;
    private View.OnClickListener mOnClickListener;

    public void setOnClickListener(View.OnClickListener mOnClickListener) {
        this.mOnClickListener = mOnClickListener;
    }

    public static abstract class ClickableTouchSpan extends ClickableSpan {
        private boolean mIsPressed;

        @ColorInt
        public int getNormalTextColor() {
            return Sdk.app().getResources().getColor(R.color.moon_sdk_base_span_link);
        }

        @ColorInt
        public int getPressedTextColor() {
            return getNormalTextColor();
        }

        public int getPressedBackgroundColor() {
            return Sdk.app().getResources().getColor(R.color.moon_sdk_base_span_press);
        }

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        public boolean isUnderline() {
            return false;
        }

        @Override
        public final void updateDrawState(TextPaint tp) {
            super.updateDrawState(tp);
            tp.setColor(mIsPressed ? getPressedTextColor() : getNormalTextColor());
            tp.bgColor = mIsPressed ? getPressedBackgroundColor() : Color.TRANSPARENT;
            tp.setUnderlineText(isUnderline());
        }
    }

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        if (textView == null) {
            return false;
        }
        boolean result = true;
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mPressedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(mPressedSpan),
                        spannable.getSpanEnd(mPressedSpan));
                result = true;
            } else {
                textView.getParent().requestDisallowInterceptTouchEvent(false);
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            ClickableTouchSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (mPressedSpan != null && touchedSpan != mPressedSpan) {
                mPressedSpan.setPressed(false);
                mPressedSpan = null;
                Selection.removeSelection(spannable);
                result = true;
            }
        } else {
            if (mPressedSpan != null) {
                mPressedSpan.setPressed(false);
                super.onTouchEvent(textView, spannable, event);
                result = false;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(textView);
                }
            }
            mPressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return result;
    }

    public ClickableTouchSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        //获取当前touch点的文本行号
        int line = layout.getLineForVertical(y);
        //获取当前touch点对应的文本index，比如当前行为第二行，第一行有10个文字，第二行为abcd，点击到了b，则返回值为12
        int off = layout.getOffsetForHorizontal(line, x);
        ClickableTouchSpan touchedSpan = null;
        if (off < spannable.length()) {
            //获取点击的点对应的span
            ClickableTouchSpan[] link = spannable.getSpans(off, off, ClickableTouchSpan.class);
            if (link.length > 0) {
                touchedSpan = link[0];
            }
        }

        return touchedSpan;
    }

    public static LinkMovementClickMethod getInstance() {
        return new LinkMovementClickMethod();
    }

}
