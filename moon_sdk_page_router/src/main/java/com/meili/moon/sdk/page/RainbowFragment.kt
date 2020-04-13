package com.meili.moon.sdk.page

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.annotation.ColorInt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.meili.moon.sdk.page.internal.SdkFragment
import com.meili.moon.sdk.page.internal.utils.*
import com.meili.moon.sdk.page.pagestate.PageStatesView
import com.meili.moon.sdk.page.titlebar.ITitleBarView
import com.meili.moon.sdk.util.*
import com.meili.moon.ui.dialog.widget.MNNotification

/**
 * fragment的基类，主要功能有
 *
 * 1. titleBar的支持
 * 2. 沉浸式状态栏的支持
 *
 * Created by imuto on 2018/4/23.
 */
abstract class RainbowFragment : SdkFragment(), ITitleBarView by TitleBarDelegate() {

    /**当前页面的titleBar动画是否是特殊动画，如果是的话，其他页面的titleBar动画使用页面相同的动画*/
    protected var isTitleBarAnimSpecial = false

    protected var mTitleBar: ITitleBarView.ImplView? = null
        set(value) {
            field = value
            field?.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE
            field?.setBackClickListener { finish() }
            getClassDelegate<TitleBarDelegate>(RainbowFragment::class)?.realTitleBar = field
            post {
                setNotificationMarginTopPx(mTitleBar?.getTitleBarHeight() ?: 0)
            }
        }

    protected var mStateView: PageStatesView? = null

    //状态栏颜色
    private var statusBarColor = -1

    private var isLightStatusBar = true

    //是否已检查过页面动画
    private var hasCheckAnimator = false

    /**
     * 当前的软键盘输入模式，参见[setSoftInputMode]
     */
    private var currSoftInputMode = -1

    /**页面内的通知view*/
    internal var mNotification: MNNotification? = null

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarColor = TranslucentStatusBarUtils.getStatusBarColor()
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view = if (getLayoutResId() != View.NO_ID) {
            layoutInflater.inflate(getLayoutResId(), container, false)
        } else injectLayout(container)

        assertNonNull(view, "没有页面可用，可以使用Layout注解，或者重写getLayoutResId()方法提供layout信息")

        return view!!
    }

    protected open fun getLayoutResId(): Int {
        return View.NO_ID
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.isClickable = true

        if (isUseDefaultNotification()) {
            mNotification = MNNotification(pageActivity)

            val notificationLayoutParams = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
            mViewParent.addView(mNotification, notificationLayoutParams)
            fixNotificationNormal()
        }

        if (isUseDefaultTitleBar() && container is RelativeLayout) {
            //添加titleBar
            mTitleBar = inflating(R.layout.rainbow_common_title_bar, container, false) as ITitleBarView.ImplView
            mTitleBar?.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_ALPHA
            var titleBarLayoutParam = (mTitleBar as View).layoutParams as? RelativeLayout.LayoutParams
            if (titleBarLayoutParam == null) {
                titleBarLayoutParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            titleBarLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            mViewParent.addView(mTitleBar as View, titleBarLayoutParam)

            // 在主页面上添加Titlebar
            (mViewParent.getMainAnimateView()?.layoutParams as? RelativeLayout.LayoutParams)
                    ?.addRule(RelativeLayout.BELOW, (mTitleBar as View).id)
        }

        if (isUseDefaultPageStatesView() && mTitleBar != null) {
            mStateView = PageStatesView(pageActivity)
            mStateView?.setOnErrorClickListener { onRefresh(false) }
            mViewParent.addView(mStateView)
        }
        container?.requestLayout()

        // 设置初始的softInputMode
        setSoftInputMode(Rainbow.getConfig().softInputMode)

        super.onViewCreated(view, savedInstanceState)

        hideKeyboard()
    }

    override fun onPageResume() {
        super.onPageResume()

        // 从新修正当前页面的softInputMode，由于基本上使用的是一个activity，
        // 可能下个页面将inputMode更改了导致当前页面错误。这里进行了场景复原
        val softInputMode = activity?.window?.attributes?.softInputMode
        if (currSoftInputMode != softInputMode) {
            setSoftInputMode(currSoftInputMode)
        }

        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            setStatusBarColor(statusBarColor)
            setLightStatusBar(isLightStatusBar)
        }

        val prePage = getPrePage(false) as? RainbowFragment ?: return

        val titleBar = mTitleBar ?: return

        if (prePage.isTitleBarAnimSpecial && titleBar.pageAnimatorFlag != ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE) {
            titleBar.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_SLID
        }
    }

    /** 设置statusBar的背景色  */
    fun setStatusBarColor(@ColorInt color: Int) {
        statusBarColor = color
        TranslucentStatusBarUtils.setStatusBarColor(color, pageActivity)
    }

    /** 设置是否是浅色statusBar样式  */
    fun setLightStatusBar(isLight: Boolean) {
        isLightStatusBar = isLight
        TranslucentStatusBarUtils.setLightStatusBar(isLight, pageActivity)
    }

    /*------------------titleBar相关----------------------------------------------------------------------------------------*/

    /**是否使用默认的titlebar，如果要自己实现，可返回false*/
    open fun isUseDefaultTitleBar() = true

    /**是否使用默认的pageStatesView，如果要自己实现，可返回false*/
    open fun isUseDefaultPageStatesView() = true

    /**是否使用默认的app内通知，通知会在fragment的view的头部，如果要自己实现，可返回false*/
    open fun isUseDefaultNotification() = true

    /**titelBar的title被双击*/
    fun setOnTitleDoubleClickListener(lis: () -> Unit) {
        mTitleBar?.setOnTitleDoubleClickListener(lis)
    }

    /**titelBar的title被双击*/
    fun setOnTitleCloseMenuClickListener(lis: () -> Unit) {
        mTitleBar?.setCloseMenuClickListener(lis)
    }

    /**初始化数据*/
    open fun onRefresh(isPullDown: Boolean = false) {
    }

    /**
     * 设置当前的软键盘输入模式
     */
    fun setSoftInputMode(mode: Int) {
        currSoftInputMode = mode
        activity?.window?.setSoftInputMode(mode)
    }

    override fun onPageDetach() {
        super.onPageDetach()
        mTitleBar = null
    }

    override fun onPreFinish(): Boolean {
        hideKeyboard()
        return super.onPreFinish()
    }
}