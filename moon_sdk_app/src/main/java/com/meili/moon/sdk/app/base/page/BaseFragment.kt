package com.meili.moon.sdk.app.base.page

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.page.util.TranslucentStatusBarUtils
import com.meili.moon.sdk.app.base.page.widget.ITitleBarView
import com.meili.moon.sdk.app.base.page.widget.Menu
import com.meili.moon.sdk.app.base.page.widget.MenuIcon
import com.meili.moon.sdk.app.base.page.widget.MenuText
import com.meili.moon.sdk.app.util.setSoftInputModeResize
import com.meili.moon.sdk.app.widget.pagetools.PageToolsLayout
import com.meili.moon.sdk.base.util.OnNormalCallback
import com.meili.moon.sdk.base.util.inflating
import com.meili.moon.sdk.base.util.px
import com.meili.moon.sdk.page.exception.StartPageException
import com.meili.moon.sdk.page.internal.SdkFragment
import com.meili.moon.sdk.page.internal.utils.isDirty
import com.meili.moon.ui.dialog.widget.MNNotification

/**
 * fragment的基类，主要功能有
 *
 * 1. titleBar的支持
 * 2. progressDialog的支持
 * 3. toolLayout的支持
 * 4. 沉浸式状态栏的支持
 *
 * Created by imuto on 2018/4/23.
 */
abstract class BaseFragment : SdkFragment(), ITitleBarView {

    protected var mTitleBar: ITitleBarView.ImplView? = null
        set(value) {
            field = value
            field?.setBackClickListener { onTitleBarBackClick() }
            field?.setH5BackClickListener { onTitleBarH5CloseBackClick() }
            field?.setOnTitleDoubleClickListener { onTitleDoubleClick() }
            field?.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE
        }

    protected var mPageTool: PageToolsLayout? = null
    //状态栏颜色
    private var statusBarColor = -1

    private var isLightStatusBar = true

    var isH5ForceClose = false

    /**页面内的通知view*/
    protected lateinit var mNotification: MNNotification

    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusBarColor = TranslucentStatusBarUtils.getStatusBarColor()
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return if (getLayoutResId() != View.NO_ID) {
            layoutInflater.inflate(getLayoutResId(), container, false)
        } else throw StartPageException(msg = "layoutId无效，无法初始化页面")
    }

    protected open fun getLayoutResId(): Int {
        return View.NO_ID
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isClickable = true

        if (isUseDefaultNotification()) {
            mNotification = MNNotification(pageActivity!!)
            val notificationLayoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            mViewParent.addView(mNotification, notificationLayoutParams)
        }

        if (isUseDefaultTitleBar() && container is RelativeLayout) {
            //添加titleBar
            mTitleBar = inflating(R.layout.moon_sdk_app_common_title_bar, container, false) as ITitleBarView.ImplView
            mTitleBar?.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_ALPHA
            var titleBarLayoutParam = (mTitleBar as View).layoutParams as? RelativeLayout.LayoutParams
            if (titleBarLayoutParam == null) {
                titleBarLayoutParam = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
            titleBarLayoutParam.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            container?.addView(mTitleBar as View, titleBarLayoutParam)
            if (savedInstanceState == null && getPrePage(false) != null) {
                mTitleBar?.titleBarVisibility = View.INVISIBLE
            } else {
                mTitleBar?.titleBarVisibility = View.VISIBLE
            }

            //添加依赖关系
            (mViewParent.layoutParams as RelativeLayout.LayoutParams).addRule(RelativeLayout.BELOW, (mTitleBar as View).id)
        }

        if (isUseDefaultPageTool() && mTitleBar != null) {
            mPageTool = LayoutInflater.from(context)
                    .inflate(R.layout.moon_sdk_app_common_page_tool, mViewParent, false) as PageToolsLayout
            mPageTool?.addOnFlagListener(object : PageToolsLayout.OnStateListener() {
                override fun onClickError(): Boolean {
                    onRefresh(false)
                    return super.onClickError()
                }
            })
            mViewParent.addView(mPageTool)
        }
        container?.requestLayout()

        if (isUseSystemResumeAndPause()) {
            setSoftInputModeResize()
        }
    }

    override fun onPageResume() {
        super.onPageResume()
        val translationX = view?.translationX ?: 0F
        if (translationX != 0F) {
            view?.translationX = 0F
        }
        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle() && isUseSystemResumeAndPause()) {
            setStatusBarColor(statusBarColor)
            setLightStatusBar(isLightStatusBar)
//            TranslucentStatusBarUtils.statusBarLightMode(pageActivity, !isLightStatusBar)
        }
    }

    override fun onPageAnimIn(value: Float, isPopBack: Boolean) {
        super.onPageAnimIn(value, isPopBack)
        val title = mTitleBar ?: return
        if (title.pageAnimatorFlag == ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE) {
            return
        }
        title.onPageAnimIn(value, isPopBack)
        if (title.titleBarVisibility != View.VISIBLE) {
//            Sdk.task().postOnce(188667) { title.titleBarVisibility = View.VISIBLE }
            title.titleBarVisibility = View.VISIBLE
        }
    }

    override fun onPageAnimOut(value: Float, isPopBack: Boolean) {
        super.onPageAnimOut(value, isPopBack)
        mTitleBar?.onPageAnimOut(value, isPopBack)
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

    fun getTitleBar(): ITitleBarView.ImplView {
        return mTitleBar!!
    }

    fun getTitleBarCanNull(): ITitleBarView.ImplView? {
        return mTitleBar
    }

    /**是否使用默认的titlebar，如果要自己实现，可返回false*/
    open fun isUseDefaultTitleBar() = true

    /**设置页面view距离titleBar的高度，默认为0，只有[isUseDefaultTitleBar]为true时才可用*/
    fun setContentMarginTitleBarDp(topMargin: Float) {
        if (!isUseDefaultTitleBar()) return
        if (topMargin < 0) {
            mViewParent.bringToFront()
        }
        (mViewParent.layoutParams as RelativeLayout.LayoutParams).topMargin = topMargin.px.toInt()
    }

    /**是否使用默认的pageTool，如果要自己实现，可返回false*/
    open fun isUseDefaultPageTool() = true


    /**是否使用默认的app内通知，通知会在fragment的view的头部，如果要自己实现，可返回false*/
    open fun isUseDefaultNotification() = true

    /**设置通知距离顶部的margin*/
    fun setNotificationMarginTopPx(px: Int) {
        (mNotification.layoutParams as FrameLayout.LayoutParams).topMargin = px
    }

    /**titelBar的title被双击*/
    open fun onTitleDoubleClick() {
    }

    /**
     * title back click
     */
    open fun onTitleBarBackClick() {
        finish()
    }

    open fun onTitleBarH5CloseBackClick() {
        isH5ForceClose = true
        onTitleBarBackClick()
    }

    /**初始化数据*/
    open fun onRefresh(isPullDown: Boolean = false) {
    }

    override fun setH5CloseVisible() {
        mTitleBar?.setH5CloseVisible()
    }

    override fun setH5CloseGone() {
        mTitleBar?.setH5CloseGone()
    }

    override fun setTitle(text: CharSequence?) {
        mTitleBar?.setTitle(text)
    }

    override fun setTitle(resId: Int) {
        mTitleBar?.setTitle(resId)
    }

    override fun setTitleTextColor(color: Int) {
        mTitleBar?.setTitleTextColor(color)
    }

    override fun setBackIcon(resId: Int) {
        mTitleBar?.setBackIcon(resId)
    }

    override fun setBackIcon(drawable: Drawable?) {
        mTitleBar?.setBackIcon(drawable)
    }

    override fun setBackIconVisible(visible: Int) {
        mTitleBar?.setBackIconVisible(visible)
    }

    override fun setBackText(resId: Int) {
        mTitleBar?.setBackText(resId)
    }

    override fun setBackText(text: CharSequence?) {
        mTitleBar?.setBackText(text)
    }

    override fun setBackTextColor(resId: Int) {
        mTitleBar?.setBackTextColor(resId)
    }

    override fun setTitleBarBackgroundColor(color: Int) {
        mTitleBar?.setTitleBarBackgroundColor(color)
    }

    override fun setTitleBarBackgroundDrawable(resId: Int) {
        mTitleBar?.setTitleBarBackgroundDrawable(resId)
    }

    /**
     * 添加TextMenu,如果要设置字体颜色等,需要使用返回的MenuText进行设置
     */
    fun addMenuText(text: CharSequence, listener: View.OnClickListener): MenuText? {
        if (isFinishing()) {
            return null
        }
        val menuText = MenuText(pageActivity)
        menuText.setText(text)
        menuText.setOnClickListener(listener)
        addMenu(text.toString(), menuText)
        return menuText
    }

    /**
     * 添加TextMenu,如果要设置字体颜色等,需要使用返回的MenuText进行设置
     */
    fun addMenuText(text: CharSequence, listener: OnNormalCallback): MenuText? {
        if (isFinishing()) {
            return null
        }
        val menuText = MenuText(pageActivity)
        menuText.setText(text)
        if (listener != null) {
            menuText.setOnClickListener { listener() }
        }
        addMenu(text.toString(), menuText)
        return menuText
    }

    /**添加一个icon类型的menu*/
    fun addMenuIcon(@DrawableRes drawable: Int, lsn: View.OnClickListener): MenuIcon? {
        return addMenuIcon(drawable, lsn::onClick)
    }

    /**添加一个icon类型的menu*/
    fun addMenuIcon(@DrawableRes drawable: Int, listener: (View) -> Unit): MenuIcon? {
        if (isFinishing()) {
            return null
        }
        val menuIcon = MenuIcon(pageActivity)
        menuIcon.setOnClickListener(listener)
        menuIcon.setImageResource(drawable)
        addMenu(drawable.toString() + "", menuIcon)
        return menuIcon
    }

    override fun removeMenu(menu: Menu?) {
        mTitleBar?.removeMenu(menu)
    }

    override fun onPageDestroyView() {
        super.onPageDestroyView()
        if (isDirty) {
            val titleView = mTitleBar as? View ?: return
            val parent = titleView.parent ?: return
            if (parent == container) {
                container?.removeView(titleView)
            }
        }
    }

    override fun onPageDestroy() {
        super.onPageDestroy()
        if (!isDirty) {
            val titleView = mTitleBar as? View ?: return
            val parent = titleView.parent ?: return
            if (parent == container) {
                container?.removeView(titleView)
            }
        }

    }

    override fun onPageDetach() {
        super.onPageDetach()
        mTitleBar = null
    }

    @Deprecated("请使用其他便捷的方法添加menu，例如：addMenuIcon()、addMenuText()")
    override fun addMenu(id: String?, menu: Menu?) {
        mTitleBar?.addMenu(id, menu)
    }

    override fun setTitleBarVisibility(visible: Int) {
        mTitleBar?.titleBarVisibility = visible
    }

    override fun getTitleBarVisibility(): Int {
        return mTitleBar?.titleBarVisibility ?: View.GONE
    }

    override fun setSupportNoNetworkStyle(isSupport: Boolean) {
        mTitleBar?.setSupportNoNetworkStyle(isSupport)
    }

    override fun setStyle(style: Int) {
        mTitleBar?.setStyle(style)
    }
}