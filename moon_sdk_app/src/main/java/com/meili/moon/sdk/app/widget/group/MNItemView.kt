package com.meili.moon.sdk.app.widget.group

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.meili.moon.sdk.app.R


/**
 * Created by zhengshuai on 2018/5/22.
 */
class MNItemView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ConstraintLayout(context, attrs, defStyleAttr) {


    private var mIcon: ImageView? = null
    private var mArrow: ImageView? = null
    private var mTxt: TextView? = null
    private var mDescribeTxt: TextView? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_group_item, this)
        mIcon = findViewById(R.id.mIcon)
        mTxt = findViewById(R.id.mTxt)
        mDescribeTxt = findViewById(R.id.mDescribeTxt)
        mArrow = findViewById(R.id.mArrow)
        if (attrs != null) {
            @SuppressLint("Recycle")
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MNItemView)
            val icon = typedArray.getResourceId(R.styleable.MNItemView_itemIcon, -1)
            setIcon(icon)
            val arrow = typedArray.getResourceId(R.styleable.MNItemView_itemArrow, -1)
            setArrow(arrow)

            val string = typedArray.getString(R.styleable.MNItemView_itemTxt)
            setTxt(string)
            val txtColor = typedArray.getColor(R.styleable.MNItemView_itemTxtColor, Color.BLACK)
            setTxtColor(txtColor)
            val txtSize = typedArray.getDimension(R.styleable.MNItemView_itemTxtSize, 16f).toInt()
            setTxtSize(txtSize)


            val describeTxt = typedArray.getString(R.styleable.MNItemView_itemDescribeTxt)
            setDescribeTxt(describeTxt)
            val describeTxtColor = typedArray.getColor(R.styleable.MNItemView_itemDescribeTxtColor, resources.getColor(R.color.normal_content))
            setDescribeTxtColor(describeTxtColor)
            val describeTxtSize = typedArray.getDimension(R.styleable.MNItemView_itemDescribeTxtSize, 14f).toInt()
            setDescribeTxtSize(describeTxtSize)

        }

    }


    /**
     * 设置icon资源
     *
     * @param drawable
     */
    fun setIcon(drawable: Int) {
        if (drawable == -1) {
            mIcon?.visibility = View.GONE
        } else {
            mIcon?.visibility = View.VISIBLE
            mIcon?.setImageResource(drawable)
        }

    }


    /**
     * 设置箭头资源
     *
     * @param arrow
     */
    fun setArrow(arrow: Int) {
        if (arrow == -1) {
            mArrow!!.visibility = View.GONE
        } else {
            mArrow!!.visibility = View.VISIBLE
            mArrow!!.setImageResource(arrow)
        }
    }

    /**
     * 设置字体大小
     *
     * @param txtSize
     */
    fun setTxtSize(txtSize: Int) {
        mTxt?.setTextSize(TypedValue.COMPLEX_UNIT_PX, txtSize.toFloat())
    }


    /**
     * 设置字体颜色
     *
     * @param txtColor
     */
    fun setTxtColor(txtColor: Int) {
        mTxt?.setTextColor(txtColor)
    }

    /**
     * 设置文字内容
     *
     * @param string
     */
    fun setTxt(string: String?) {
        mTxt?.text = if (TextUtils.isEmpty(string)) "" else string
    }

    /**
     * 设置箭头右侧描述文案
     *
     * @param describeTxt
     */
    fun setDescribeTxt(describeTxt: CharSequence?) {
        mDescribeTxt?.visibility = if (TextUtils.isEmpty(describeTxt)) View.GONE else View.VISIBLE
        mDescribeTxt?.text = describeTxt
    }

    /**
     * 设置箭头右侧描述文案颜色
     *
     * @param describeTxtColor
     */
    fun setDescribeTxtColor(describeTxtColor: Int) {
        mDescribeTxt?.setTextColor(describeTxtColor)
    }

    /**
     * 设置箭头右侧描述文案字号
     *
     * @param describeTxtSize
     */
    fun setDescribeTxtSize(describeTxtSize: Int) {
        mDescribeTxt?.setTextSize(TypedValue.COMPLEX_UNIT_PX, describeTxtSize.toFloat())
    }
}
