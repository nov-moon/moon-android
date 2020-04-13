@file:JvmName("SdkFragmentExtra")

package com.meili.moon.sdk.page.internal.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.AttrRes
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.StyleRes
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.meili.moon.sdk.CommonSdk
import com.meili.moon.sdk.base.util.dpF
import com.meili.moon.sdk.base.util.px
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.page.*
import com.meili.moon.sdk.page.annotation.Layout
import com.meili.moon.sdk.page.internal.PageManagerImpl
import com.meili.moon.sdk.page.internal.SdkFragment
import com.meili.moon.sdk.page.titlebar.ITitleBarView
import com.meili.moon.sdk.page.titlebar.Menu
import com.meili.moon.sdk.page.titlebar.MenuIcon
import com.meili.moon.sdk.page.titlebar.MenuText
import com.meili.moon.sdk.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

/**
 * Created by imuto on 2019-06-19.
 */

//为了扩展PageIntent做的缓存
private val onResultCache: MutableMap<Intent, OnPageResultCallback<Any>> = mutableMapOf()

/**
 * 跳转到指定页面
 *
 * 使用举例：
 *
 * ``` kotlin
 * val arguments = Bundle()
 * arguments.putIntExtra("id", id)
 *
 * gotoPage("order/detail", bundle)
 *
 * ```
 *
 * [pageName] 跳转的页面名称，可以通过PageDefine的方式或者注解的方式进行添加和查询
 *
 * [bundle] 给下个页面的参数
 *
 * 这个方法之所以没有放到SdkFragmentExtra_.kt中，是因为已经有很多页面在使用此方法。如果更换位置，需要更换大量引用
 *
 * 更多打开页面方法，请参考：[SdkFragmentExtra_.kt]
 */
fun IDestroable.gotoPage(pageName: String, bundle: Bundle? = null) {
    val pageIntent = PageIntent(pageName)
    if (bundle != null) {
        pageIntent.putExtras(bundle)
    }

    Rainbow.gotoPage(pageIntent)
}

fun Activity.gotoPage(pageName: String, bundle: Bundle? = null) {
    val pageIntent = PageIntent(pageName)
    if (bundle != null) {
        pageIntent.putExtras(bundle)
    }

    Rainbow.gotoPage(pageIntent)
}

/**
 * 打开指定页面，并可以做参数传递，回调结果等
 *
 * 使用举例：
 *
 * ``` kotlin
 * gotoPage("pageName") {
 *      // 添加参数
 *      putExtra("param1", 1)
 *      putExtra("param2", "value")
 *
 *      // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
 *      onResult<String> {
 *          //处理结果
 *      }
 * }
 * ```
 */
fun IDestroable.gotoPage(pageName: String, initCallback: PageIntent.() -> Unit) {
    val pageIntent = PageIntent(pageName)
    pageIntent.initCallback()

    val onResult = onResultCache[pageIntent]
    onResultCache.remove(pageIntent)

    PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = onResult)
}

fun Activity.gotoPage(pageName: String, initCallback: PageIntent.() -> Unit) {
    val pageIntent = PageIntent(pageName)
    pageIntent.initCallback()

    val onResult = onResultCache[pageIntent]
    onResultCache.remove(pageIntent)

    PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = onResult)
}

/**
 * 打开指定页面，并可以做参数传递，回调结果等
 *
 * 如果你已经有参数集，或者完全不需要传递参数，只关心结果，可以使用此方法
 *
 * 使用举例：
 *
 * ``` kotlin
 * // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
 * gotoPage<String>("pageName") {
 *      //结果处理
 * }
 *
 * val bundle = Bundle()
 * bundle.putIntExtra("params", 1)
 *
 * // 得到回调结果，泛型必须和下个页面返回的参数类型一致，如果不关注结果类型，可设置为Any
 * gotoPage<String>("pageName", bundle) {
 *      //结果处理
 * }
 * ```
 */
fun <T : Any> IDestroable.gotoPage(pageName: String, bundle: Bundle? = null, invoker: OnPageResultCallback<in T>) {
    val pageIntent = PageIntent(pageName)

    if (bundle != null) {
        pageIntent.putExtras(bundle)
    }

    PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = invoker)
}

fun <T : Any> Activity.gotoPage(pageName: String, bundle: Bundle? = null, invoker: OnPageResultCallback<in T>) {
    val pageIntent = PageIntent(pageName)

    if (bundle != null) {
        pageIntent.putExtras(bundle)
    }

    PageManagerImpl.gotoPage(pageIntent, destroyable = this, pageCallback = invoker)
}

/**
 * 打开activity，如果需要请求结果的情况下，返回结果在intent中。
 *
 * 为了方便定制，在Intent中新增了如下扩展：
 *
 * [Intent.resultCode] 当前结果的结果状态，对应activityResult中的resultCode
 *
 * [Intent.receiveCancelResult] 当前请求是否处理cancel情况，默认不处理，一般情况在用户直接返回时会触发cancel的结果回调
 *
 * 入参解释：
 *
 * [intent] 打开页面的配置参数
 *
 * [activityCallback] 如果此参数不为null，则打开方式为startActivityForResult方式
 *
 * 使用举例：
 *
 * ``` kotlin
 * gotoActivity(FirstActivity::class) {
 *      //设置传参
 *      putExtra("param1", 1)
 *
 *      //是否接收Cancel回调，默认不接收
 *      receiveCancelResult = true
 *
 *      //得到回调
 *      onResult {
 *          //回调结果的resultCode字段，一般对应Activity.RESULT_OK等
 *          it.resultCode
 *      }
 * }
 * ```
 */
fun SdkFragment.gotoActivity(clazz: KClass<out Activity>?, initCallback: (Intent.() -> Unit)? = null) {
    val context = activity ?: return
    context.gotoActivity(clazz, this, initCallback)
}

fun Context.gotoActivity(clazz: KClass<out Activity>?, initCallback: (Intent.() -> Unit)? = null) {
    gotoActivity(clazz, this.toT(), initCallback)
}

fun Context.gotoActivity(clazz: KClass<out Activity>?, destroyable: IDestroable?, initCallback: (Intent.() -> Unit)? = null) {
    val pageIntent = Intent()

    if (clazz != null) {
        pageIntent.setClass(this, clazz.java)
    }
    if (initCallback != null) {
        pageIntent.initCallback()
    }

    val onResult = onResultCache[pageIntent]
    onResultCache.remove(pageIntent)

    PageManagerImpl.gotoActivity(pageIntent, destroyable, onResult)
}

/**
 * 打开activity，如果需要请求结果的情况下，返回结果在intent中。
 *
 * 当已经有参数集，或者完全不用传递参数，并且需要获取结果，则此方法使用更简单一些
 *
 * Intent在此起作用的扩展：
 *
 * 在打开页面的intent上设置：[Intent.receiveCancelResult] 当前请求是否处理cancel情况，默认不处理，一般情况在用户直接返回时会触发cancel的结果回调
 *
 * 在接收lambda的结果intent上获取：[Intent.resultCode] 获取当前结果的结果状态，对应activityResult中的resultCode
 *
 * 入参解释：
 *
 * [intent] 打开页面的配置参数
 *
 * [invoker] 如果此参数不为null，则打开方式为startActivityForResult方式
 *
 * 使用举例：
 *
 * ``` kotlin
 * gotoActivityForResult(FirstActivity::class) {
 *     //回调结果的resultCode字段，一般对应Activity.RESULT_OK等
 *     it.resultCode
 * }
 * ```
 */
fun SdkFragment.gotoActivityForResult(clazz: KClass<out Activity>?, intent: Intent? = null, invoker: OnPageResultCallback<Intent>? = null) {
    val context = activity ?: return
    context.gotoActivityForResult(clazz, intent, this, invoker)
}

fun Context.gotoActivityForResult(clazz: KClass<out Activity>?, intent: Intent? = null, destroyable: IDestroable?, invoker: OnPageResultCallback<Intent>? = null) {
    val pageIntent = Intent()

    if (clazz != null) {
        pageIntent.setClass(this, clazz.java)
    }

    if (intent != null) {
        pageIntent.putExtras(intent)
    }

    PageManagerImpl.gotoActivity(pageIntent, destroyable, invoker)
}

/**
 * 生命周期函数是否是有效的，如果是无效的，则不应该使用声明周期回调
 */
var SdkFragment.isDirty: Boolean
    get() = pageIntent.getBooleanExtra("isCancelLifeCircle_SdkFragment", false)
    set(value) {
        pageIntent.putExtra("isCancelLifeCircle_SdkFragment", value)
    }

/**
 * 专门用来做页面的回调结果扩展，不用做其他。
 *
 * 此处的泛型必须和打开页面的设置结果类型一致，否则得不到回调。
 * 例如A->B，在B页面setResult(Int)，则这里的泛型必须为[Int]。
 *
 * 如果你只关注接收结果，并不关注接收的数据类型，那么你可以将此泛型设置为[Any]类型
 *
 * 只有当打开的页面调用了setResult方法后，此回调才能接到结果
 *
 */
fun <T : Any> PageIntent.onResult(result: OnPageResultCallback<T>) {
    onResultCache[this] = result as OnPageResultCallback<Any>
}

fun Intent.onResult(result: OnPageResultCallback<Intent>) {
    onResultCache[this] = result as OnPageResultCallback<Any>
}

internal fun View.fixContentBackground(): View {
    // fixme 这里的值从defStyleAttr拿不出来
    val typedArray = CommonSdk.app().theme
            .obtainStyledAttributes(null, R.styleable.RainbowStyle, 0, 0)
    setBackgroundColor(
            typedArray.getColor(R.styleable.RainbowStyle_rainbowPageBackgroundColor,
                    CommonSdk.app().resources.getColor(R.color.defRainbowPageBackground)))
    typedArray.recycle()
    return this
}

internal fun RainbowFragment.injectLayout(viewGroup: ViewGroup?): View? {
    val layoutAnnotation = this::class.findAnnotation<Layout>() ?: return null
    return pageActivity.inflating(layoutAnnotation.value, viewGroup, false)
}

/**
 * 显示一个notification，默认2秒关闭
 *
 * 如果设置 关闭时间 <= 0，则认为不自动关闭，页面通知会常驻顶部，直到调用dissmiss方法。
 * 当常驻显示并且设置了点击事件，则会自动显示右边的箭头，否则自动隐藏，除非单独设置
 */
fun RainbowFragment.showNotification(msg: CharSequence, delayDismissSecond: Int = 2,
                                     onClickCallback: OnClickCallback = null) {
    fixNotificationNormal(onClickCallback)
    mNotification?.show(msg, delayDismissSecond)
}

/**
 * 显示一个失败状态的notification，默认2秒关闭
 *
 * 如果设置 关闭时间 <= 0，则认为不自动关闭，页面通知会常驻顶部，直到调用dissmiss方法。
 * 当常驻显示并且设置了点击事件，则会自动显示右边的箭头，否则自动隐藏，除非单独设置
 */
fun RainbowFragment.showNotificationFailed(msg: CharSequence, delayDismissSecond: Int = 2,
                                           onClickCallback: OnClickCallback = null) {
    fixNotificationFailed(onClickCallback)
    mNotification?.show(msg, delayDismissSecond)
}

/**
 * 显示一个成功状态的notification，默认2秒关闭
 *
 * 如果设置 关闭时间 <= 0，则认为不自动关闭，页面通知会常驻顶部，直到调用dissmiss方法。
 * 当常驻显示并且设置了点击事件，则会自动显示右边的箭头，否则自动隐藏，除非单独设置
 */
fun RainbowFragment.showNotificationSuccess(msg: CharSequence, delayDismissSecond: Int = 2,
                                            onClickCallback: OnClickCallback = null) {
    fixNotificationSuccess(onClickCallback)
    mNotification?.show(msg, delayDismissSecond)
}

/**
 * 手动关闭页面通知，默认页面通知展示2秒会自动关闭
 */
fun RainbowFragment.dissmissNotification() {
    mNotification?.dismiss()
}

internal fun RainbowFragment.fixNotificationNormal(onClickCallback: OnClickCallback = null) {
    if (notificationState == 0) {
        return
    }
    notificationState = 0
    fixNotificationStyle(R.attr.moonRainbowNotificationNormal, R.style.rainbowNotificationNormal, onClickCallback)
}

internal fun RainbowFragment.fixNotificationFailed(onClickCallback: OnClickCallback = null) {
    if (notificationState == -1) {
        return
    }
    notificationState = -1
    fixNotificationStyle(R.attr.moonRainbowNotificationFailed, R.style.rainbowNotificationFailed, onClickCallback)
}

internal fun RainbowFragment.fixNotificationSuccess(onClickCallback: OnClickCallback = null) {
    if (notificationState == 1) {
        return
    }
    notificationState = 1
    fixNotificationStyle(R.attr.moonRainbowNotificationSuccess, R.style.rainbowNotificationSuccess, onClickCallback)
}

private var RainbowFragment.notificationState: Int
    get() {
        return pageIntent.getIntExtra("moonNotificationState_", -100)
    }
    set(value) {
        pageIntent.putExtra("moonNotificationState_", value)
    }

private fun RainbowFragment.fixNotificationStyle(@AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int, onClickCallback: OnClickCallback = null) {
    mNotification?.setOnNotificationClickListener { onClickCallback?.invoke(it) }

    pageActivity.theme.obtainStyledAttributes(null,
            R.styleable.RainbowPageNotification, defStyleAttr, defStyleRes).apply {
        val color = getColor(R.styleable.RainbowPageNotification_rainbowPageNotificationTextColor,
                Color.parseColor("#385590"))
        val size = getDimension(R.styleable.RainbowPageNotification_rainbowPageNotificationTextSize,
                14F)
        val height = getDimensionPixelSize(R.styleable.RainbowPageNotification_rainbowPageNotificationHeight,
                -1)
        val backgroundColor = getColor(R.styleable.RainbowPageNotification_rainbowPageNotificationBackgroundColor,
                Color.parseColor("#48385590"))
        val arrow = getResourceId(R.styleable.RainbowPageNotification_rainbowPageNotificationArrow, 0)
        val icon = getResourceId(R.styleable.RainbowPageNotification_rainbowPageNotificationIcon, 0)

        mNotification?.setMessageTextSize(size.dpF)
        mNotification?.setMessageColor(color)
        mNotification?.setNotificationBackgroundColor(backgroundColor)
        if (icon == 0) {
            mNotification?.setIconVisibility(View.GONE)
        } else {
            mNotification?.setIcon(icon)
        }
        if (arrow == 0 || onClickCallback == null) {
            mNotification?.setArrowVisibility(View.GONE)
        } else {
            mNotification?.setArrow(arrow)
        }
        if (height >= 0) {
            mNotification?.layoutParams?.height = height
        }
    }.recycle()
}

/** 为沉浸式修改top部分的View的Padding，如果不支持沉浸式，则不修改，只调用一次，例如在onCreate中调用  */
fun RainbowFragment.fixTranslucentStatusBarTopPadding(view: View) {
    if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
        view.setPadding(view.paddingLeft, view.paddingTop + statusBarHeight, view.paddingRight, view.paddingBottom)
    }
}

/**
 * 设置标题是否为透明，如果有的话
 */
fun RainbowFragment.setTitleTransparentStyle(transparentStyle: Boolean) {
    if (transparentStyle) {
        setStyle(R.style.rainbowTitlebarTransparentTheme)
    } else {
        setStyle(0)
    }
}

/**设置页面view距离titleBar的高度，默认为0，只有[isUseDefaultTitleBar]为true时才可用*/
fun RainbowFragment.setContentMarginTitleBarDp(topMargin: Float) {
    if (!isUseDefaultTitleBar()) return
    if (topMargin < 0) {
        mViewParent.bringToFront()
    }
    (mViewParent.layoutParams as RelativeLayout.LayoutParams).topMargin = topMargin.px.toInt()
}

/**设置通知距离顶部的margin*/
fun RainbowFragment.setNotificationMarginTopPx(px: Int) {
    (mNotification?.layoutParams as RelativeLayout.LayoutParams).topMargin = px
}


/**
 * 添加TextMenu,如果要设置字体颜色等,需要使用返回的MenuText进行设置
 */
fun RainbowFragment.addMenuText(text: CharSequence, @ColorRes colorResId: Int = 0, listener: View.OnClickListener): MenuText? {
    if (isFinishing()) {
        return null
    }
    val menuText = MenuText(pageActivity)
    menuText.setText(text)
    menuText.setTextColor(getColor(colorResId))
    menuText.setOnClickListener(listener)
    addMenu(menuText)
    return menuText
}

/**
 * 添加TextMenu,如果要设置字体颜色等,需要使用返回的MenuText进行设置
 */
fun RainbowFragment.addMenuText(text: CharSequence, @ColorRes colorResId: Int = 0, listener: OnClickCallback): MenuText? {
    if (isFinishing()) {
        return null
    }

    return addMenuText(text, colorResId, View.OnClickListener { listener?.invoke(it) })
}

/**添加一个icon类型的menu*/
fun RainbowFragment.addMenuIcon(@DrawableRes drawable: Int, listener: OnClickCallback): MenuIcon? {
    if (isFinishing()) {
        return null
    }
    return addMenuIcon(drawable, View.OnClickListener { listener?.invoke(it) })
}

/**添加一个icon类型的menu*/
fun RainbowFragment.addMenuIcon(@DrawableRes drawable: Int, listener: View.OnClickListener): MenuIcon? {
    if (isFinishing()) {
        return null
    }
    val menuIcon = MenuIcon(pageActivity)
    menuIcon.setOnClickListener(listener)
    menuIcon.setImageResource(drawable)
    addMenu(menuIcon)
    return menuIcon
}

class TitleBarDelegate : ITitleBarView {

    var realTitleBar: ITitleBarView? = null

    override var titleBarVisibility: Int
        get() = realTitleBar?.titleBarVisibility ?: View.GONE
        set(value) {
            realTitleBar?.titleBarVisibility = value
        }

    override fun setTitle(text: CharSequence) {
        realTitleBar?.setTitle(text)
    }

    override fun setTitle(resId: Int) {
        realTitleBar?.setTitle(resId)
    }

    override fun setCloseMenuVisible(visible: Int) {
        realTitleBar?.setCloseMenuVisible(visible)
    }

    override fun setTitleTextColor(color: Int) {
        realTitleBar?.setTitleTextColor(color)
    }

    override fun setBackIcon(resId: Int) {
        realTitleBar?.setBackIcon(resId)
    }

    override fun setBackIcon(drawable: Drawable) {
        realTitleBar?.setBackIcon(drawable)
    }

    override fun setBackIconVisible(visible: Int) {
        realTitleBar?.setBackIconVisible(visible)
    }

    override fun setBackText(resId: Int) {
        realTitleBar?.setBackText(resId)
    }

    override fun setBackText(text: CharSequence) {
        realTitleBar?.setBackText(text)
    }

    override fun setBackTextColor(resId: Int) {
        realTitleBar?.setBackTextColor(resId)
    }

    override fun setTitleBarBackgroundColor(color: Int) {
        realTitleBar?.setTitleBarBackgroundColor(color)
    }

    override fun setTitleBarBackgroundDrawable(resId: Int) {
        realTitleBar?.setTitleBarBackgroundDrawable(resId)
    }

    override fun addMenu(menu: Menu) {
        realTitleBar?.addMenu(menu)
    }

    override fun removeMenu(menu: Menu) {
        realTitleBar?.removeMenu(menu)
    }

    /** 设置是否支持无网络连接的View  */
    override fun setSupportNoNetworkStyle(isSupport: Boolean) {
        realTitleBar?.setSupportNoNetworkStyle(isSupport)
    }

    /** 设置样式  */
    override fun setStyle(style: Int) {
        realTitleBar?.setStyle(style)
    }
}