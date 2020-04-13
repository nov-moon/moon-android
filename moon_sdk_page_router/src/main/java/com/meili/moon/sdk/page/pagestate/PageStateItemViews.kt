package com.meili.moon.sdk.page.pagestate

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import com.meili.moon.sdk.page.R
import kotlinx.android.synthetic.main.rainbow_page_state_empty_view.*
import kotlinx.android.synthetic.main.rainbow_page_state_error_view.*
import kotlinx.android.synthetic.main.rainbow_page_state_loading_view.*

/**
 * 页面 loading 的状态view
 *
 * Created by imuto on 2019-08-14.
 */
class PageStateLoadingView(parent: ViewGroup) : PageStateItemView.AbsStateItemView(parent) {

    /**页面的layout资源id*/
    override fun getLayoutId(): Int = R.layout.rainbow_page_state_loading_view

    override var stateParams: PageStateParams = Params()

    override val state: Int = PageStatesView.STATE_LOADING

    override fun notifyParamChange() {
        super.notifyParamChange()

        mPageStateLoadingProgress.indeterminateDrawable.setColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY)
        mPageStateLoadingTxt.visibility = stateParams.tipTextVisibility
        mPageStateLoadingTxt.text = stateParams.tipText
    }

    class Params : PageStateParams.AbsParams() {
        /**提示的文案*/
        override var tipText: CharSequence = "请稍后"
    }
}


/**
 * 页面 空 的状态view
 *
 * Created by imuto on 2019-08-14.
 */
class PageStateEmptyView(parent: ViewGroup) : PageStateItemView.AbsStateItemView(parent) {

    /**页面的layout资源id*/
    override fun getLayoutId(): Int = R.layout.rainbow_page_state_empty_view

    override val state: Int = PageStatesView.STATE_EMPTY

    override var stateParams: PageStateParams = Params()

    override fun notifyParamChange() {
        super.notifyParamChange()

        mTxtPageStateEmpty.visibility = stateParams.tipTextVisibility
        mTxtPageStateEmpty.text = stateParams.tipText

        (stateParams as Params).apply {
            if (tipImageId == 0) {
                mImgPageStateEmpty.visibility = View.GONE
                return@apply
            }
            mImgPageStateEmpty.setImageResource(tipImageId)
            mImgPageStateEmpty.visibility = tipImageVisibility
        }
    }

    open class Params : PageStateParams.AbsParams() {
        /**提示的文案*/
        override var tipText: CharSequence = "暂无内容哦"

        /**提示图片的资源id*/
        open var tipImageId = 0

        /**提示图片的显示状态*/
        open var tipImageVisibility = View.VISIBLE
    }
}


/**
 * 页面 本地错误 的状态view
 *
 * Created by imuto on 2019-08-14.
 */
open class PageStateErrorView(parent: ViewGroup) : PageStateItemView.AbsStateItemView(parent) {

    override fun getOnClickView(): View = mTxtPageStateErrorReload

    /**页面的layout资源id*/
    override fun getLayoutId(): Int = R.layout.rainbow_page_state_error_view

    override val state: Int = PageStatesView.STATE_ERROR

    override var stateParams: PageStateParams = Params()

    override fun notifyParamChange() {
        super.notifyParamChange()

        mTxtPageStateError.visibility = stateParams.tipTextVisibility
        mTxtPageStateError.text = stateParams.tipText

        (stateParams as Params).apply {
            if (tipImageId == 0) {
                mImgPageStateError.visibility = View.GONE
                return@apply
            }
            mImgPageStateError.setImageResource(tipImageId)
            mImgPageStateError.visibility = tipImageVisibility
        }
    }

    class Params : PageStateEmptyView.Params() {
        /**提示的文案*/
        override var tipText: CharSequence = "出错了😭"
    }
}
