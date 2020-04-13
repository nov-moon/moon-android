package com.meili.moon.sdk.base.util;

import android.content.Context;

import com.meili.moon.sdk.base.Sdk;

/**
 * dp、sp等转换工具类
 * Created by imuto on 16/05/20.
 */
@Deprecated
public class DensityUtil {

    private DensityUtil() {
    }

    public static float getDensity() {
        return Sdk.app().getResources().getDisplayMetrics().density;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(float dpValue) {
//        if (Sdk.app() != null && Sdk.app().getResources() != null && Sdk.app().getResources().getDisplayMetrics() != null) {
        final float scale = Sdk.app().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5F);
//        }
//        return (int) dpValue;
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int px2dip(float pxValue) {
        final float scale = Sdk.app().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5F);
    }


    /**
     * 将sp值转换为px值，保证文字大小不变
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

}
