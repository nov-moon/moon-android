package com.meili.moon.sdk.app.base.role

import android.content.Context
import android.os.Bundle
import android.view.View
import com.meili.moon.sdk.app.base.IKeyValueModel
import com.meili.moon.sdk.app.util.*
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.page.PageIntent
import com.meili.moon.ui.dialog.widget.MNDialog
import kotlinx.android.extensions.LayoutContainer

/**
 * Author wudaming
 * Created on 2018/9/26
 */
abstract class AbsRoleStrategy<T>(val context: Context, override val containerView:View) : IRoleStrategy<T>,LayoutContainer,IDestroable {

    override var hasDestroyed: Boolean = false

    override fun onViewCreated(view: View) {
    }

    override fun showProgressDialog(message: CharSequence?, holder: Any?) {
        if (message == null)
            context.showProgressDialog(holder = holder)
        else
            context.showProgressDialog(message, holder)
    }

    override fun dismissProgressDialog() {
        context.dismissProgressDialog()
    }

    override fun showPerformDialog(title: String, msg: String?, leftBtnStr: String?, rightBtnStr: String?, leftBtnListener: (() -> Boolean)?, rightBtnListener: (() -> Boolean)?) {
        context.showPerformDialog(title, msg, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener)
    }

    override fun showSuccessTip(message: CharSequence?) {
        if (message == null)
            context.showSuccessTip()
        else
            context.showSuccessTip(message)
    }

    override fun showErrorTip(message: CharSequence?) {
        if (message == null)
            context.showErrorTip()
        else
            context.showErrorTip(message)
    }

    override fun showActionSheet(contentView: View, title: String, leftBtnStr: String?, rightBtnStr: String?, leftBtnListener: (() -> Boolean)?, rightBtnListener: (() -> Boolean)?, autoShow: Boolean): MNDialog {
        return context.showActionSheet(contentView, title, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener, autoShow)
    }

    override fun showActionSheet(autoShow: Boolean): ActionBuilder {
        return context.showActionSheet(autoShow)
    }

    override fun showActionSheet(contentView: View): MNDialog {
        return context.showActionSheet(contentView)
    }

    override fun showActionWheelByIndex(data: List<IKeyValueModel>, selectIndex: Int, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
        return context.showActionWheelByIndex(data, selectIndex, onSubmit)
    }

    fun showActionWheel(data: List<IKeyValueModel>,title:String="", selectIndex: Int, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
        return context.showActionWheelByIndex(data, selectIndex, onSubmit,title)
    }

    override fun showChoiceDialog(title: CharSequence, items: Array<IKeyValueModel>, delayDismiss: Long, listener: ((Int) -> Boolean)?): MNDialog {
        return context.showChoiceDialog(title, items, delayDismiss, null, listener)
    }

    override fun showDateDialog(selectTime: Long, listener: (year: Int, month: Int, day: Int, time: Long) -> Unit) {
        context.showDateDialog(selectTime, listener)
    }

    fun gotoPage(pageName: String, bundle: Bundle? = null, nickName: String? = null,canSameWithPre:Boolean = false) {
        val pageIntent = PageIntent(pageName, nickName)
        if (bundle != null) {
            pageIntent.putExtras(bundle)
        }

        Sdk.page().gotoPage(pageIntent,canSameWithPre = canSameWithPre)
    }

    fun getString(id:Int): String {
        return Sdk.app().getString(id)
    }

    override fun onDestroyView() {
        hasDestroyed = true
        super.onDestroyView()
    }

}