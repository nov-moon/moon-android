package com.meili.moon.sdk.base.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.Gravity;
import android.widget.Toast;

import com.meili.moon.sdk.base.Sdk;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.util.IOUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 通用工具类，随着项目增长，后期应该要重构Utils的结构
 * Created by imuto on 17/3/1.
 */
public class Utils {

    private static RestartRunnable restartAppRunnable = new RestartRunnable();

    /***
     * 复制text到黏贴板
     * @return true 复制成功， false 复制失败
     */
    public static boolean copy2Clipboard(Context context, CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        try {
            final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            final android.content.ClipData clipData = android.content.ClipData.newPlainText(text, text);
            clipboardManager.setPrimaryClip(clipData);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 重启APP
     */
    public static void restartApp(String tipMsg) {
        Sdk.task().removeCallbacks(restartAppRunnable);
        restartAppRunnable.tipMsg = tipMsg;
        Sdk.task().post(restartAppRunnable, 300);
    }

    /** 重置重启app的状态，一般在welcome中重置 */
    public static void resetRestartApp() {
        restartAppRunnable.hasRestartApp = false;
    }

    /** 是否是主进程 */
    public static boolean isMainProcess(Context context) {
        int pid = android.os.Process.myPid();

        // 通过进程的虚拟文件读取
        FileInputStream fis = null;
        try {
            File file = new File("/proc/" + pid + "/cmdline");
            if (file.exists()) {
                fis = new FileInputStream(file);
                String cmdLine = IOUtil.readText(fis, "UTF-8").trim();
                return context.getPackageName().equals(cmdLine);
            }
        } catch (IOException ex) {
            LogUtil.e("isMainProcess", ex.getMessage());

        } finally {
            IOUtil.closeQuietly(fis);
        }

        return false;
    }


    private static class RestartRunnable implements Runnable {
        boolean hasRestartApp = false;
        String tipMsg = null;

        @Override
        public void run() {
            try {
                if (hasRestartApp) {
                    return;
                }
                hasRestartApp = true;

                if (!TextUtils.isEmpty(tipMsg)) {
                    Toast toast = Toast.makeText(Sdk.app(), tipMsg, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

                Intent intent = Sdk.app().getPackageManager().getLaunchIntentForPackage(Sdk.app().getPackageName());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent = Intent.makeRestartActivityTask(intent.getComponent());
//                intent.putExtra(Constants.Commons.KEY_CLEAR_TASK, true);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                MoonSdk.page().finishAll();
                Sdk.app().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
