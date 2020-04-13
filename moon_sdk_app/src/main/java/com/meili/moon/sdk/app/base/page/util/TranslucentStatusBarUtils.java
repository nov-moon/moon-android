package com.meili.moon.sdk.app.base.page.util;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.base.Sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 沉浸式通知栏工具类
 * Created by imuto on 17/8/17.
 */
public class TranslucentStatusBarUtils {

    private static int translucentStatus = 0;

    /** app是否支持沉浸式通知栏 */
    public static boolean isSupportTranslucentStatusBarStyle() {
        if (translucentStatus == 0) {
            int[] attr = new int[]{R.attr.isSupportTranslucentStatusBarStyle};
            TypedArray typedArray = Sdk.app().getTheme().obtainStyledAttributes(attr);
            if (typedArray.getBoolean(0, true)) {
                translucentStatus = 1;
            } else {
                translucentStatus = -1;
            }
            typedArray.recycle();
        }

        return translucentStatus > 0;
    }

    /** 设置状态栏背景色 */
    public static void setStatusBarColor(@ColorInt int color, Activity activity) {
        if (color == -1) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    /** 设置浅色状态栏 */
    public static void setLightStatusBar(boolean isLight, Activity activity) {
        if (OsUtils.isMIUI()) {
            Class<? extends Window> clazz = activity.getWindow().getClass();
            try {
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                int isLightFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(activity.getWindow(), isLight ? isLightFlag : 0, isLightFlag);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (OsUtils.isFlyme()) {
            try {
                Window window = activity.getWindow();
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (isLight) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = activity.getWindow().getDecorView();
            if (isLight) {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                decorView.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        }
    }

    /** 获取statusBar的默认背景色，如果不支持则返回-1 */
    public static int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Color.TRANSPARENT;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return Sdk.app().getResources().getColor(R.color.moon_sdk_app_status_bar_def_color);
        }
        return -1;
    }

    /** 尝试开启透明状态栏 */
    public static void tryOpenTranslucentStatusBarStyle(Activity activity) {
        //设置沉浸式状态栏

        try {
            //用来修复Android沉浸式状态栏开启后，软键盘弹起resize模式失效的问题
            AndroidBug5497Workaround.assistActivity(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = activity.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 用来修复通知栏bug
     * Created by imuto on 17/8/22.
     */
    private static class AndroidBug5497Workaround {

        private View mChildOfContent;
        private int usableHeightPrevious;
        private ViewGroup.LayoutParams frameLayoutParams;
        private Rect mRect;
        private Activity mActivity;

        private AndroidBug5497Workaround(Activity activity) {
            mActivity = activity;
            if (mActivity != null) {
                mChildOfContent = mActivity.findViewById(android.R.id.content);
                mChildOfContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    public void onGlobalLayout() {
                        possiblyResizeChildOfContent();
                    }
                });
                frameLayoutParams = mChildOfContent.getLayoutParams();
            }
        }

        /** 委托重新计算Activity的高度，用来适配通知栏透明后造成的bug */
        private static void assistActivity(Activity activity) {
            new AndroidBug5497Workaround(activity);
        }

        private void possiblyResizeChildOfContent() {

            WindowManager.LayoutParams attributes = mActivity.getWindow().getAttributes();
            if ((attributes.softInputMode & WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) != 0) {
                return;
            }

            int usableHeightNow = computeUsableHeight();

            //如果两次高度不一致
            if (usableHeightNow != usableHeightPrevious) {
//            int usableHeightSansKeyboard = mChildOfContent.getRootView().getHeight();
//            int heightDifference = usableHeightSansKeyboard - usableHeightNow;
//            if (heightDifference > (usableHeightSansKeyboard / 4)) {
//                // keyboard probably just became visible
//                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference;
//            } else {
//                // keyboard probably just became hidden
//                Rect r = new Rect();
//                mChildOfContent.getWindowVisibleDisplayFrame(r);
//
//                frameLayoutParams.height = usableHeightSansKeyboard - r.top;
//            }

                frameLayoutParams.height = usableHeightNow;
                mChildOfContent.requestLayout();//请求重新布局
                usableHeightPrevious = usableHeightNow;
            }
        }

        /** 计算视图可视高度 */
        private int computeUsableHeight() {
            if (mRect == null) {
                mRect = new Rect();
            }
            mChildOfContent.getWindowVisibleDisplayFrame(mRect);
            return (mRect.bottom);
        }
    }

    /**
     * 系统工具
     * Created by imuto on 17/8/17.
     */
    private static class OsUtils {
        /** 是否是米UI系统 */
        private static boolean isMIUI() {
            return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"));
        }

        /** 是否是魅族系统 */
        private static boolean isFlyme() {
            try {
                final Method method = Build.class.getMethod("hasSmartBar");
                return method != null;
            } catch (final Exception e) {
                return false;
            }
        }

        private static String getSystemProperty(String propName) {
            String line;
            BufferedReader input = null;
            try {
                java.lang.Process p = Runtime.getRuntime().exec("getprop " + propName);
                input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
                line = input.readLine();
                input.close();
            } catch (IOException ex) {
                return null;
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return line;
        }
    }

    /**
     * 状态栏亮色模式，设置状态栏黑色文字、图标，
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param isDark  true为深色的文字 false为浅色的文字
     * @return 1:MIUUI 2:Flyme 3:android6.0
     */
    public static int statusBarLightMode(Activity activity,boolean isDark) {
        int result = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (MIUISetStatusBarLightMode(activity, isDark)) {
                    result = 1;
                } else if (FlymeSetStatusBarLightMode(activity.getWindow(), isDark)) {
                    result = 2;
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(isDark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    }else{
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }result = 3;
                }
            }
        return result;
    }

    /**
     * 已知系统类型时，设置状态栏黑色文字、图标。
     * 适配4.4以上版本MIUIV、Flyme和6.0以上版本其他Android
     *
     * @param activity
     * @param type     1:MIUUI 2:Flyme 3:android6.0
     */
    public static void statusBarLightMode(Activity activity, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity, true);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.getWindow(), true);
        } else if (type == 3) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }

    /**
     * 状态栏暗色模式，清除MIUI、flyme或6.0以上版本状态栏黑色文字、图标
     */
    public static void StatusBarDarkMode(Activity activity, int type) {
        if (type == 1) {
            MIUISetStatusBarLightMode(activity, false);
        } else if (type == 2) {
            FlymeSetStatusBarLightMode(activity.getWindow(), false);
        } else if (type == 3) {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }

    }

    /**
     * 设置状态栏图标为深色和魅族特定的文字风格
     * 可以用来判断是否为Flyme用户
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean FlymeSetStatusBarLightMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {

            }
        }
        return result;
    }

    /**
     * 需要MIUIV6以上
     *
     * @param activity
     * @param dark     是否把状态栏文字及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean MIUISetStatusBarLightMode(Activity activity, boolean dark) {
        boolean result = false;
        Window window = activity.getWindow();
        if (window != null) {
            Class clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                if (dark) {
                    extraFlagField.invoke(window, darkModeFlag, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    extraFlagField.invoke(window, 0, darkModeFlag);//清除黑色字体
                }
                result = true;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //开发版 7.7.13 及以后版本采用了系统API，旧方法无效但不会报错，所以两个方式都要加上
                    if (dark) {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    } else {
                        activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            } catch (Exception e) {

            }
        }
        return result;
    }


}
