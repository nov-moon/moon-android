package com.meili.moon.sdk.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import com.meili.moon.sdk.CommonSdk

/**
 * 获取当前网络是否可用，依赖CommonSdk的初始化
 * Created by imuto on 2018/9/12.
 */
fun isNetworkEnable(): Boolean {
    val isNetConnected: Boolean
    // 获得网络连接服务
    val connManager = CommonSdk.app().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connManager.activeNetworkInfo
    isNetConnected = info != null && info.isAvailable
    return isNetConnected
}

/**
 * 获取网络类型，依赖CommonSdk的初始化
 */
fun getNetworkType(): String {
    var type = ""
    val connectManager = CommonSdk.app().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val info = connectManager.activeNetworkInfo
    if (info == null) {
        type = "null"
    } else if (info.type == ConnectivityManager.TYPE_WIFI) {
        val wifiManager = CommonSdk.app().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo.ssid
        type = "wifi:$ssid"
    } else if (info.type == ConnectivityManager.TYPE_MOBILE) {
        val subType = info.subtype
        if (subType == TelephonyManager.NETWORK_TYPE_CDMA || subType == TelephonyManager.NETWORK_TYPE_GPRS || subType == TelephonyManager.NETWORK_TYPE_EDGE) {
            type = "2g"
        } else if (subType == TelephonyManager.NETWORK_TYPE_UMTS || subType == TelephonyManager.NETWORK_TYPE_HSDPA || subType == TelephonyManager.NETWORK_TYPE_EVDO_A || subType == TelephonyManager.NETWORK_TYPE_EVDO_0
                || subType == TelephonyManager.NETWORK_TYPE_EVDO_B) {
            type = "3g"
        } else if (subType == TelephonyManager.NETWORK_TYPE_LTE) {// LTE是3g到4g的过渡，是3.9G的全球标准
            type = "4g"
        }
    }
    return type
}