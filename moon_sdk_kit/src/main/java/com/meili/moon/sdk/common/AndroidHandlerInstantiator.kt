package com.meili.moon.sdk.common

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.meili.moon.sdk.task.IHandler

/**
 * Created by imuto on 17/12/15.
 */
class AndroidHandlerInstantiator : Instantiator<IHandler> {
    override fun newInstance(vararg var2: Any): IHandler {
        return InnerHandler()
    }

    class InnerHandler : IHandler {
        private val handler = WrapHandler(this)

        override fun sendMessage(what: Int, arg1: Int, arg2: Int, obj: Any?, delayMillis: Long) {
            handler.sendMessageDelayed(handler.obtainMessage(what, arg1, arg2, obj), delayMillis)
        }

        override fun handleMessage(what: Int, arg1: Int, arg2: Int, obj: Any?) {
        }

        override fun post(runnable: Runnable, delayMillis: Long) {
            handler.postDelayed(runnable, delayMillis)
        }

        override fun removeCallbacks(runnable: Runnable) {
            handler.removeCallbacks(runnable)
        }

        private inner class WrapHandler(val handler: IHandler, looper: Looper = Looper.getMainLooper()) : Handler(looper) {

            override fun handleMessage(msg: Message?) {
                handler.handleMessage(msg?.what ?: 0, msg?.arg1 ?: 0, msg?.arg2 ?: 0, msg?.obj ?: 0)
            }
        }
    }
}