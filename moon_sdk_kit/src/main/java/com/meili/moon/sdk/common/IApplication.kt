package com.meili.moon.sdk.common

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter

/**
 * 自定义通用Application的规范接口
 * Created by imuto on 17/10/18.
 */
interface IApplication {

    val isRunning: Boolean

    fun sendLocalBroadcast(intent: Intent)

    fun registerLocalReceiver(receiver: BroadcastReceiver, filter: IntentFilter)

    fun unregisterLocalReceiver(receiver: BroadcastReceiver)

    fun registerActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks)

    fun unregisterActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks)

    fun application(): Application?
}
