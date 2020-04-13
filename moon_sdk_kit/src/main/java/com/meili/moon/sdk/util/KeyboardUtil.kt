@file:JvmName("KeyboardUtil")

package com.meili.moon.sdk.util

import android.app.Activity
import android.content.Context
import android.support.v4.app.Fragment
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import com.meili.moon.sdk.log.LogUtil


fun Fragment.hideKeyboard() {
    val act = activity ?: return
    act.hideKeyboard()
}

fun Activity.hideKeyboard() {
    try {
        val v = window.decorView ?: return
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(v.windowToken, 0)
    } catch (ex: Throwable) {
        LogUtil.e(ex, ex.message)
    }
}

fun showKeyboard(view: View?) {
    view ?: return
    try {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED)
    } catch (ex: Throwable) {
        LogUtil.e(ex, ex.message)
    }
}

fun View.hideKeyboard() {
    try {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    } catch (ex: Throwable) {
        LogUtil.e(ex, ex.message)
    }
}

fun Fragment.setSoftInputModePan() {
    val act = activity ?: return
    act.setSoftInputModePan()
}

fun Activity.setSoftInputModePan() {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}

fun Fragment.setSoftInputModeResize() {
    val act = activity ?: return
    act.setSoftInputModeResize()
}

fun Activity.setSoftInputModeResize() {
    window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
}

fun Fragment.isKeyboardShowing(): Boolean {
    return activity?.isKeyboardShowing() ?: false
}

fun Activity.isKeyboardShowing(): Boolean {
    return window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED
}