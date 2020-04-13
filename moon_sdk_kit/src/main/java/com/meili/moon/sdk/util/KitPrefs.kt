package com.meili.moon.sdk.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.log.LogUtil
import java.util.*

/**
 * Created by imuto on 2019/1/14.
 */
object KitPrefs {

    private val mPref: SharedPreferences = CommonSdk.app()
            .getSharedPreferences("kit_info2", Context.MODE_PRIVATE)

    private var IMEI = ""
    private var DI = ""

    init {
        IMEI = getIMEI()
        DI = getDeviceID()
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getIMEI(): String {
        if (!isEmpty(IMEI)) {
            return IMEI
        }

        var result: String? = mPref.getString("device_imei", null)
        if (result == null) {
            try {
                val tm = CommonSdk.app().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                result = tm.deviceId
            } catch (e: Throwable) {
                LogUtil.e(e)
            }
            if (result == null) {
                result = getDeviceID()
            }
            setIMEI(result)
        }

        IMEI = result
        return result
    }

    private fun setIMEI(imei: String) {
        IMEI = imei
        mPref.edit().putString("device_imei", imei).apply()
    }

    fun getDeviceID(): String {
        if (!isEmpty(DI)) {
            return DI
        }
        var result = mPref.getString("device_uuid", null)
        if (result == null) {

            result = try {
                val uuid = UUID.randomUUID().toString()
                val split = uuid.split("-")
                val nano = System.currentTimeMillis().toString()
                split[3] + nano.subSequence(2, nano.length)
            } catch (e: Throwable) {
                System.nanoTime().toString()
            }
            setDeviceID(result)
        }
        DI = result
        return result
    }

    private fun setDeviceID(di: String) {
        DI = di
        mPref.edit().putString("device_uuid", di).apply()
    }

}