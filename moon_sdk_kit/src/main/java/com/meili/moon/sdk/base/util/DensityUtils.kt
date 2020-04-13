package com.meili.moon.sdk.base.util

import com.meili.moon.sdk.CommonSdk as Sdk

/**
 * Created by imuto on 2018/5/22.
 */
/**获取一个int类型的px对应的dp*/
val Int.dp: Int
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return (this / scale + 0.5F).toInt()
    }

/**获取一个int类型的dp对应的px*/
val Int.px: Int
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return (this * scale + 0.5F).toInt()
    }

/**获取一个int类型的px对应的sp*/
val Int.sp: Int
    get() {
        val fontScale = Sdk.app().resources.displayMetrics.scaledDensity
        return (this * fontScale + 0.5F).toInt()
    }
/**String 千分位加，*/
val String.thousands: String
    get() {
        var sb=StringBuilder(this)
        sb.insert(sb.length-6,",")
        return sb.toString()
    }


val Int.dpF: Float
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return this / scale + 0.5F
    }

/**获取一个int类型的dp对应的px*/
val Int.pxF: Float
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return this * scale + 0.5F
    }

/**获取一个int类型的px对应的sp*/
val Int.spF: Float
    get() {
        val fontScale = Sdk.app().resources.displayMetrics.scaledDensity
        return this * fontScale + 0.5F
    }

/**获取一个int类型的px对应的dp*/
val Float.dp: Int
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return (this / scale + 0.5F).toInt()
    }

/**获取一个int类型的dp对应的px*/
val Float.px: Float
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return this * scale + 0.5F
    }

/**获取一个int类型的px对应的sp*/
val Float.sp: Int
    get() {
        val fontScale = Sdk.app().resources.displayMetrics.scaledDensity
        return (this * fontScale + 0.5F).toInt()
    }

val Float.dpF: Float
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return this / scale + 0.5F
    }

/**获取一个int类型的dp对应的px*/
val Float.pxF: Float
    get() {
        val scale = Sdk.app().resources.displayMetrics.density
        return this * scale + 0.5F
    }

/**获取一个int类型的px对应的sp*/
val Float.spF: Float
    get() {
        val fontScale = Sdk.app().resources.displayMetrics.scaledDensity
        return this * fontScale + 0.5F
    }
