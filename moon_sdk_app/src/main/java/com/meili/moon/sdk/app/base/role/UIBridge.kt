package com.meili.moon.sdk.app.base.role

import android.view.View
import com.meili.moon.sdk.app.base.IKeyValueModel
import com.meili.moon.sdk.app.util.ActionBuilder
import com.meili.moon.ui.dialog.widget.MNDialog

/**
 * Author wudaming
 * Created on 2018/9/26
 */
interface UIBridge {
    fun showProgressDialog(message: CharSequence? = null, holder: Any? = null)
    fun dismissProgressDialog()

    fun showPerformDialog(title: String, msg: String? = null, leftBtnStr: String? = null, rightBtnStr: String? = null,
                          leftBtnListener: (() -> Boolean)? = null,
                          rightBtnListener: (() -> Boolean)? = null)

    fun showSuccessTip(message: CharSequence? = null)
    fun showErrorTip(message: CharSequence? = null)

    fun showActionSheet(contentView: View, title: String = "", leftBtnStr: String? = null, rightBtnStr: String? = null,
                        leftBtnListener: (() -> Boolean)? = null,
                        rightBtnListener: (() -> Boolean)? = null, autoShow: Boolean = true): MNDialog
    fun showActionSheet(autoShow: Boolean = true): ActionBuilder

    fun showActionSheet(contentView: View): MNDialog

    fun showActionWheelByIndex(data: List<IKeyValueModel>, selectIndex: Int = -1, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog

    fun showChoiceDialog(title: CharSequence, items: Array<IKeyValueModel>, delayDismiss: Long = 300, listener: ((Int) -> Boolean)? = null): MNDialog

    fun showDateDialog(selectTime: Long = 0, listener: ((year: Int, month: Int, day: Int, time: Long) -> Unit))


}