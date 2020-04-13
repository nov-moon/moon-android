package com.meili.moon.sdk.page.titlebar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.annotation.StyleRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.meili.moon.sdk.base.util.pxF
import com.meili.moon.sdk.page.R
import com.meili.moon.sdk.page.internal.utils.TranslucentStatusBarUtils
import com.meili.moon.sdk.util.statusBarHeight
import kotlinx.android.synthetic.main.rainbow_titlebar_layout.view.*

/**
 * Created by imuto on 2019-08-14.
 */
@SuppressLint("Recycle")
class RainbowTitleBarView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr), ITitleBarView.ImplView {

    override var titleBarVisibility: Int
        get() = visibility
        set(value) {
            visibility = value
        }

    private var mMenuTextColor: Int = 0

    override var pageAnimatorFlag = 0

    private var onDoubleClickListener: (() -> Unit)? = null

    /**
     * 是否已经初始化view
     */
    private var hasInitView = false

    private var mPageAnimatorEnable = true

    /**
     * use [.getButtonBackground],
     */
    @Deprecated("")
    private var mButtonBackground: Drawable? = null

    private var STANDARD_HEIGHT: Int = 0

    private val onTitleClickListener = object : OnClickListener {
        private var mLastTitleClick: Long = 0
        private val maxDuration = ViewConfiguration.getDoubleTapTimeout().toLong()

        override fun onClick(v: View) {
            val currTime = System.currentTimeMillis()
            synchronized(this) {
                if (currTime - mLastTitleClick < maxDuration) {
                    mLastTitleClick = 0
                    onDoubleClickListener?.invoke()
                } else {
                    mLastTitleClick = currTime
                }
            }
        }
    }

    init {
        if (!isInEditMode) {
            mButtonBackground = context.resources.getDrawable(R.drawable.rainbow_titlebar_item_bg)
        }
        context.obtainStyledAttributes(null, R.styleable.RainbowTitleBarView, R.attr.rainbowTitleBarView, 0)
                .initView().recycle()
    }

    private fun TypedArray.initView(): TypedArray {
        if (!hasInitView) {
            STANDARD_HEIGHT = context.resources.getDimensionPixelSize(R.dimen.rainbow_title_bar_height)
            hasInitView = true
            View.inflate(context, R.layout.rainbow_titlebar_layout, this@RainbowTitleBarView)
        }

        var titlePaddingTop = getDimension(R.styleable.RainbowTitleBarView_rainbowTitleBarPaddingTop, resources.getDimension(R.dimen.rainbow_title_bar_translucent)).toInt()
        val titleHomeMinWidth = getDimension(R.styleable.RainbowTitleBarView_rainbowTitleBarBackMinWidth, resources.getDimension(R.dimen.rainbow_title_bar_button)).toInt()
        var backBg = getDrawable(R.styleable.RainbowTitleBarView_rainbowTitleBarBackBg)
        if (backBg == null) {
            backBg = resources.getDrawable(R.drawable.rainbow_titlebar_item_bg)
        }
        var backDrawableRes = getDrawable(R.styleable.RainbowTitleBarView_rainbowTitleBarBackSrc)
        if (backDrawableRes == null) {
            backDrawableRes = resources.getDrawable(R.drawable.rainbow_title_bar_home_back)
        }

        var backTextColor = getColorStateList(R.styleable.RainbowTitleBarView_rainbowTitleBarBackTxtColor)
        if (backTextColor == null) {
            backTextColor = resources.getColorStateList(R.color.rainbow_titlebar_menu_text_color)
        }

        val homeTextSize = getDimension(R.styleable.RainbowTitleBarView_rainbowTitleBarBackTxtSize, 14.pxF).toInt()
        val homeText = getString(R.styleable.RainbowTitleBarView_rainbowTitleBarBackTxt)

        val titleColor = getColor(R.styleable.RainbowTitleBarView_rainbowTitleBarTitleColor, resources.getColor(R.color.rainbow_title_bar_menu_text_normal))
        val titleSize = getDimension(R.styleable.RainbowTitleBarView_rainbowTitleBarTitleSize, resources.getDimension(R.dimen.rainbow_title_bar_title_text)).toInt()


        var bgDrawable = getDrawable(R.styleable.RainbowTitleBarView_rainbowTitleBarBackground)
        if (bgDrawable == null) {
            bgDrawable = resources.getDrawable(R.drawable.rainbow_titlebar_bg)
        }

        mMenuTextColor = getColor(R.styleable.RainbowTitleBarView_rainbowTitleBarMenuTextColor, Color.BLACK)

        descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS

        mLayoutBack.minimumWidth = titleHomeMinWidth
        mLayoutBack.setBackgroundDrawable(backBg)
        mImgBack.setImageDrawable(backDrawableRes)
        mTxtBack.setTextColor(backTextColor)
        mTxtBack.setTextSize(TypedValue.COMPLEX_UNIT_PX, homeTextSize.toFloat())
        mTxtBack.text = homeText

        mTxtTitle.setTextColor(titleColor)
        mTxtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())

        setBackgroundDrawable(bgDrawable)

        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            titlePaddingTop += statusBarHeight
        }

        setPadding(paddingLeft, titlePaddingTop, paddingRight, paddingBottom)

        setOnTouchListener { _, _ -> true }

        return this
    }


    override fun setBackClickListener(lis: () -> Unit) {
        mLayoutBack.setOnClickListener {
            lis.invoke()
        }
    }

    override fun setCloseMenuClickListener(lis: () -> Unit) {
        mH5Close.setOnClickListener {
            lis.invoke()
        }
    }

    override fun setCloseMenuVisible(visible: Int) {
        mH5Close.visibility = visible
    }

    override fun setOnTitleDoubleClickListener(lis: () -> Unit) {
        if (lis == null) {
            return
        }
        onDoubleClickListener = lis
        mTxtTitle.setOnClickListener(onTitleClickListener)
    }

    override fun getTitleBarHeight(): Int {
        return context.resources.getDimensionPixelSize(R.dimen.rainbow_title_bar_height) + statusBarHeight
    }

    override fun setTitle(text: CharSequence) {
        mTxtTitle.text = text
    }

    override fun setTitle(resId: Int) {
        setTitle(resources.getText(resId))
    }

    override fun setTitleTextColor(color: Int) {
        mTxtTitle.setTextColor(color)
    }

    override fun setBackIcon(resId: Int) {
        setBackIcon(resources.getDrawable(resId))
    }

    override fun setBackIcon(drawable: Drawable) {
        mImgBack.setImageDrawable(drawable)
    }

    override fun setBackIconVisible(visible: Int) {
        mImgBack.visibility = visible
    }

    override fun setBackText(resId: Int) {
        setBackText(resources.getText(resId))
    }

    override fun setBackText(text: CharSequence) {
        mTxtBack.setText(text)
    }

    override fun setBackTextColor(resId: Int) {
        mTxtBack.setTextColor(resId)
    }

    override fun setTitleBarBackgroundColor(color: Int) {
        setBackgroundColor(color)
    }

    override fun setTitleBarBackgroundDrawable(resId: Int) {
        setBackgroundResource(resId)
    }

    override fun removeMenu(menu: Menu) {
        val view = menu.rootView
        if (view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
        mViewMenuBar.removeView(view)
    }

    override fun addMenu(menu: Menu) {
        val view = menu.rootView
        if (view.parent != null) {
            (view.parent as ViewGroup).removeView(view)
        }
        initMenuLayoutParams(view)
        if (menu is MenuText) {
            menu.setTextColor(mMenuTextColor)
        }

        mViewMenuBar.addView(view)
    }

    override fun setSupportNoNetworkStyle(isSupport: Boolean) {
        // TODO: 17/7/24 未完成无网络view
    }

    override fun setStyle(@StyleRes style: Int) {
        val attr = R.attr.rainbowTitleBarView
        context.obtainStyledAttributes(null, R.styleable.RainbowTitleBarView, attr, style)
                .initView().recycle()
    }

    override fun onPageAnimIn(value: Float, isPopBack: Boolean) {
        if (pageAnimatorFlag == ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE || !mPageAnimatorEnable) {
            return
        }
        if (pageAnimatorFlag == ITitleBarView.ImplView.PAGE_ANIM_FLAG_ALPHA) {
            translationX = 0f
            if (!isPopBack) {
                mTxtTitle.translationX = (1 - value) * mTxtTitle.width
                alpha = value
            } else {
                alpha = 1f
            }
        } else {
            alpha = 1f
            val width = width
            translationX = if (isPopBack) {
                val curr = 1 - value
                -(curr * (width / 3f))
            } else {
                (1 - value) * width
            }
        }
    }

    override fun onPageAnimOut(value: Float, isPopBack: Boolean) {
        if (pageAnimatorFlag == ITitleBarView.ImplView.PAGE_ANIM_FLAG_NONE || !mPageAnimatorEnable) {
            return
        }
        if (pageAnimatorFlag == ITitleBarView.ImplView.PAGE_ANIM_FLAG_ALPHA) {
            if (isPopBack) {
                mTxtTitle.translationX = value * mTxtTitle.width
                alpha = 1 - value
            }
        } else {
            val width = width
            translationX = if (isPopBack) {
                value * width
            } else {
                -(value * (width / 3f))
            }
        }
    }

    fun getTitleTextView(): TextView {
        return mTxtTitle
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var varHeightSpec = heightMeasureSpec
        var size = MeasureSpec.getSize(varHeightSpec)
        if (size in 1..STANDARD_HEIGHT && TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            size += statusBarHeight
            varHeightSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.getMode(varHeightSpec))
        }
        super.onMeasure(widthMeasureSpec, varHeightSpec)
    }


    private fun initMenuLayoutParams(view: View) {
        val params = LinearLayout.LayoutParams(-1, -1)
        params.width = -2
        params.height = -1
        view.layoutParams = params
        view.minimumWidth = resources.getDimension(R.dimen.rainbow_title_bar_button).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.background = getButtonBackground()
        } else {
            view.setBackgroundDrawable(getButtonBackground())
        }
    }

    private fun getButtonBackground(): Drawable? {
        return if (mButtonBackground == null || mButtonBackground?.constantState == null) {
            null
        } else mButtonBackground?.constantState!!.newDrawable()
    }

    override fun setPageAnimatorEnable(enable: Boolean) {
        mPageAnimatorEnable = enable
    }

    override fun isPageAnimatorEnable(): Boolean {
        return mPageAnimatorEnable
    }
}