package com.meili.moon.sdk.app.widget.group

import android.content.Context
import android.graphics.Color
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import com.meili.moon.sdk.app.R

class MNImageTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        ConstraintLayout(context, attrs, defStyleAttr) {

    private var imageView: ImageView? = null
    private var textView: TextView? = null

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        LayoutInflater.from(context).inflate(R.layout.moon_sdk_app_group_imagetextview, this)
        imageView = findViewById(R.id.mImage)
        textView = findViewById(R.id.mText)
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.MNImageView)
            val imageIcon = typedArray.getResourceId(R.styleable.MNImageView_imageIcon, -1)
            setIcon(imageIcon)
            val imageText = typedArray.getString(R.styleable.MNImageView_imageText)
            setText(imageText)
            val color = typedArray.getColor(R.styleable.MNImageView_imageTextColor, Color.BLACK)
            setTextColor(color)
            val dimension = typedArray.getDimension(R.styleable.MNImageView_imageTextSize, 14f).toInt()
            setTextSize(dimension)
            typedArray.recycle()
        }
    }

    fun setIcon(icon: Int) {
        if (icon != -1) {
            imageView?.setImageResource(icon)
        }
    }

    fun setText(text: String?) {
        textView?.text = text
    }

    fun setTextColor(textColor: Int) {
        textView?.setTextColor(textColor)
    }

    fun setTextSize(textSize: Int) {
        textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
    }
}
