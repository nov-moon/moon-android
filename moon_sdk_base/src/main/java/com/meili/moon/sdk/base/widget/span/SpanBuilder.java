package com.meili.moon.sdk.base.widget.span;

import android.graphics.Typeface;
import android.support.annotation.ColorInt;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.View;

import com.meili.moon.sdk.base.R;
import com.meili.moon.sdk.base.Sdk;
import com.meili.moon.sdk.base.util.VersionUtils;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.util.ArrayUtil;


/**
 * Created by Naite.Zhou on 15/11/2.
 */
public class SpanBuilder {

    private SpannableString content;

    private int mStart;
    private int mCount;

    public static SpanBuilder getInstance(CharSequence str) {
        SpanBuilder builder = new SpanBuilder();
        if (TextUtils.isEmpty(str)) {
            str = "";
        }
        builder.content = new SpannableString(str);
        return builder;
    }

    /**
     * 构建多参数化提示文案，例如 VIN码 | 里程数 | 行驶证 | 登记证。其中分隔符默认使用 ' | ' 两个空格中间一个竖线，
     * 默认分隔符颜色使用R.color.widget_span_builder_def_label资源定义的颜色
     *
     * @param params 具体的参数，如：VIN码、里程数等
     * @param color  参数的颜色值,如果传入一个color值，则直接所有属性复用此color值，
     *               如果传入多个，则长度必须和params的长度相同
     * @return 通过params和dividers组装的文本对象
     */
    public static CharSequence colorList(CharSequence[] params, @ColorInt Integer... color) {
        if (color == null || (color.length > 1 && color.length != params.length)) {
            if (VersionUtils.isDebug()) {
                throw new BaseException("color参数错误");
            }
            return null;
        }
        CharSequence[] labels = new CharSequence[params.length - 1];
        for (int i = 0; i < params.length - 1; i++) {
            labels[i] = " | ";
        }
        Integer[] colorsResult = new Integer[params.length + 1];
        for (int i = 0; i < colorsResult.length; i++) {
            if (color.length > 1) {
                colorsResult[i] = color[i];
            } else {
                colorsResult[i] = color[0];
            }
            if (i == colorsResult.length - 1) {
                colorsResult[i] = Sdk.app().getResources().getColor(R.color.moon_sdk_base_span_builder_def_label);
            }
        }
        return colorList(params, labels, colorsResult);
    }

    /**
     * 构建多参数化提示文案，例如 VIN码 | 里程数 | 行驶证 | 登记证
     *
     * @param params   具体的参数，如：VIN码、里程数等
     * @param dividers 具体的分隔符，如：|
     * @param colors   参数和分隔符的颜色值，长度必须大于等于(params.length + 1)，前面的颜色用来这是参数颜色，最后一个颜色用来设置分隔符颜色
     * @return 通过params和dividers组装的文本对象
     */
    public static CharSequence colorList(CharSequence[] params, CharSequence[] dividers, @ColorInt Integer... colors) {
        if (ArrayUtil.isEmpty(params) || ArrayUtil.isEmpty(dividers)
                || !ArrayUtil.largerSize(colors, params.length)
                || dividers.length < params.length - 1) {
            if (VersionUtils.isDebug()) {
                throw new BaseException("参数错误");
            }
            return null;
        }

        SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
        for (int i = 0; i < params.length; i++) {
            stringBuilder.append(getInstance(params[i]).color(colors[i], 0, params[i].length()).build());
            if (i != params.length - 1) {
                stringBuilder.append(getInstance(dividers[i]).color(colors[colors.length - 1], 0, dividers[i].length()).build());
            }
        }
        return stringBuilder;
    }

    public SpanBuilder color(@ColorInt int color, int start, int count) {
        checkStartAndCount(start, count);
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        try {
            content.setSpan(span, mStart, mCount + mStart, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SpanBuilder bold(boolean isBold, int start, int count) {
        checkStartAndCount(start, count);
        StyleSpan span = new StyleSpan(isBold ? Typeface.BOLD : Typeface.NORMAL);
        try {
            content.setSpan(span, mStart, mCount + mStart, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SpanBuilder underline(int start, int count) {
        checkStartAndCount(start, count);
        UnderlineSpan span = new UnderlineSpan();
        try {
            content.setSpan(span, mStart, mCount + mStart, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SpanBuilder size(int dp, int start, int count) {
        checkStartAndCount(start, count);
        AbsoluteSizeSpan span = new AbsoluteSizeSpan(dp, true);
        try {
            content.setSpan(span, mStart, mCount + mStart, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public SpanBuilder icon(int start, int resId, int textSizePx) {
        return icon(start, resId, textSizePx, 0);
    }

    /**
     * 设置图标，建议color等方法调用完成以后，最后调用此方法，原因是此方法会改变文本长度，你计算的start应该会出错
     *
     * @param start      插入位置
     * @param resId      插入的图标id
     * @param textSizePx 文本的字体大小，用于计算图标大小
     * @param lineExtra  文本的行距，如果没有设置，则为0
     */
    public SpanBuilder icon(int start, int resId, int textSizePx, int lineExtra) {

        if (start > content.length()) {
            start = 0;
        }
        mStart = start + 1;
        TextImageSpan imageSpan = new TextImageSpan(Sdk.app(), resId, textSizePx - 4, lineExtra);
        SpannableStringBuilder sb = new SpannableStringBuilder(" ");
        sb.append(content);
        try {
            sb.setSpan(imageSpan, mStart, mStart + 1, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        content = new SpannableString(sb.subSequence(0, sb.length()));
        return this;
    }

    /**
     * 添加点击事件
     *
     * @param start    点击的开始位置
     * @param count    点击的总字符数
     * @param color    文本的颜色
     * @param listener 点击的回调事件
     * @param inParam  点击回调方法里的入参
     */
    public SpanBuilder click(int start, int count, int color, Object inParam, OnSpanClickListener listener) {
        checkStartAndCount(start, count);
        UserClickSpan span = new UserClickSpan(color, inParam, listener);
        try {
            content.setSpan(span, mStart, mCount + mStart, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public CharSequence build() {
        return content;
    }

    private void checkStartAndCount(int start, int count) {
        if (start < 0) {
            start = 0;
        }
        mStart = start;
        if (mStart >= content.length()) {
            mStart = 0;
            mCount = 0;
            return;
        }
        if (count + start > content.length()) {
            count = content.length() - mStart;
        }
        mCount = count;
    }

    private class UserClickSpan extends LinkMovementClickMethod.ClickableTouchSpan {
        int color;
        Object param;
        OnSpanClickListener listener;

        UserClickSpan(int color, Object param, OnSpanClickListener lis) {
            this.color = color;
            this.param = param;
            listener = lis;
        }

        @Override
        public int getNormalTextColor() {
            return color;
        }

        @Override
        public void onClick(View widget) {
            if (listener != null) {
                listener.onClick(param);
            }
        }

    }

    public interface OnSpanClickListener {
        void onClick(Object inParam);
    }

}
