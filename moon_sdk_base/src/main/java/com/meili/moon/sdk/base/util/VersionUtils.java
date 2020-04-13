package com.meili.moon.sdk.base.util;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.meili.moon.sdk.base.Sdk;
import com.meili.moon.sdk.log.LogUtil;

/**
 * 版本帮助类
 * <p>
 * Created by imuto on 16/6/2.
 */
public class VersionUtils {

    private static String channel = "meili";
    private static BuildType buildType;
    private static final Object buildTypeLock = new Object();
    private static boolean isDebug = false;

    public enum BuildType {
        /**
         * 开发
         */
        BUILD_TYPE_DEBUG(0, "开发"),
        /**
         * 测试
         */
        BUILD_TYPE_DEV(1, "测试"),
        /**
         * 预发
         */
        BUILD_TYPE_PRE_RELEASE(2, "预发"),
        /**
         * 灰度
         */
        BUILD_TYPE_STAGE(3, "灰度"),
        /**
         * 自定义
         */
        BUILD_TYPE_CUSTOM(20, "自定义"),
        /**
         * 线上
         */
        BUILD_TYPE_RELEASE(1000, "线上");

        private int value;

        private String name;

        BuildType(int v, String n) {
            value = v;
            name = n;
        }

        public static BuildType get(int value) {
            for (BuildType buildType : values()) {
                if (buildType.value == value) {
                    return buildType;
                }
            }
            return null;
        }

        public int getValue() {
            return value;

        }
        public String getName() {
            return name;
        }

        public boolean isCustom() {
            return this == BUILD_TYPE_CUSTOM;
        }
    }

    /**
     * 同步debug属性,建议在APP初始化的时候调用一次
     */
    public static void syncDebug() {
        ApplicationInfo info = Sdk.app().getApplicationInfo();
        if (info != null) {
            isDebug = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
    }

    /**
     * 当前工程是否是debug模式
     *
     * @see #syncDebug()
     */
    public static boolean isDebug() {
        return isDebug;
    }

    /**
     * 获取渠道号
     */
    public static String getChannel() {
        return channel;
    }

    /**
     * 获取版本名称, 如1.1.0
     */
    public static String getVersionName() {
        try {
            PackageManager manager = Sdk.app().getPackageManager();
            PackageInfo info = manager.getPackageInfo(Sdk.app().getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取版本号, 如20160620
     */
    public static int getVersionCode() {
        try {
            PackageManager manager = Sdk.app().getPackageManager();
            PackageInfo info = manager.getPackageInfo(Sdk.app().getPackageName(), 0);
            return info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 传入版本是否为新版本
     *
     * @param versionName 如,1.1.0
     * @return true 是新版本, false 不是新版本
     */
    public static boolean isNewVersion(String versionName) {
        return isBiggerVersion(versionName);
    }

    /**
     * 当前版本是否比最低兼容版本还要低,如果还要低,则返回true,否则返回false
     *
     * @param versionName 最低兼容版本名称
     */
    public static boolean isForceUpdate(String versionName) {
        return isBiggerVersion(versionName);
    }

    /**
     * 获取当前app的构建类型,0:debug构建,1:test构建,2:preview构建,3:stg,4:release构建
     */
    public static BuildType getBuildType() {
        if (buildType == null) {
            synchronized (buildTypeLock) {
                if (buildType == null) {
                    ApplicationInfo appInfo;
                    try {
                        appInfo = Sdk.app().getPackageManager()
                                .getApplicationInfo(Sdk.app().getPackageName(), PackageManager.GET_META_DATA);
                        int value = (int) appInfo.metaData.get("BUILD_VERSION");
                        buildType = BuildType.get(value);
                    } catch (ClassCastException ex) {
                        buildType = null;
                        LogUtil.e(ex, ex.getMessage());
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        buildType = null;
                    }
                }
            }
        }
        return buildType;
    }

    /**
     * 用点（.）对服务器版本号和本地版本号进行分组，拿两个数组的最小长度做for循环，如果相同位的值不相等，则返回结果。
     * 如果相等，则对两个数组分别计算和，做和的比较
     *
     * @param versionName 服务器版本
     * @return true，服务器版本比当前版本高，false，相反
     */
    private static boolean isBiggerVersion(String versionName) {
        if (TextUtils.isEmpty(versionName)) {
            return false;
        }
        try {
            String[] versions = versionName.split("\\.");
            if (versions.length <= 0) {
                versions = new String[]{versionName};
            }
            String currVersion = getVersionName();
            if (currVersion == null) {
                return false;
            }
            String[] versionsCurr = currVersion.split("\\.");
            int count = Math.min(versions.length, versionsCurr.length);
            for (int i = 0; i < count; i++) {
                int versionNew = Integer.parseInt(versions[i]);
                int versionCurr = Integer.parseInt(versionsCurr[i]);
                if (versionNew != versionCurr) {
                    return versionNew > versionCurr;
                }
            }

            if (versions.length == versionsCurr.length) {
                return false;
            }

            int newMax = 0;
            for (String version : versions) {
                int versionNew = Integer.parseInt(version);
                newMax += Math.abs(versionNew);
            }

            int currMax = 0;
            for (String version : versionsCurr) {
                int versionNew = Integer.parseInt(version);
                currMax += Math.abs(versionNew);
            }

            return newMax > currMax;
        } catch (Exception e) {
            LogUtil.e(e);
        }
        return false;
    }

    public static boolean isApk(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        try {
            PackageManager packageManager = Sdk.app().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES);
            if (packageInfo == null || TextUtils.isEmpty(packageInfo.packageName)) {
                return false;
            }
        } catch (Throwable throwable) {
            return false;
        }
        return true;
    }
}
