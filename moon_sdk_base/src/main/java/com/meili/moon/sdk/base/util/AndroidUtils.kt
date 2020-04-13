package com.meili.moon.sdk.base.util

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import android.support.annotation.AttrRes
import android.support.annotation.LayoutRes
import android.support.annotation.StyleRes
import android.support.annotation.StyleableRes
import android.support.v4.app.Fragment
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.text.method.ReplacementTransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.TextView
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.base.R
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.common.UEHttpHolder
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.common.StartLambda
import com.meili.moon.sdk.common.SuccessLambda
import com.meili.moon.sdk.log.LogUtil
import com.meili.moon.sdk.util.isEmpty


const val SCHEMA_SYSTEM = "http://schemas.android.com/apk/res/android"
const val SCHEMA_APP = "http://schemas.android.com/apk/res-auto"
const val SCHEMA_TOOLS = "http://schemas.android.com/tools"

const val MATCH_PARENT = ViewGroup.LayoutParams.MATCH_PARENT
const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT

/**动画回调*/
typealias AnimatorLambda = ((Animator?) -> Unit)?

/**view的onClick回调*/
typealias OnClickCallback = SuccessLambda<View>

/**一个通用的callback，不带参数*/
typealias OnNormalCallback = StartLambda

/**
 * Created by imuto on 2018/5/22.
 */
/**
 * 获取画笔画的text基于[textCenterY]作为y轴中心点的baseline的y坐标，可用来作为画text的y坐标
 *
 * [textCenterY] 想要text显示的中心点y坐标，比如一个view的正中心，则返回值的坐标为text画的y坐标
 * */
fun Paint.getTextBaseline(textCenterY: Int): Int {
    return textCenterY + getTextBaseline2Center()
}

fun Paint.getTextBaselineByTop(startY: Int): Float {
    return startY.toFloat() + Math.abs(fontMetrics.ascent)
}

fun Paint.getTextBaselineByBottom(bottomY: Int): Float {
    return bottomY.toFloat() - Math.abs(fontMetrics.descent)
}

/**
 * 获取text的baseline到字体高度中心的距离，返回值为正值
 */
fun Paint.getTextBaseline2Center(): Int {
    val font = fontMetricsInt
    val top = Math.abs(font.top)
    val halfHeight = (top + font.bottom) / 2
    return top - halfHeight
}

/**根据schema获取指定名称的resourceValue，默认使用Android的schema*/
fun AttributeSet.schemaResValue(name: String, defValue: Int = 0, schema: String = SCHEMA_SYSTEM): Int {
    return getAttributeResourceValue(schema, name, defValue)
}

/**根据schema获取指定名称的float值，默认使用Android的schema*/
fun AttributeSet.schemaFloatValue(name: String, defValue: Float = 0.0F, schema: String = SCHEMA_SYSTEM): Float {
    return getAttributeFloatValue(schema, name, defValue)
}

/**根据schema获取指定名称的Int，默认使用Android的schema*/
fun AttributeSet.schemaIntValue(name: String, defValue: Int = 0, schema: String = SCHEMA_SYSTEM): Int {
    return getAttributeIntValue(schema, name, defValue)
}

/**根据schema获取指定名称的boolean，默认使用Android的schema*/
fun AttributeSet.schemaBooleanValue(name: String, defValue: Boolean = false, schema: String = SCHEMA_SYSTEM): Boolean {
    return getAttributeBooleanValue(schema, name, defValue)
}

/**根据schema获取指定名称的String，默认使用Android的schema*/
fun AttributeSet.schemaStringValue(name: String, schema: String = SCHEMA_SYSTEM): String {
    return getAttributeValue(schema, name)
}

/**初始化自定义view中的属性，会自动回收当前的TypedArray*/
fun View.obtainAttr(set: AttributeSet?,
                    @StyleableRes attrs: IntArray,
                    @AttrRes defStyleAttr: Int = 0,
                    @StyleRes defStyleRes: Int = 0,
                    block: TypedArray.() -> Unit) {
    set.obtainAttr(attrs, defStyleAttr, defStyleRes, block)
}

/**初始化自定义view中的属性，会自动回收当前的TypedArray*/
fun AttributeSet?.obtainAttr(@StyleableRes attrs: IntArray,
                             @AttrRes defStyleAttr: Int = 0,
                             @StyleRes defStyleRes: Int = 0,
                             block: TypedArray.() -> Unit) {
    val typedArray = app().obtainStyledAttributes(this, attrs, defStyleAttr, defStyleRes)
    typedArray.block()
    typedArray.recycle()
}

/**初始化自定义view中的属性，并返回TypedArray*/
fun View?.obtainAttrInstance(set: AttributeSet?,
                             @StyleableRes attrs: IntArray,
                             @AttrRes defStyleAttr: Int = 0,
                             @StyleRes defStyleRes: Int = 0,
                             block: (TypedArray.() -> Unit)? = null): TypedArray {
    return set.obtainAttrInstance(attrs, defStyleAttr, defStyleRes, block)
}

/**初始化自定义view中的属性，并返回TypedArray*/
fun AttributeSet?.obtainAttrInstance(@StyleableRes attrs: IntArray,
                                     @AttrRes defStyleAttr: Int = 0,
                                     @StyleRes defStyleRes: Int = 0,
                                     block: (TypedArray.() -> Unit)? = null): TypedArray {
    val typedArray = app().obtainStyledAttributes(this, attrs, defStyleAttr, defStyleRes)
    block?.invoke(typedArray)
    return typedArray
}

fun app() = Sdk.app()

/**简化版设置动画监听*/
fun Animator.addOnAnimListener(onStart: AnimatorLambda = null,
                               onRepeat: AnimatorLambda = null,
                               onCancel: AnimatorLambda = null,
                               onEnd: AnimatorLambda = null) {
    this.addListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator?) {
            onRepeat?.invoke(animation)
        }

        override fun onAnimationEnd(animation: Animator?) {
            onEnd?.invoke(animation)
        }

        override fun onAnimationCancel(animation: Animator?) {
            onCancel?.invoke(animation)
        }

        override fun onAnimationStart(animation: Animator?) {
            onStart?.invoke(animation)
        }
    })
}

fun TextView.addTextChangeListener(
        beforeTextChanged: ((s: CharSequence?, start: Int, count: Int, after: Int) -> Unit)? = null,
        onTextChanged: ((s: CharSequence?, start: Int, before: Int, count: Int) -> Unit)? = null,
        afterTextChanged: ((e: Editable?) -> Unit)? = null
) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged?.invoke(s)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged?.invoke(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged?.invoke(s, start, before, count)
        }
    })
}

fun Fragment.getColor(colorId: Int): Int {
    return context?.resources?.getColor(colorId) ?: 0
}

fun Activity.getColor(colorId: Int): Int {
    return resources.getColor(colorId)
}

fun View.getColor(colorId: Int): Int {
    return context.resources.getColor(colorId)
}

fun View.getColorList(colorId: Int): ColorStateList? {
    return context.resources.getColorStateList(colorId)
}

fun View.getString(stringId: Int): String {
    return context.resources.getString(stringId)
}

fun View.getString(stringId: Int, vararg formatArgs: Any): String {
    return context.resources.getString(stringId, formatArgs)
}

fun Fragment.getColorList(colorId: Int): ColorStateList? {
    return context?.resources?.getColorStateList(colorId)
}

fun Activity.getColorList(colorId: Int): ColorStateList? {
    return resources.getColorStateList(colorId)
}

fun Fragment.getDrawable(drawableId: Int): Drawable? {
    return context?.resources?.getDrawable(drawableId)
}

fun Activity.getDrawable(drawableId: Int): Drawable? {
    return resources?.getDrawable(drawableId)
}

fun View.getDrawable(drawableId: Int): Drawable? {
    return context?.resources?.getDrawable(drawableId)
}

fun inflating(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = true): View {
    return inflateNullable(layoutId, root, attach)!!
}

fun inflateNullable(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = false): View? {
    return LayoutInflater.from(Sdk.app()).inflate(layoutId, root, attach)
}

val screenWidth: Int by lazy {
    val width = CommonSdk.app().resources.displayMetrics.widthPixels
    val height = CommonSdk.app().resources.displayMetrics.heightPixels
    return@lazy Math.min(width, height)
}

val screenHeight: Int by lazy {
    val width = CommonSdk.app().resources.displayMetrics.widthPixels
    val height = CommonSdk.app().resources.displayMetrics.heightPixels
    return@lazy Math.max(width, height)
}

val statusBarHeight: Int by lazy {
    val resources = Sdk.app().resources
    val statusBarId = resources.getIdentifier("status_bar_height", "dimen", "android")
    var statusBarHeight = 0
    if (statusBarId > 0) {
        statusBarHeight = resources.getDimension(statusBarId).toInt()
    }
    return@lazy statusBarHeight
}


/**复制到粘贴板*/
fun copy2Clipboard(info: CharSequence) {
    Utils.copy2Clipboard(Sdk.app(), info)
}

//@Suppress("UNCHECKED_CAST")
//        /**将[target]强转为T*/
//fun <T> toT(target: Any?): T? = target as? T

@Suppress("UNCHECKED_CAST")
/**将[target]强转为T*/
inline fun <reified T> Any?.toT(): T? {
    return if (this is T) {
        this
    } else null
}

@Suppress("UNCHECKED_CAST")
        /**将[target]强转为T*/
fun <T> Any?.toType(): T? {
    return try {
        this as? T
    } catch (thr: Throwable) {
        null
    }
}

fun Fragment.openAppSetting() {
    context?.openAppSetting()
}

fun View.openAppSetting() {
    context.openAppSetting()
}

fun Context.openAppSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

/**当前运行环境是否是debug*/
val isDebug
    get() = Sdk.environment().isDebug()

fun post(delayed: Long = 0, runnable: () -> Unit) {
    Sdk.task().post(delayed, runnable)
}

//fun IDestroable.post(delayed: Long = 0, runnable: () -> Unit) {
//    if (hasDestroyed) {
//        return
//    }
//    Sdk.task().post(delayed, runnable)
//}

fun hasNull(vararg args: Any?): Any? {
    if (isEmpty(args)) return null
    if (args.contains(null)) return null
    return args[0]
}

fun TextView.autoUpperCase(useEnglishKeyboard: Boolean = true) {
    transformationMethod = A2bigA()
    if (useEnglishKeyboard) {
        keyListener = DigitsKeyListener.getInstance("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
    }
}

/**view是否显示，必须满足父类显示状态都为[View.VISIBLE]和当前view在view树结构中*/
fun View?.hasShown(): Boolean {
    if (this == null || !isShown) {
        return false
    }
    var target: ViewParent? = this.parent

    while (target != null) {
        if (target !is View){
            return true
        }

        target = (target as ViewParent).parent

    }
    return false
}

class A2bigA : ReplacementTransformationMethod() {
    override fun getOriginal(): CharArray {
        return charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
    }

    override fun getReplacement(): CharArray {
        return charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
    }
}

private val onClickListenerFilter = OnClickListenerFilter()

fun View.onClick(holder: Any? = null, lis: ((View) -> Unit)?) {
    setTag(R.id.moonSdkBaseOnClickListenerFilter, lis)
    setTag(R.id.moonSdkBaseOnClickListenerFilterHolder, holder)
    setOnClickListener(OnClickListenerFilter())
}

fun View.onClick(lis: ((View) -> Unit)?) {
    onClick(null, lis)
}

fun isNotificationsEnabled(context: Context?):Boolean{
    val notificationManagerCompat = NotificationManagerCompat.from(context!!)
    return notificationManagerCompat.areNotificationsEnabled()

}


/**
 * @Description: 跳转应用通知设置页面
 * @Author: xiaoyu
 * @Date: 2019/2/20
 */
fun goToAppNotification(context: Context){
    val action = "android.settings.APP_NOTIFICATION_SETTINGS"
    val intent = Intent()

    when(Build.VERSION.SDK_INT){
        in 26..100 ->{
            intent.action = action
            intent.putExtra("android.provider.extra.APP_PACKAGE", context.applicationContext.packageName)
        }

        in 21..25 ->{
            // android 5.0-7.0
            intent.action = action
            intent.putExtra("app_package", context.applicationContext.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
        } else -> {
        // 其他
        intent.action = action
        intent.data = (Uri.fromParts("package", context.applicationContext.packageName, null))
    }


    }

    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    ContextCompat.startActivity(context, intent, null)
}

private var lastClickTime = 0L

internal fun View.dp(value: Int): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value.toFloat(), resources.displayMetrics)
}

class OnClickListenerFilter : View.OnClickListener {

    override fun onClick(v: View?) {
        if (v == null) return
        val uptimeMillis = SystemClock.uptimeMillis()
        if (uptimeMillis - lastClickTime < 500) {
            LogUtil.e("点击太频繁")
            return
        }

        lastClickTime = uptimeMillis

        try {
            val holder = v.getTag(R.id.moonSdkBaseOnClickListenerFilterHolder) ?: null
            if (holder != null) {
                if (holder is IDestroable && holder.hasDestroyed) {
                    return
                }
                if (holder is UEHttpHolder && !holder.isUEnable) {
                    return
                }
            }

            val function = v.getTag(R.id.moonSdkBaseOnClickListenerFilter).toT<Function1<View, Unit>>()
            function?.invoke(v)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}