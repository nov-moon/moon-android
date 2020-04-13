package com.meili.moon.sdk.page.pagestate

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.meili.moon.sdk.util.fadeOut
import kotlinx.android.extensions.LayoutContainer
import java.util.*

/**
 * 页面的各种状态的辅助展示view
 *
 * 一般一个页面会有
 * 内部定义了页面loading、空、错误等状态。你还可以通过[addStateView]方法添加自定义的状态view。
 *
 * Created by imuto on 2019-08-14.
 */
class PageStatesView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    companion object {
        /** 页面状态：隐藏  */
        val STATE_GONE = -1
        /** 页面状态：loading  */
        val STATE_LOADING = 0
        /** 页面状态：empty  */
        val STATE_EMPTY = 1
        /** 页面状态：错误  */
        val STATE_ERROR = 2
    }

    /** 页面状态对应的view，index为对应的flag  */
    private val mStateViews = SparseArray<PageStateItemView>()

    /** 当前状态  */
    private var mState = STATE_GONE
    /**
     * 页面状态监听器
     */
    private val mStateChangeListeners = ArrayList<(state: Int) -> Boolean>()

    private var mLoadingCount = 0

    init {
        initViews()
    }

    protected fun initViews() {
        // 初始化loading页面
        addStateView(PageStateLoadingView(this))
        // 初始化空页面
        addStateView(PageStateEmptyView(this))
        // 初始化错误页面
        addStateView(PageStateErrorView(this))

        gone(true)
    }

    /**获取当前状态*/
    fun getState(): Int {
        return mState
    }

    /**添加状态View到当前页面*/
    fun addStateView(stateView: PageStateItemView) {
        removeStateView(stateView.state)

        mStateViews.put(stateView.state, stateView)
        stateView.attachView(this).visibility = View.INVISIBLE
    }

    /**获取指定[state]的param*/
    fun getParams(state: Int): PageStateParams? {
        return getStateView(state)?.stateParams
    }

    /**获取指定[state]的状态view*/
    fun getStateView(state: Int): PageStateItemView? {
        if (!isLegalState(state)) {
            return null
        }
        return mStateViews.get(state)
    }

    /**
     * 移除指定状态的StateView
     */
    fun removeStateView(state: Int) {
        val stateView = getStateView(state) ?: return
        val view = stateView.getView()
        removeView(view)
        mStateViews.remove(state)
    }

    /**设置指定[state]的param*/
    fun setParams(state: Int, params: PageStateParams) {
        mStateViews.get(state)?.stateParams = params
    }

    /**显示空页面*/
    fun showEmpty(emptyText: String? = null) {
        if (emptyText != null) {
            val stateView = getStateView(STATE_EMPTY)
            stateView?.stateParams?.tipText = emptyText
        }

        show(STATE_EMPTY)
    }

    /**显示loading页面*/
    fun showLoading() {
        show(STATE_LOADING)
    }

    /**显示错误页面*/
    fun showError(errorMsg: String? = null) {
        if (errorMsg != null) {
            val stateView = getStateView(STATE_ERROR)
            stateView?.stateParams?.tipText = errorMsg
        }
        show(STATE_ERROR)
    }

    /**显示指定[state]的页面*/
    fun show(state: Int) {

        if (!isLegalState(state)) {
            return
        }

        if (mState == state) {
            return
        }

        mStateChangeListeners.forEach {
            if (it(state)) {
                return
            }
        }

        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
        }

        this.bringToFront()

        if (state == STATE_LOADING) {
            mLoadingCount++
        } else {
            mLoadingCount = 0
        }

        if (!goneCurrent(true)) {
            return
        }

        mState = state

        // next
        val stateView = mStateViews.get(state)
        stateView.show()

    }

    /**隐藏所有页面*/
    fun gone(immediately: Boolean = false) {
        if (!goneCurrent(false)) {
            return
        }
        if (!immediately && this.visibility == View.VISIBLE) {
            fadeOut(300)
        } else {
            visibility = View.GONE
        }
    }

    private fun goneCurrent(isSwitchType: Boolean): Boolean {
        if (mState == STATE_LOADING) {
            mLoadingCount--
            if (mLoadingCount > 1) {
                return false
            }
        }
        val lastStateView = mStateViews.get(mState)
        if (lastStateView != null) {
            onGonCurr(lastStateView, isSwitchType)
        }
        mState = STATE_GONE
        return true
    }

    protected fun onGonCurr(stateView: PageStateItemView, isSwitchType: Boolean) {
        stateView.hide()
    }

    protected fun isLegalState(state: Int): Boolean {
        val index = mStateViews.indexOfKey(state)
        if (index < 0) {
            return false
        }
        return true
    }

    /**
     * 添加页面状态变化监听，如果监听返回true，则会拦截页面状态变化。
     */
    fun addOnStateChangeListener(listener: (state: Int) -> Boolean) {
        mStateChangeListeners.add(listener)
    }

    /**
     * 设置指定[state]的状态页面点击事件，如果要清除事件，可设置为null
     */
    fun setOnStateClickListener(state: Int, listener: (() -> Unit)?) {
        getStateView(state)?.stateParams?.clickListener = listener
    }

    /**
     * 设置错误状态页面点击事件，如果要清除事件，可设置为null
     */
    fun setOnErrorClickListener(listener: (() -> Unit)?) {
        setOnStateClickListener(STATE_ERROR, listener)
    }

    /**
     * 设置空状态页面点击事件，如果要清除事件，可设置为null
     */
    fun setOnEmptyClickListener(listener: (() -> Unit)?) {
        setOnStateClickListener(STATE_ERROR, listener)
    }
}

/**
 * 页面状态的通用入参
 *
 * 每种页面状态都应该有自己对应的页面入参，这里定义了常见的通用状态设置；每种不同的状态页面还可以自定义自己的入参信息
 */
interface PageStateParams {
    /**提示的文案*/
    var tipText: CharSequence

    /**提示文案的显示状态*/
    var tipTextVisibility: Int

    /**当前状态view的点击事件*/
    var clickListener: (() -> Unit)?

    abstract class AbsParams : PageStateParams {

        override var tipTextVisibility: Int = View.VISIBLE

        override var clickListener: (() -> Unit)? = null
    }
}

/**
 * 页面状态的具体状态View
 *
 * 定义了页面状态的常用方法，入参，绑定状态等信息
 */
interface PageStateItemView : LayoutContainer {

    /**当前页面的param信息，里面存储了页面的显示参数*/
    var stateParams: PageStateParams

    /**当前页面的state值*/
    val state: Int

    /**更新当前状态页面*/
    fun notifyParamChange()

    /**显示状态页面*/
    fun show()

    /**隐藏状态页面*/
    fun hide()

    /**初始化状态页面，并尝试绑定他到[parent]中*/
    fun attachView(parent: ViewGroup): View

    /**获取当前状态页面的View，调用前请先调用[attachView]方法*/
    fun getView(): View

    abstract class AbsStateItemView(parent: ViewGroup) : PageStateItemView {

        final override val containerView: View? = attachView(parent)

        var contentView: View? = null

        /**页面的layout资源id*/
        abstract fun getLayoutId(): Int

        /**
         * 获取处理状态点击的View
         */
        open fun getOnClickView(): View = getView()

        /**
         * 创建View，一般不用关心此方法
         */
        open fun onCreateView(parent: ViewGroup): View {
            return LayoutInflater.from(parent.context).inflate(getLayoutId(), parent, false)
        }

        override fun notifyParamChange() {
            if (stateParams.clickListener != null) {
                getOnClickView().setOnClickListener {
                    stateParams.clickListener?.invoke()
                }
            } else getOnClickView().setOnClickListener(null)
        }

        final override fun attachView(parent: ViewGroup): View {

            val valView = contentView ?: onCreateView(parent)

            if (valView.parent == null) {
                parent.addView(valView)
            }

            contentView = valView

            return valView
        }

        override fun getView(): View {
            return contentView!!
        }

        override fun show() {
            notifyParamChange()
            contentView?.visibility = View.VISIBLE
        }

        override fun hide() {
            contentView?.visibility = View.INVISIBLE
        }
    }
}
