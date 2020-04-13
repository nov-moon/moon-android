package com.meili.moon.sdk.app.widget.picker

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.meili.moon.sdk.app.R
import com.meili.moon.sdk.app.base.KeyValueModel
import com.meili.moon.sdk.base.util.onClick
import com.meili.moon.sdk.util.isEmpty
import com.meili.moon.sdk.util.largerSize
import kotlinx.android.synthetic.main.moon_sdk_app_wheel_picker.view.*

/**
 * Created by imuto on 2018/7/9.
 */
class DataPickerView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val mData = mutableListOf<KeyValueModel>()

    var onItemSelectedListener: ((KeyValueModel) -> Unit)? = null

    /**取消按钮的回调*/
    var onCancelClick: ((View) -> Unit)? = null
        set(value) {
            field = value
            mTxtCancel.onClick(null, field)
        }

    /**确定按钮的回调*/
    var onSubmitClick: ((View) -> Unit)? = null
        set(value) {
            field = value
            mTxtSubmit.onClick(null, field)
        }

    init {
        inflate(context, R.layout.moon_sdk_app_wheel_picker, this)

        mNumberPicker.setOnValueChangeListener { _, _, newVal ->
            val index = convertIndex(newVal)
            val model = mData[index]
            onItemSelectedListener?.invoke(model)
        }
    }

    /**设置数据*/
    fun setData(data: List<KeyValueModel>) {
        mData.clear()
        mData.addAll(data)
        val maped = mData.map { it.name }.toMutableList()
        ensureSize(maped)
        mNumberPicker.set(0, maped.size - 1, 2)
        mNumberPicker.setCustomTextArray(maped.toTypedArray())
    }

    private fun ensureSize(maped: MutableList<String>) {
        if (maped.isEmpty()) {
            return
        }
        if (maped.size < 7) {
            maped.addAll(maped)
            ensureSize(maped)
        }
    }

    /**选中指定index的item*/
    fun select(index: Int) {
        if (isEmpty(mData)) return
        if (!largerSize(mData, index)) return

        mNumberPicker.currentNumber = index
    }

    fun select(model: KeyValueModel) {
        select(mData.indexOf(model))
    }

    /**获取选中的item*/
    fun getSelect(): KeyValueModel {
        return mData[convertIndex(mNumberPicker.currentNumber)]
    }

    /**获取选中的item的index*/
    fun getSelectIndex(): Int {
        return convertIndex(mNumberPicker.currentNumber)
    }

    fun showPickOnly() {
        mTxtCancel.visibility = View.GONE
        mTxtSubmit.visibility = View.GONE
        mViewTopLabel.visibility = View.GONE
    }

    private fun convertIndex(numIndex: Int): Int {
        val size = mData.size
        val position = numIndex + 1
        val index = position % size - 1
        return if (index < 0) {
            index + size
        } else index
    }

}