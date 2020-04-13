package com.meili.moon.sdk.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.meili.moon.sdk.CommonSdk;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 设备工具类
 * <p>
 * Created by imuto on 16/5/24.
 */
public class DeviceUtil {

    private static String MAC_ADDRESS;

    public static boolean isNetConnected(Context context) {
        boolean isNetConnected;
        // 获得网络连接服务
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            // String name = info.getTypeName();
            // L.i("当前网络名称：" + name);
            isNetConnected = true;
        } else {
            //L.i("没有可用网络");
            isNetConnected = false;
        }
        return isNetConnected;
    }

    /**
     * 获取网络类型
     *
     * @return
     */
    public static String getCurrentNetType() {
        String type = "";
        ConnectivityManager cm = (ConnectivityManager) CommonSdk.app().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null) {
            type = "null";
        } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wifiManager = (WifiManager) CommonSdk.app().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            type = "wifi:" + ssid;
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            int subType = info.getSubtype();
            if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
                type = "2g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
                type = "3g";
            } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
                type = "4g";
            }
        }
        return type;
    }

    /**
     * mac地址
     */
    public static String getMacAddress() {
        if (TextUtils.isEmpty(MAC_ADDRESS)) {
//            WifiManager wifi = (WifiManager) Sdk.app().getSystemService(Context.WIFI_SERVICE);
//            WifiInfo info = wifi.getConnectionInfo();
//            if (info != null && !TextUtils.isEmpty(info.getMacAddress())) {
//                MAC_ADDRESS = info.getMacAddress();
//            }
            MAC_ADDRESS = getMacByShell();
        }
        return MAC_ADDRESS;
    }

    private static String getMacByShell() {
        String macSerial = null;
        String str = "";

        try {
            Process pp = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            InputStreamReader ir = new InputStreamReader(pp.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);

            for (; null != str; ) {
                str = input.readLine();
                if (str != null) {
                    macSerial = str.trim();// 去空格
                    break;
                }
            }
        } catch (IOException ex) {
            // 赋予默认值
            ex.printStackTrace();
        }
        return macSerial;
    }

    public static String getIMEI() {
        return KitPrefs.INSTANCE.getIMEI();
    }

    public static String getDI() {
        return KitPrefs.INSTANCE.getDeviceID();
    }

    public static boolean isEmulator(Context context) {
        if (CommonSdk.environment().isDebug()) {
            return false;
        }

        try {
            String imei = getIMEI();
            String android_id = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
            if ("000000000000000".equals(imei) ||
                    TextUtils.isEmpty(android_id) || CheckEmulatorBuild()) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Boolean CheckEmulatorBuild() {
        String BOARD = android.os.Build.BOARD;
        String BOOTLOADER = android.os.Build.BOOTLOADER;
        String BRAND = android.os.Build.BRAND;
        String DEVICE = android.os.Build.DEVICE;
        String HARDWARE = android.os.Build.HARDWARE;
        String MODEL = android.os.Build.MODEL;
        String PRODUCT = android.os.Build.PRODUCT;
        return "generic".equalsIgnoreCase(BRAND) || "generic".equalsIgnoreCase(DEVICE)
                || "sdk".equalsIgnoreCase(MODEL) || "sdk".equalsIgnoreCase(PRODUCT)
                || "goldfish".equalsIgnoreCase(HARDWARE);
    }

    /** 获取运营商名称 */
    public static String getSimOperatorName() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) CommonSdk.app().getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getSimOperatorName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /** 获取SIM卡运营商ID */
    public static String getSimOperatorID() {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) CommonSdk.app().getSystemService(Context.TELEPHONY_SERVICE);
            return telephonyManager.getSimOperator();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取ip地址
     */
    public static String getIPAddress() {
        try {
            NetworkInfo info = ((ConnectivityManager) CommonSdk.app()
                    .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                    try {
                        //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                        for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                            NetworkInterface intf = en.nextElement();
                            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                                InetAddress inetAddress = enumIpAddr.nextElement();
                                if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                    return inetAddress.getHostAddress();
                                }
                            }
                        }
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                    WifiManager wifiManager = (WifiManager) CommonSdk.app().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    return intIP2StringIP(wifiInfo.getIpAddress());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }
}
