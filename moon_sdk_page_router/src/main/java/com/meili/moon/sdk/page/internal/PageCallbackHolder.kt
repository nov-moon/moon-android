package com.meili.moon.sdk.page.internal

import android.app.Activity
import android.content.Intent
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.page.OnPageResultCallback
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.sdk.page.exception.PageCallbackException
import com.meili.moon.sdk.page.resultCode
import com.meili.moon.sdk.util.reflect1Class
import com.meili.moon.sdk.util.throwOnDebug
import java.util.concurrent.atomic.AtomicInteger

/**
 * Created by imuto on 2019-06-11.
 */
object PageCallbackHolder {

    private val mCallbackCache = mutableMapOf<PageIntent, Pair<OnPageResultCallback<Any>, Any?>>()
    private val mCallback = mutableMapOf<String, Pair<OnPageResultCallback<Any>, Any?>>()

    private val mActivityRequestCode = AtomicInteger(6578)
    private val mActivityCallback = mutableMapOf<Int, ActivityCallbackModel>()

    fun registerCallback(intent: PageIntent, callback: OnPageResultCallback<Any>?, destroyable: Any?) {
        callback ?: return
        mCallbackCache[intent] = Pair(callback, destroyable)
    }

    fun fixCallback(intent: PageIntent, tag: String) {
        val callback = mCallbackCache[intent] ?: return

        remove(intent)

        mCallback[tag] = callback
    }


    fun registerActivityCallback(callback: OnPageResultCallback<Intent>?, receiveCancel: Boolean, destroyable: IDestroable?): Int {
        callback ?: return -1
        val requestCode = mActivityRequestCode.incrementAndGet()

        val model = ActivityCallbackModel(callback, receiveCancel, destroyable)

        mActivityCallback[requestCode] = model
        return requestCode
    }

    fun callback(tag: String, result: Any): Boolean {
        try {
            val pair = mCallback[tag] ?: return false
            val function = pair.first
            val destroyable = pair.second

            if (destroyable == null ||
                    (destroyable is IDestroable && destroyable.hasDestroyed) ||
                    (destroyable is Activity && destroyable.isFinishing)
            ) {
                return true
            }

            val reflect1Class = function.reflect1Class() ?: return false

            if (reflect1Class == result::class) {
                function(result)
                return true
            }
        } catch (t: Throwable) {
            throwOnDebug(PageCallbackException())
            t.printStackTrace()
        }

        return false
    }

    fun callback(requestCode: Int, result: Intent): Boolean {
        try {
            val model = mActivityCallback[requestCode] ?: return false
            mActivityCallback.remove(requestCode)

            if (result.resultCode == Activity.RESULT_CANCELED && !model.receiveCancel) {
                return false
            }

            val function = model.callback ?: return false

            //这里会尝试去拿当前lambda的外部引用类，如果获取到了，则检查他的状态是否可用，如果不可用则直接返回
            if (model.destroyable?.hasDestroyed == true) {
                return true
            }

            function(result)
            return true
        } catch (t: Throwable) {
            throwOnDebug(PageCallbackException())
            t.printStackTrace()
        }

        return false
    }

    fun match(requestCode: Int): Boolean {
        try {
            mActivityCallback[requestCode] ?: return false
            return true
        } catch (t: Throwable) {
            throwOnDebug(PageCallbackException())
            t.printStackTrace()
        }
        return false
    }

    fun remove(tag: String) {
        mCallback.remove(tag)
    }

    fun remove(intent: PageIntent) {
        mCallbackCache.remove(intent)
    }


    private data class ActivityCallbackModel(val callback: OnPageResultCallback<Intent>?, val receiveCancel: Boolean, val destroyable: IDestroable?)
}