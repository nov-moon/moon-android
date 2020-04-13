package com.meili.moon.sdk.app.util

import android.content.Context
import android.graphics.Color
import android.support.annotation.LayoutRes
import android.support.annotation.MainThread
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.IKeyValueModel
import com.meili.moon.sdk.app.base.KeyValueModel
import com.meili.moon.sdk.app.base.page.BaseFragment
import com.meili.moon.sdk.app.base.page.PageFragment
import com.meili.moon.sdk.app.base.page.widget.MNProgressDialog
import com.meili.moon.sdk.app.util.ProgressDialogRunnable.Companion.dismissByHolder
import com.meili.moon.sdk.app.widget.datepicker.DatePicker
import com.meili.moon.sdk.app.widget.datepickerwithoutday.DatePickerWithoutDay
import com.meili.moon.sdk.app.widget.pagetools.PageToolsLayout
import com.meili.moon.sdk.app.widget.wheelview.KeyValueWheelAdapter
import com.meili.moon.sdk.app.widget.wheelview.WheelView
import com.meili.moon.sdk.base.Sdk
import com.meili.moon.sdk.base.util.inflating
import com.meili.moon.sdk.base.util.onClick
import com.meili.moon.sdk.base.util.px
import com.meili.moon.sdk.common.IDestroable
import com.meili.moon.sdk.common.SuccessLambda
import com.meili.moon.sdk.http.IRequestParams
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.ui.dialog.config.MNDialogConfig
import com.meili.moon.ui.dialog.widget.MNDialog
import com.meili.moon.ui.dialog.widget.MNToast

/**
 * Created by imuto on 2018/6/14.
 */

private class ProgressDialogRunnable : Runnable {
    var dialog: MNProgressDialog? = null
    var msg: CharSequence = ""
    var holder: Any? = null

    /**标记当前状态，0初始状态，1等待显示, 2等待结束*/
    private var state = STATE_INITIALIZE

    companion object {
        private const val DELAY_TIME = 300L
        /**显示状态：初始化*/
        private const val STATE_INITIALIZE = 0
        /**显示状态：等待显示*/
        private const val STATE_SHOW_WAITING = 1
        /**显示状态：等待结束*/
        private const val STATE_DISMISS_WAITING = 3

        private var mProgressRunnable = ProgressDialogRunnable()
        private var mProgressDismissRunnable = Runnable {
            mProgressRunnable.recycle()
        }

        fun isShowing(): Boolean {
            return mProgressRunnable.dialog?.isShowing() ?: false
                    || mProgressRunnable.state == STATE_SHOW_WAITING
        }

        fun tryShow(context: Context, message: CharSequence, holder: Any?) {
            //如果已经显示（等待显示、正在显示）
//            LogUtil.e("AddCount" + count.count)
            count.increment()
            if (isShowing()) {
                updateMessage(message)
                cancelDismissRequest()
                mProgressRunnable.state = STATE_SHOW_WAITING
                return
            }

            mProgressRunnable.holder = holder
            mProgressRunnable.msg = message
            mProgressRunnable.dialog = MNProgressDialog(context)

            val task = Sdk.task()

            mProgressRunnable.state = STATE_SHOW_WAITING

            task.removeCallbacks(mProgressDismissRunnable)
            task.removeCallbacks(mProgressRunnable)

            task.post(mProgressRunnable, DELAY_TIME)
        }

        fun tryShowSuccess(context: Context, message: CharSequence) {
            if (mProgressRunnable.dialog?.isShowing() == true) {
                mProgressRunnable.state = STATE_INITIALIZE
                mProgressRunnable.dialog?.showSuccess(message)
                return
            }

            mProgressRunnable.state = STATE_INITIALIZE

            val task = Sdk.task()
            task.removeCallbacks(mProgressDismissRunnable)
            task.removeCallbacks(mProgressRunnable)

            mProgressRunnable.dialog = MNProgressDialog(context)
            mProgressRunnable.dialog?.showSuccess(message)
        }

        fun tryShowError(context: Context, message: CharSequence) {
            if (mProgressRunnable.dialog?.isShowing() == true) {
                mProgressRunnable.state = STATE_INITIALIZE
                mProgressRunnable.dialog?.showError(message)
                return
            }

            mProgressRunnable.state = STATE_INITIALIZE

            val task = Sdk.task()
            task.removeCallbacks(mProgressDismissRunnable)
            task.removeCallbacks(mProgressRunnable)

            mProgressRunnable.dialog = MNProgressDialog(context)
            mProgressRunnable.dialog?.showError(message)
        }

        fun tryDismiss(immediate: Boolean) {
            count.decrement()
            val task = Sdk.task()
            if (immediate) {
                task.removeCallbacks(mProgressRunnable)
                mProgressDismissRunnable.run()
                return
            }
//            LogUtil.e("DismissCount" + count.count)
            if (count.count > 0) {
                return
            }
            task.removeCallbacks(mProgressRunnable)

            if (mProgressRunnable.state == STATE_DISMISS_WAITING) {
                return
            }

            mProgressRunnable.state = STATE_DISMISS_WAITING
            task.post(mProgressDismissRunnable, DELAY_TIME)
        }

        fun updateMessage(message: CharSequence) {
            mProgressRunnable.msg = message
            mProgressRunnable.dialog?.setProgressDialogText(mProgressRunnable.msg)
        }

        fun cancelDismissRequest() {
            Sdk.task().removeCallbacks(mProgressDismissRunnable)
        }

        fun dismissByHolder(h: IDestroable) {
//            LogUtil.e("h: $h  mProgressRunnable.holder: ${mProgressRunnable.holder}")
            if (h != mProgressRunnable.holder) {
                return
            }
            val task = Sdk.task()
            task.removeCallbacks(mProgressDismissRunnable)
            task.removeCallbacks(mProgressRunnable)
            task.post(mProgressDismissRunnable, DELAY_TIME)
        }

        var count = ProgressCount()
    }

    override fun run() {
        val holderVal = holder
        if ((holderVal is IDestroable && holderVal.hasDestroyed) || state == STATE_DISMISS_WAITING) {
            recycle()
            return
        }
        dialog?.show(msg)
        state = STATE_INITIALIZE
        mProgressRunnable.dialog?.setOnDismissListener {
            count.cleanCount()
        }
    }

    fun recycle() {
        dialog?.dismiss()
        dialog = null
        msg = ""
        holder = null
        state = STATE_INITIALIZE
        count.cleanCount()
    }

}

fun IDestroable.dismissSelfProgress() {
    dismissByHolder(this)
}

@MainThread
fun Context.showProgressDialog(message: CharSequence = "加载中", holder: Any? = null) {
    ProgressDialogRunnable.tryShow(this, message, holder)
}

fun Fragment.showProgressDialog(message: CharSequence = "请稍等..") {
    context?.showProgressDialog(message, this)
}

fun View.showProgressDialog(message: CharSequence = "请稍等..", holder: Any? = null) {
    context.showProgressDialog(message, holder ?: this)
}

/**暂时有问题，尽量不使用*/
fun Context.showSuccessTip(message: CharSequence = "成功") {
    ProgressDialogRunnable.tryShowSuccess(this, message)
}

/**暂时有问题，尽量不使用*/
fun View.showSuccessTip(message: CharSequence = "成功") {
    context.showSuccessTip(message)
}

/**暂时有问题，尽量不使用*/
fun Fragment.showSuccessTip(message: CharSequence = "成功") {
    context?.showSuccessTip(message)
}

/**暂时有问题，尽量不使用*/
fun Context.showErrorTip(message: CharSequence = "失败") {
    ProgressDialogRunnable.tryShowError(this, message)
}

/**暂时有问题，尽量不使用*/
fun View.showErrorTip(message: CharSequence = "失败") {
    context.showErrorTip(message)
}

/**暂时有问题，尽量不使用*/
fun Fragment.showErrorTip(message: CharSequence = "失败") {
    context?.showErrorTip(message)
}

fun Context.dismissProgressDialog(immediate: Boolean = false) {
    ProgressDialogRunnable.tryDismiss(immediate)
}

fun Fragment.dismissProgressDialog(immediate: Boolean = false) {
    context?.dismissProgressDialog(immediate)
}

fun View.dismissProgressDialog(immediate: Boolean = false) {
    context?.dismissProgressDialog(immediate)
}

/**展示一个dialog*/
fun Context.showPerformDialog(title: String, msg: String? = null, leftBtnStr: String? = "取消", rightBtnStr: String? = "确认",
                              leftBtnListener: (() -> Boolean)? = null,
                              rightBtnListener: (() -> Boolean)? = null) {

    val contentView = LayoutInflater.from(this).inflate(R.layout.mn_dialog_layout_center_dialog, null)
    if (isEmpty(msg)) {
        contentView.findViewById<View>(R.id.text_dialog_desc).visibility = View.GONE
    } else {
        contentView.findViewById<TextView>(R.id.text_dialog_desc).text = msg
    }
    val mTxtCancel = contentView.findViewById<TextView>(R.id.btn_dialog_cancle)
    val mViewDivider = contentView.findViewById<View>(R.id.mViewDivider)
    if (isEmpty(leftBtnStr)) {
        mTxtCancel.visibility = View.GONE
        mViewDivider.visibility = View.GONE
    }
    mTxtCancel.text = leftBtnStr

    val mTxtSubmit = contentView.findViewById<TextView>(R.id.btn_dialog_confirm)
    if (isEmpty(rightBtnStr)) {
        mTxtSubmit.visibility = View.GONE
        mViewDivider.visibility = View.GONE
    }
    mTxtSubmit.text = rightBtnStr

    contentView.findViewById<TextView>(R.id.text_dialog_title).text = title

    val canCancel = mTxtCancel.visibility != View.VISIBLE && mTxtSubmit.visibility != View.VISIBLE

    val dialog = MNDialog(MNDialogConfig.Builder()
            .setContext(this)
            .setGravity(Gravity.CENTER)
            .setContentView(contentView)
            .setCancel(canCancel)
            .setThemeResId(R.style.MNDialogStyle)
            .build())

    contentView.findViewById<View>(R.id.btn_dialog_cancle).setOnClickListener {
        if (leftBtnListener != null && leftBtnListener()) {
            return@setOnClickListener
        }
        dialog.dismiss()
    }
    contentView.findViewById<View>(R.id.btn_dialog_confirm).setOnClickListener {
        if (rightBtnListener != null && rightBtnListener()) {
            return@setOnClickListener
        }
        dialog.dismiss()
    }
    dialog.show()
}

/**展示一个dialog*/
fun BaseFragment.showPerformDialog(title: String, msg: String? = null, leftBtnStr: String? = "取消", rightBtnStr: String? = "确认",
                                   leftBtnListener: (() -> Boolean)? = null,
                                   rightBtnListener: (() -> Boolean)? = null) {
    pageActivity?.showPerformDialog(title, msg, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener)
}

/**展示一个dialog*/
fun View.showPerformDialog(title: String, msg: String? = null, leftBtnStr: String? = "取消", rightBtnStr: String? = "确认",
                           leftBtnListener: (() -> Boolean)? = null,
                           rightBtnListener: (() -> Boolean)? = null) {
    context?.showPerformDialog(title, msg, leftBtnStr, rightBtnStr, leftBtnListener, rightBtnListener)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun BaseFragment.showDialog(@LayoutRes layoutId: Int, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): Pair<View?, MNDialog?> {
    if (isFinishing()) return Pair(null, null)
    val cxt: Context? = context
    cxt ?: return Pair(null, null)
    val inflating = inflating(layoutId)
    val showDialog = cxt.showDialog(inflating, dismissOutside, onDismiss)
    return Pair(inflating, showDialog)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun BaseFragment.showDialog(contentView: View, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): MNDialog? {
    if (isFinishing()) return null
    val cxt: Context? = context
    cxt ?: return null
    return cxt.showDialog(contentView, dismissOutside, onDismiss)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun View.showDialog(@LayoutRes layoutId: Int, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): Pair<View?, MNDialog?> {
    val cxt: Context? = context
    cxt ?: return Pair(null, null)
    val inflating = inflating(layoutId)
    val showDialog = cxt.showDialog(inflating, dismissOutside, onDismiss)
    return Pair(inflating, showDialog)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun View.showDialog(contentView: View, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): MNDialog? {
    val cxt: Context? = context
    cxt ?: return null
    return cxt.showDialog(contentView, dismissOutside, onDismiss)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun Context.showDialog(@LayoutRes layoutId: Int, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): Pair<View, MNDialog> {
    val inflating = inflating(layoutId)
    val showDialog = showDialog(inflating, dismissOutside, onDismiss)
    return Pair(inflating, showDialog)
}

/**显示一个中间的Dialog，不带任何通用view*/
@JvmOverloads
fun Context.showDialog(contentView: View, dismissOutside: Boolean = true, onDismiss: SuccessLambda<MNDialog> = null): MNDialog {
    val dialog = MNDialog(MNDialogConfig.Builder()
            .setContext(this)
            .setGravity(Gravity.CENTER)
            .setContentView(contentView)
            .setCancel(dismissOutside)
            .setThemeResId(R.style.MNDialogStyle)
            .build())
    if (onDismiss != null) {
        dialog.setOnDismissListener(object : MNDialog.OnDismissListener {
            override fun onDismiss() {
                onDismiss.invoke(dialog)
            }
        })
    }
    dialog.show()
    return dialog
}

fun Context.showActionSheet(contentView: View, title: String = "", leftBtnStr: String? = null, rightBtnStr: String? = null,
                            leftBtnListener: (() -> Boolean)? = null,
                            rightBtnListener: (() -> Boolean)? = null, autoShow: Boolean = true): MNDialog {

    val rootView = inflating(R.layout.moon_sdk_app_dialog_action_sheet) as LinearLayout

    val mTxtCancel = rootView.findViewById<TextView>(R.id.mTxtDialogCancel)
    val mTxtSubmit = rootView.findViewById<TextView>(R.id.mTxtDialogSubmit)
    val mTxtTitle = rootView.findViewById<TextView>(R.id.mTxtDialogTitle)
    if (!isEmpty(leftBtnStr)) {
        mTxtCancel.text = leftBtnStr
    }
    if (!isEmpty(rightBtnStr)) {
        mTxtSubmit.text = leftBtnStr
    }
    mTxtTitle.text = title

    rootView.addView(contentView)
    val dialog = MNDialog(MNDialogConfig.Builder()
            .setContext(this)
            .setGravity(Gravity.BOTTOM)
            .setContentView(rootView)
            .setCancel(false)
            .setThemeResId(R.style.MNDialogStyle)
            .build())


    mTxtCancel.onClick {
        if (leftBtnListener?.invoke() != true) {
            dialog.dismiss()
        }
    }

    mTxtSubmit.onClick {
        if (rightBtnListener?.invoke() != true) {
            dialog.dismiss()
        }
    }

    if (autoShow) {
        dialog.show()
    }
    return dialog
}

fun Context.showActionSheet(autoShow: Boolean = true): ActionBuilder {
    return ActionBuilder(autoShow) { list, builder ->
        var txtCancel: View? = null
        var mAction1: View? = null
        var mAction2: View? = null
        var mTxtAction1: TextView? = null
        var mTxtAction2: TextView? = null
        var mTxtAction1Desc: TextView? = null
        var mTxtAction2Desc: TextView? = null

        val contentView = if (builder.isDescType) {
            val view = inflating(R.layout.mn_dialog_layout_bottom_dialog_by_desc)
            mTxtAction1 = view.findViewById(R.id.mTxtAction1)
            mTxtAction2 = view.findViewById(R.id.mTxtAction2)
            mTxtAction1Desc = view.findViewById(R.id.mTxtActionDesc1)
            mTxtAction2Desc = view.findViewById(R.id.mTxtActionDesc2)
            mAction1 = view.findViewById(R.id.action1)
            mAction2 = view.findViewById(R.id.action2)
            view
        } else {
            val view = inflating(R.layout.mn_dialog_layout_bottom_dialog)
            mTxtAction1 = view.findViewById(R.id.action1)
            mTxtAction2 = view.findViewById(R.id.action2)
            mAction1 = mTxtAction1
            mAction2 = mTxtAction2
            view
        }

        txtCancel = contentView.findViewById(R.id.btn_dialog_cancle)

        val dialog = MNDialog(MNDialogConfig.Builder()
                .setContext(this)
                .setGravity(Gravity.BOTTOM)
                .setContentView(contentView)
                .setCancel(false)
                .setThemeResId(R.style.MNDialogStyle)
                .build())

        txtCancel?.onClick { dialog.dismiss() }


        list.forEachIndexed { index, action ->
            when (index) {
                0 -> {
                    mTxtAction1?.text = action.actionName
                    mTxtAction1Desc?.text = action.actionDesc
                    mAction1?.onClick {
                        if (action.action?.invoke() != false) {
                            return@onClick
                        }
                        dialog.dismiss()
                    }
                }
                1 -> {
                    mTxtAction2?.text = action.actionName
                    mTxtAction2Desc?.text = action.actionDesc
                    mAction2?.setOnClickListener {
                        if (action.action?.invoke() != false) {
                            return@setOnClickListener
                        }
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }
}

fun View.showActionSheet(autoShow: Boolean = true): ActionBuilder {
    return context.showActionSheet(autoShow)
}

fun PageFragment.showActionSheet(autoShow: Boolean = true): ActionBuilder {
    return pageActivity!!.showActionSheet(autoShow)
}


fun Context.showActionSheet(contentView: View): MNDialog {
    val dialog = MNDialog(this, contentView)
    dialog.showActionSheet()
    return dialog
}

fun Context.showActionSheet(contentView: View, animation: Int): MNDialog {
    val dialog = MNDialog(this, contentView)
            .setGravity(Gravity.BOTTOM)
            .setAnimation(animation)
            .setCancel(true)
    dialog.show()
    return dialog
}

fun View.showActionSheet(contentView: View): MNDialog {
    return context.showActionSheet(contentView)
}

fun PageFragment.showActionSheet(contentView: View): MNDialog {
    return pageActivity!!.showActionSheet(contentView)
}

/**显示一个滚轮的dialog*/
fun PageFragment.showActionWheelByIndex(data: List<IKeyValueModel>, selectIndex: Int = -1, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
    return pageActivity!!.showActionWheelByIndex(data, selectIndex, onSubmit)
}

/**显示一个滚轮的dialog*/
fun View.showActionWheelByIndex(data: List<IKeyValueModel>, selectIndex: Int = -1, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
    return context.showActionWheelByIndex(data, selectIndex, onSubmit)
}

/**显示一个滚轮的dialog*/
fun Context.showActionWheelByIndex(data: List<IKeyValueModel>, selectIndex: Int = -1, onSubmit: (index: Int, model: IKeyValueModel) -> Unit, title: CharSequence = ""): MNDialog {

    val singleView = LayoutInflater.from(this).inflate(R.layout.dialog_single_picker, null)
    val singleChoose = this.showActionSheet(singleView)
    if (data.isEmpty()) {
        MNToast.showCenter(this, "数据为空")
        return singleChoose
    }
    val singlePickerTitle = singleView.findViewById<TextView>(R.id.singlePickerTitle)
    if (!isEmpty(title)) {
        singlePickerTitle.text = "请选择$title"
        singlePickerTitle.visibility = View.VISIBLE
    }
    val wheelViewSingle = singleView.findViewById<WheelView>(R.id.wheelViewSingle)
    wheelViewSingle.adapter = KeyValueWheelAdapter(data)
    wheelViewSingle.isCyclic = false
    wheelViewSingle.currentItem = if (selectIndex == -1) 0 else selectIndex
    if (data.size >= 7) {
        wheelViewSingle.visibleItems = 7
    } else {
        wheelViewSingle.visibleItems = data.size
        val layoutParams = RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        when (data.size) {
            1 -> {
                layoutParams.height = 41.px
                layoutParams.setMargins(0, 124.px, 0, 124.px)
            }
            2, 3 -> {
                layoutParams.height = 123.px
                layoutParams.setMargins(0, 82.px, 0, 82.px)
            }
            4, 5 -> {
                layoutParams.height = 205.px
                layoutParams.setMargins(0, 41.px, 0, 41.px)
            }
            6 -> {
                layoutParams.height = 289.px
                layoutParams.setMargins(0, 0, 0, 0)
            }
        }

        wheelViewSingle.layoutParams = layoutParams
    }

    val singlePickerCancel = singleView.findViewById<TextView>(R.id.singlePickerCancel)
    singlePickerCancel.setOnClickListener { singleChoose.dismiss() }

    val singlePickerConfirm = singleView.findViewById<TextView>(R.id.singlePickerConfirm)
    singlePickerConfirm.setOnClickListener {
        if (!isEmpty(data)) {
            onSubmit(wheelViewSingle.currentItem, data[wheelViewSingle.currentItem])
        }
        singleChoose.dismiss()
    }

    return singleChoose
}

/**显示一个滚轮的dialog*/
fun PageFragment.showActionWheel(data: List<IKeyValueModel>, id: String? = null, title: CharSequence = "", onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
    return pageActivity!!.showActionWheel(data, id, onSubmit, title)
}

/**显示一个滚轮的dialog*/
fun View.showActionWheel(data: List<IKeyValueModel>, id: String? = null, onSubmit: (index: Int, model: IKeyValueModel) -> Unit): MNDialog {
    return context.showActionWheel(data, id, onSubmit)
}

/**显示一个滚轮的dialog*/
fun Context.showActionWheel(data: List<IKeyValueModel>, id: String? = null, onSubmit: (index: Int, model: IKeyValueModel) -> Unit, title: CharSequence = ""): MNDialog {
    var position = -1
    if (id != null) {
        data.forEachIndexed { index, model ->
            if (model.getKeyValueId() == id) {
                position = index
                return@forEachIndexed
            }
        }
    }
    return this.showActionWheelByIndex(data, position, onSubmit, title)
}

fun Context.showChoiceDialog(title: CharSequence, items: Array<IKeyValueModel>, delayDismiss: Long = 300, dismissListener: (() -> Unit)? = null, listener: ((Int) -> Boolean)? = null): MNDialog {
    val view = inflating(R.layout.moon_sdk_app_dialog_choice_with_title)
    val dialog = MNDialog(MNDialogConfig.Builder()
            .setContext(this)
            .setGravity(Gravity.CENTER)
            .setContentView(view)
            .setCancel(true)
            .setThemeResId(R.style.MNDialogStyle)
            .build())

    val mRecyclerDialog = view.findViewById<RecyclerView>(R.id.mRecyclerDialog)

    view.findViewById<View>(R.id.mImgDialogClose).setOnClickListener { dialog.dismiss() }
    view.findViewById<TextView>(R.id.mTxtDialogTitle).text = title

    val mLayoutDialogContainer = view.findViewById<LinearLayout>(R.id.mLayoutDialogContainer)

    if (items.size <= 5) {
        mLayoutDialogContainer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
    } else {
        mLayoutDialogContainer.layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 293.px)
    }

    mRecyclerDialog.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

    val adapter = ChoiceAdapter(this, items.toList())
    adapter.onItemClickLambda = { index ->
        if (listener == null || !listener(index)) {
            Sdk.task().post(delayDismiss) { dialog.dismiss() }
        }
    }
    mRecyclerDialog.adapter = adapter
    if (dismissListener != null) {
        dialog.setOnDismissListener(object : MNDialog.OnDismissListener {
            override fun onDismiss() {
                dismissListener.invoke()
            }
        })
    }
    dialog.show()
    return dialog
}

fun BaseFragment.showChoiceDialog(title: CharSequence, items: Array<String>, delayDismiss: Long = 300, listener: ((Int) -> Boolean)? = null): MNDialog? {

    val keyValueList = items.mapIndexed { index, s -> KeyValueModel(s, index.toString()) }

    return pageActivity?.showChoiceDialog(title, keyValueList, delayDismiss) superMethod@{
        return@superMethod listener?.invoke(keyValueList.indexOf(it)) ?: false
    }
}

fun View.showChoiceDialog(title: CharSequence, items: Array<String>, delayDismiss: Long = 300, listener: ((Int) -> Boolean)? = null): MNDialog? {
    val keyValueList = items.mapIndexed { index, s -> KeyValueModel(s, index.toString()) }

    return context?.showChoiceDialog(title, keyValueList, delayDismiss) superMethod@{
        return@superMethod listener?.invoke(keyValueList.indexOf(it)) ?: false
    }
}

fun Context.showChoiceDialog(title: CharSequence, items: List<IKeyValueModel>, delayDismiss: Long = 300, dismissListener: (() -> Unit)? = null, listener: ((IKeyValueModel) -> Boolean)? = null): MNDialog {

    return showChoiceDialog(title, items.toTypedArray(), delayDismiss, dismissListener) superMethod@{
        return@superMethod listener?.invoke(items[it]) ?: false
    }
}

fun BaseFragment.showChoiceDialog(title: CharSequence, items: List<IKeyValueModel>, delayDismiss: Long = 300, listener: ((IKeyValueModel) -> Boolean)? = null): MNDialog? {
    return pageActivity?.showChoiceDialog(title, items, delayDismiss, null, listener)
}

fun View.showChoiceDialog(title: CharSequence, items: List<IKeyValueModel>, delayDismiss: Long = 300, listener: ((IKeyValueModel) -> Boolean)? = null): MNDialog? {
    return context?.showChoiceDialog(title, items, delayDismiss, null, listener)
}

fun Context.showDateDialog(selectTime: Long = 0, listener: ((year: Int, month: Int, day: Int, time: Long) -> Unit)) {
    val view = inflating(R.layout.moon_sdk_app_date_pick)
    val dialog = MNDialog(this, view)
    val mDatePicker = view.findViewById<DatePicker>(R.id.mDatePicker)
    if (selectTime > 0) {
        mDatePicker.setDate(selectTime)
    }
    view.findViewById<View>(R.id.mBtnDatePickerCancel).setOnClickListener { dialog.dismiss() }
    view.findViewById<View>(R.id.mBtnDatePickerPerform).setOnClickListener {
        listener.invoke(mDatePicker.year, mDatePicker.rawMonth, mDatePicker.day, mDatePicker.longTime)
        dialog.dismiss()
    }

    dialog.showActionSheet()
}

fun Context.showDateDialogWithoutDay(selectTime: Long = 0, startTime: Long = 0, endTime: Long = 0, listener: ((year: Int, month: Int, time: Long) -> Unit)) {
    val view = inflating(R.layout.moon_sdk_app_date_pick_without_day)
    val dialog = MNDialog(this, view)
    val mDatePicker = view.findViewById<DatePickerWithoutDay>(R.id.mDatePicker)
    if (endTime > 0) {
        mDatePicker.setEndData(endTime)
    }
    if (startTime > 0) {
        mDatePicker.setStartData(startTime)
    }
    if (selectTime > 0) {
        mDatePicker.setDate(selectTime)
    }

    view.findViewById<View>(R.id.mBtnDatePickerPerform).setOnClickListener {
        listener.invoke(mDatePicker.year, mDatePicker.month, mDatePicker.longTime)
        dialog.dismiss()
    }

    dialog.showActionSheet()
}

fun View.showDateDialogWithoutDay(selectTime: Long = 0, startTime: Long = 0, endTime: Long = 0, listener: ((year: Int, month: Int, time: Long) -> Unit)) {
    context?.showDateDialogWithoutDay(selectTime, startTime, endTime, listener)
}

fun BaseFragment.showDateDialogWithoutDay(selectTime: Long = 0, startTime: Long = 0, endTime: Long = 0, listener: ((year: Int, month: Int, time: Long) -> Unit)) {
    context?.showDateDialogWithoutDay(selectTime, startTime, endTime, listener)
}

fun BaseFragment.showDateDialog(selectTime: Long = 0, listener: ((year: Int, month: Int, day: Int, time: Long) -> Unit)) {
    pageActivity?.showDateDialog(selectTime, listener)
}

fun View.showDateDialog(selectTime: Long = 0, listener: ((year: Int, month: Int, day: Int, time: Long) -> Unit)) {
    context?.showDateDialog(selectTime, listener)
}

/**根据param入参，判断显示错误ui的类型*/
fun PageToolsLayout?.showErrors(param: IRequestParams.IHttpRequestParams? = null) {
    this ?: return

    if (param == null) {
        showError()
        return
    }

    if (param.response.state == -1) {
        showError()
        return
    }

    showServerError(param.response.message)
}

class ActionBuilder(private val autoShow: Boolean, private val onActionEnd: (MutableList<ActionInnerModel>, ActionBuilder) -> Unit) : Runnable {

    private val actions = mutableListOf<ActionInnerModel>()
    var isDescType = false

    fun addAction(action: String, actionDesc: String? = null, callback: (() -> Boolean)?): ActionBuilder {
        if (!isDescType) {
            isDescType = !isEmpty(actionDesc)
        }
        actions.add(ActionInnerModel(action, actionDesc, callback))
        if (autoShow) {
            Sdk.task().removeCallbacks(this)
            Sdk.task().post(this, 50)
        }
        return this
    }

    fun clearActions() {
        actions.clear()
    }

    override fun run() {
        show()
    }

    fun show(): ActionBuilder {
        onActionEnd(actions, this)
        return this
    }
}


class ChoiceAdapter(private val context: Context, private val data: List<IKeyValueModel>)
    : RecyclerView.Adapter<ChoiceAdapter.ViewHolder>() {

    private val onItemClick: (View) -> Unit = {
        for (agencyModelItem in data) {
            agencyModelItem.isKeyValueSelected = false
        }
        val agencyModel = it.tag as? IKeyValueModel
        if (agencyModel != null) {
            agencyModel.isKeyValueSelected = true
            val indexOf = data.indexOf(agencyModel)
            notifyDataSetChanged()
            onItemClickLambda(indexOf)
        }
    }

    var onItemClickLambda: (index: Int) -> Unit = { _ -> }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflating(R.layout.moon_sdk_app_dialog_choice_item, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = data[position]
        holder.mTxtDialogItemTitle.text = model.getKeyValueName()
        if (model.isKeyValueSelected) {
            holder.mImgDialogItemCheck.visibility = View.VISIBLE
            holder.mTxtDialogItemTitle.setTextColor(Color.parseColor("#ffb400"))
        } else {
            holder.mImgDialogItemCheck.visibility = View.GONE
            holder.mTxtDialogItemTitle.setTextColor(Color.parseColor("#545353"))
        }

        holder.itemView.setOnClickListener(onItemClick)
        holder.itemView.tag = model
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var mTxtDialogItemTitle: TextView = itemView.findViewById(R.id.mTxtDialogItemTitle)
        var mImgDialogItemCheck: ImageView = itemView.findViewById(R.id.mImgDialogItemCheck)
    }
}

class ActionInnerModel(var actionName: String = "", var actionDesc: String? = null, var action: (() -> Boolean)? = null)