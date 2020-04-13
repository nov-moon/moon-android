package com.meili.moon.sdk.base.util

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.meili.moon.sdk.log.TAG

/**
 * Created by imuto on 2019/3/22.
 */
object ActivityHolder : Application.ActivityLifecycleCallbacks {

    val isFront: Boolean
        get() {
            activitysMap.forEach {
                if (it.value >= 1) return true
            }
            return false
        }

    private val activitysMap = mutableMapOf<String, Int>()

    override fun onActivityPaused(activity: Activity?) {
        Log.e(TAG, "onActivityPaused: $activity")
        activity ?: return
        try {
            activitysMap[activity.toString()] = -1
        } catch (throwable: Throwable) {
        }
    }

    override fun onActivityResumed(activity: Activity?) {
        Log.e(TAG, "onActivityResumed: $activity")
        activity ?: return
        try {
            activitysMap[activity.toString()] = 2
        } catch (throwable: Throwable) {
        }

    }

    override fun onActivityStarted(activity: Activity?) {
        Log.e(TAG, "onActivityStarted: $activity")
        activity ?: return
        try {
            activitysMap[activity.toString()] = 1
        } catch (throwable: Throwable) {
        }
    }

    override fun onActivityDestroyed(activity: Activity?) {
        Log.e(TAG, "onActivityDestroyed: $activity")
        activity ?: return
        try {
            activitysMap.remove(activity.toString())
        } catch (throwable: Throwable) {
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        Log.e(TAG, "onActivitySaveInstanceState: $activity")
    }

    override fun onActivityStopped(activity: Activity?) {
        Log.e(TAG, "onActivityStopped: $activity")
        activity ?: return
        try {
            activitysMap[activity.toString()] = -2
        } catch (throwable: Throwable) {
        }
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        Log.e(TAG, "onActivityCreated: $activity")
        activity ?: return
        try {
            activitysMap[activity.toString()] = 0
        } catch (throwable: Throwable) {
        }
    }
}