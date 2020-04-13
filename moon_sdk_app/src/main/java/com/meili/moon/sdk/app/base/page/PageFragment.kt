package com.meili.moon.sdk.app.base.page

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.page.util.TranslucentStatusBarUtils
import com.meili.moon.sdk.app.base.page.widget.ITitleBarView
import com.meili.moon.sdk.app.base.prefs.CommonPrefs
import com.meili.moon.sdk.app.base.role.IRoleStrategy
import com.meili.moon.sdk.app.base.role.RoleStrategy
import com.meili.moon.sdk.app.base.role.TargetRole
import com.meili.moon.sdk.app.util.*
import com.meili.moon.sdk.app.widget.pagetools.PageToolsLayout
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.common.UEHttpHolder
import com.meili.moon.sdk.base.util.ViewUtil
import com.meili.moon.sdk.page.internal.animators.PageLeftRightAnimator
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.primaryConstructor

/**
 * 封装埋点、
 * Created by imuto on 2018/4/23.
 */
abstract class PageFragment : BaseFragment(), UEHttpHolder {

    /**当前页面的titleBar动画是否是特殊动画，如果是的话，其他页面的titleBar动画使用页面相同的动画*/
    protected var isTitleBarPageAnimIsSpecial = false
    private var pageStartTime: Long = 0

    var roleStrategy: IRoleStrategy<*>? = null
        private set

    private var selfUEnable = true
    override var isUEnable: Boolean
        get() = !hasDestroyed && selfUEnable
        set(value) {
            selfUEnable = value
        }

    protected companion object {
        /**ue交互的类型：初始化，会优先显示pageTools的loading*/
        val UE_TYPE_INIT = 1
        /**ue交互的类型：默认，默认会显示ProgressDialog*/
        val UE_TYPE_DEF = 0
    }

    override val isDebugLog: Boolean = true

    private fun initRoleStrategy(view: View) {
        val annotation = this::class.findAnnotation<RoleStrategy>()
        val strategy = annotation?.strategy
        strategy?.forEach {
            if (isRightStrategy(it)) {
                val primaryConstructor = it.primaryConstructor
                        ?: throw RuntimeException("角色策略类请继承AbsRoleStrategy，并保证只有主构造器")
                roleStrategy = primaryConstructor.call(context, view)
            }
        }
    }

    private fun isRightStrategy(clazz: KClass<out IRoleStrategy<*>>): Boolean {
        val role = CommonPrefs.CommonPrefsImpl.userRole
        val annotationRole = clazz.findAnnotation<TargetRole>()
        annotationRole?.apply {
            if (role::class == this.role)
                return true
        }
        return false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRoleStrategy(view)
        roleStrategy?.onViewCreated(view)
        hideKeyboard()
    }

    override fun onPageCreated(view: View, savedInstanceState: Bundle?) {
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        roleStrategy?.onActivityCreated()
    }

    override fun onPageStart() {
        super.onPageStart()
        roleStrategy?.onStart()
    }

    override fun getContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return Sdk.view().inject(this, layoutInflater, container, false)
                ?: return super.getContentView(inflater, container, savedInstanceState)
    }

    override fun onPreFinish(): Boolean {
        hideKeyboard()
        roleStrategy?.apply {
            if (this.onPreFinish())
                return true
        }
        return super.onPreFinish()
    }

    override fun onPageResume() {
        super.onPageResume()

        val prePage = getPrePage(false) as? PageFragment ?: return

        val titleBar = mTitleBar ?: return

        if (prePage.isTitleBarPageAnimIsSpecial && titleBar.pageAnimatorFlag != ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE) {
            titleBar.pageAnimatorFlag = ITitleBarView.ImplView.PAGE_ANIM_FLAG_SLID
        }
        pageStartTime = System.currentTimeMillis()
        roleStrategy?.onPageResume()
    }

    private var hasCheckAnimator = false

    override fun onPageAnimIn(value: Float, isPopBack: Boolean) {
        if (!hasCheckAnimator) {
            hasCheckAnimator = true
            val prePage = getPrePage(false) as? PageFragment ?: return

            val pageAnim = pageTransitionAnimator
            if (prePage.isTitleBarPageAnimIsSpecial && pageAnim is PageLeftRightAnimator) {
                pageAnim.fixShadow(false)
            }
        }
        super.onPageAnimIn(value, isPopBack)
    }

    override fun onPagePause() {
        super.onPagePause()
        roleStrategy?.onPagePause()
    }

    fun showToast(msg: String?) {
        msg ?: return
        ToastUtil.show(msg)
    }

    fun showToastNormal(msg: String?, len: Int = Toast.LENGTH_SHORT) {
        msg ?: return
        ToastUtil.show(msg, len)
    }

    fun showToastSuccess(msg: String?, len: Int = Toast.LENGTH_SHORT) {
        msg ?: return
        ToastUtil.show(msg, len)
    }

    fun showToastWarning(msg: String?, len: Int = Toast.LENGTH_SHORT) {
        msg ?: return
        ToastUtil.show(msg, len)
    }

    fun showToastFailed(msg: String?, len: Int = Toast.LENGTH_SHORT) {
        msg ?: return
        ToastUtil.show(msg, len)
    }

    /**显示一个notification，默认2秒关闭*/
    fun showNotification(msg: CharSequence, delayDismissSecond: Int = 2) {
        mNotification.show(msg, delayDismissSecond)
    }

    override fun getUEDelayMills(): Long {
        val duration = System.currentTimeMillis() - pageStartTime
        return (fragmentAnimator?.duration ?: 0) - duration
    }

    override fun showUEErrorMessage(msg: String?, ueType: Int) {
        if (ueType == UE_TYPE_INIT && mPageTool != null) {
            mPageTool?.showError()
        } else {
            showToast(msg)
        }
    }

    override fun showUEProgress(msg: String?, ueType: Int) {
        if (ueType == UE_TYPE_INIT && mPageTool != null) {
            mPageTool?.showLoading()
        } else {
            showProgressDialog()
        }
    }

    override fun dismissUEProgress(ueType: Int) {
        if (ueType == UE_TYPE_INIT && mPageTool != null) {
            if (mPageTool?.currentFlag == PageToolsLayout.FLAG_LOADING) {
                mPageTool?.gone()
            }
        } else {
            dismissProgressDialog()
        }
    }

    override fun onPageStop() {
        super.onPageStop()
        if (isRemoving || isFinishing()) {
            cancelHttpTasks()
            dismissSelfProgress()
        }
        roleStrategy?.onStop()
    }

    /** 为沉浸式修改top部分的View的Padding，如果不支持沉浸式，则不修改，只调用一次，例如在onCreate中调用  */
    fun fixTranslucentStatusBarTopPadding(view: View) {
        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            view.setPadding(view.paddingLeft, view.paddingTop + ViewUtil.getStatusBarHeight(), view.paddingRight, view.paddingBottom)
        }
    }

    /**
     * 设置标题是否为透明，如果有的话
     */
    fun setTitleTransparentStyle(transparentStyle: Boolean) {
        if (transparentStyle) {
            setStyle(R.style.titleTransparentTheme)
        } else {
            setStyle(0)
        }
    }

    override fun onPageDestroyView() {
        super.onPageDestroyView()
        roleStrategy?.onDestroyView()
        dismissSelfProgress()
        cancelHttpTasks()
    }

    override fun onPageDestroy() {
        super.onPageDestroy()
        roleStrategy?.onDestroy()
    }

    override fun onPageDetach() {
        super.onPageDetach()
        roleStrategy?.onDetach()
    }

    override fun onPageResult(requestCode: Int, resultCode: Int, intent: Intent) {
        super.onPageResult(requestCode, resultCode, intent)
        roleStrategy?.onPageResult(requestCode, resultCode, intent)
    }
}