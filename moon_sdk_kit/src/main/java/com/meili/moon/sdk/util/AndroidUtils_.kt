package com.meili.moon.sdk.util


import android.animation.Animator
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.support.annotation.AttrRes
import android.support.annotation.LayoutRes
import android.support.annotation.StyleRes
import android.support.annotation.StyleableRes
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.CommonSdk.app
import com.meili.moon.sdk.R
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.common.StartLambda
import com.meili.moon.sdk.common.SuccessLambda
import com.meili.moon.sdk.log.LogUtil


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

val app: Application
    get() = CommonSdk.app()

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

/**
 * 遍历所有的子View。
 *
 * 可以通过设置[isAllChild]来决定是否遍历当前view下的所有子view，如果碰到子view是ViewGroup会继续遍历子view的view。
 * 可通过设置[isOnlyViewGroup]来决定是否只回调子View是[ViewGroup]的情况，默认所有类型子view都会回调
 */
fun View.childrenForeach(isAllChild: Boolean = false, isOnlyViewGroup: Boolean = false, callback: (child: View) -> Unit) {
    if (this !is ViewGroup) {
        return
    }
    childCount.foreach {
        val childAt = getChildAt(it)
        if (isOnlyViewGroup) {
            if (childAt is ViewGroup) {
                callback(childAt)
            }
        } else {
            callback(childAt)
        }
        if (isAllChild) {
            childAt.childrenForeach(isAllChild, isOnlyViewGroup, callback)
        }
    }
}

fun Context.inflating(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = true): View {
    return inflateNullable(layoutId, root, attach)!!
}

fun Context.inflateNullable(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = false): View? {
    return LayoutInflater.from(this).inflate(layoutId, root, attach)
}

fun Fragment.inflating(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = true): View {
    return inflateNullable(layoutId, root, attach)!!
}

fun Fragment.inflateNullable(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = false): View? {
    return LayoutInflater.from(activity).inflate(layoutId, root, attach)
}

fun View.inflating(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = true): View {
    return inflateNullable(layoutId, root, attach)!!
}

fun View.inflateNullable(@LayoutRes layoutId: Int, root: ViewGroup? = null, attach: Boolean = false): View? {
    return LayoutInflater.from(context).inflate(layoutId, root, attach)
}

/**当前运行环境是否是debug*/
val isDebug
    get() = CommonSdk.environment().isDebug()

fun post(delayed: Long = 0, runnable: () -> Unit) {
    CommonSdk.task().post(delayed, runnable)
}

fun hasNull(vararg args: Any?): Any? {
    if (isEmpty(args)) return null
    if (args.contains(null)) return null
    return args[0]
}

private val onClickListenerFilter = OnClickListenerFilter()

fun View.onClick(holder: Any? = null, lis: ((View) -> Unit)?) {
    setTag(R.id.moonKitOnClickListenerFilter, lis)
    setTag(R.id.moonKitOnClickListenerFilterHolder, holder)
    setOnClickListener(OnClickListenerFilter())
}

private var lastClickTime = 0L

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
            val holder = v.getTag(R.id.moonKitOnClickListenerFilterHolder) ?: null
            if (holder != null) {
                if (holder is IDestroable && holder.hasDestroyed) {
                    return
                }
            }

            val function = v.getTag(R.id.moonKitOnClickListenerFilter).toT<Function1<View, Unit>>()
            function?.invoke(v)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}