package com.meili.moon.sdk.app.base.page.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.meili.moon.sdk.app.R;
import com.meili.moon.sdk.app.base.page.util.TranslucentStatusBarUtils;
import com.meili.moon.sdk.app.exception.ViewInitException;
import com.meili.moon.sdk.base.util.DensityUtil;
import com.meili.moon.sdk.base.util.VersionUtils;
import com.meili.moon.sdk.base.util.ViewUtil;
import com.meili.moon.sdk.log.Logcat;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;


/**
 * 皓月规范的TitleBar实现
 * Created by imuto on 16/05/20.
 */
public class MoonTitleBarView extends RelativeLayout implements ITitleBarView.ImplView {

    private ImageView mImgBack;
    private TextView mTxtBack;
    private View mLayoutBack;
    private View mH5Close;
    private LinearLayout mViewMenuBar;
    private TextView mTxtTitle;
    private int mMenuTextColor;

    private int mPageAnimatorFlag = 0;

    private OnClickListener onDoubleClickListener;

    /**
     * 是否已经初始化view
     */
    private boolean hasInitView = false;

    private boolean pageAnimatorEnable = true;

    /**
     * use {@link #getButtonBackground()},
     */
    @Deprecated
    private Drawable mButtonBackground;

    private int STANDARD_HEIGHT;

    private HashMap<String, View> mMenuViewMap = new HashMap<>();


    private final OnClickListener onTitleClickListener = new OnClickListener() {
        private long mLastTitleClick;
        private long maxDuration = ViewConfiguration.getDoubleTapTimeout();

        @Override
        public void onClick(View v) {
            long currTime = System.currentTimeMillis();
            synchronized (onTitleClickListener) {
                if (currTime - mLastTitleClick < maxDuration) {
                    mLastTitleClick = 0;
                    onDoubleClickListener.onClick(v);
                } else {
                    mLastTitleClick = currTime;
                }
            }
        }
    };


    public MoonTitleBarView(Context context) {
        super(context);
    }


    public MoonTitleBarView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.moonTitleBarView);
    }

    public MoonTitleBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (!isInEditMode()) {
            mButtonBackground = context.getResources().getDrawable(R.drawable.moon_sdk_app_titlebar_item_bg);
        }
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MoonTitleBarView, defStyleAttr, 0);
        initView(a);
        a.recycle();
    }

    private void initView(TypedArray a) {
        if (!hasInitView) {
            STANDARD_HEIGHT = getContext().getResources().getDimensionPixelSize(R.dimen.moon_sdk_app_title_bar_height);
            hasInitView = true;
            View.inflate(getContext(), R.layout.moon_sdk_app_titlebar_layout, this);
            mLayoutBack = findViewById(R.id.layout_home);
            mH5Close = findViewById(R.id.h5_close);
            mImgBack = findViewById(R.id.title_bar_home);
            mTxtBack = findViewById(R.id.title_bar_home_text);
            mViewMenuBar = findViewById(R.id.title_bar_menu_bar);
            mTxtTitle = findViewById(R.id.title_bar_title);
        }

        Resources r = getResources();
        int titlePaddingTop = (int) a.getDimension(R.styleable.MoonTitleBarView_titleBarPaddingTop, r.getDimension(R.dimen.moon_sdk_app_title_bar_translucent));
        int titleHomeMinWidth = (int) a.getDimension(R.styleable.MoonTitleBarView_titleBarHomeMinWidth, r.getDimension(R.dimen.moon_sdk_app_title_bar_button));
        Drawable homeBg = a.getDrawable(R.styleable.MoonTitleBarView_titleBarHomeBg);
        if (homeBg == null) {
            homeBg = r.getDrawable(R.drawable.moon_sdk_app_titlebar_item_bg);
        }
        Drawable homeSrc = a.getDrawable(R.styleable.MoonTitleBarView_titleBarHomeSrc);
        if (homeSrc == null) {
            homeSrc = r.getDrawable(R.drawable.moon_sdk_app_title_bar_home_back);
        }

        ColorStateList homeTextColor = a.getColorStateList(R.styleable.MoonTitleBarView_titleBarHomeTxtColor);
        if (homeTextColor == null) {
            homeTextColor = r.getColorStateList(R.color.moon_sdk_app_titlebar_menu_text_color);
        }
        int homeTextSize = (int) a.getDimension(R.styleable.MoonTitleBarView_titleBarHomeTxtSize, DensityUtil.dip2px(14));

        int titleColor = a.getColor(R.styleable.MoonTitleBarView_titleBarTitleColor, r.getColor(R.color.moon_sdk_app_title_bar_menu_text_normal));
        int titleSize = (int) a.getDimension(R.styleable.MoonTitleBarView_titleBarTitleSize, r.getDimension(R.dimen.moon_sdk_app_title_bar_title_text));

        Drawable bgDrawable = a.getDrawable(R.styleable.MoonTitleBarView_titleBarBackground);
        if (bgDrawable == null) {
            bgDrawable = r.getDrawable(R.drawable.moon_sdk_app_titlebar_bg);
        }

        mMenuTextColor = a.getColor(R.styleable.MoonTitleBarView_titleBarMenuTextColor, Color.BLACK);

        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        mLayoutBack.setMinimumWidth(titleHomeMinWidth);
        mLayoutBack.setBackgroundDrawable(homeBg);
        mImgBack.setImageDrawable(homeSrc);
        mTxtBack.setTextColor(homeTextColor);
        mTxtBack.setTextSize(TypedValue.COMPLEX_UNIT_PX, homeTextSize);

        mTxtTitle.setTextColor(titleColor);
        mTxtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);

        setBackgroundDrawable(bgDrawable);

        if (TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            titlePaddingTop = titlePaddingTop + ViewUtil.getStatusBarHeight();
        }

        setPadding(getPaddingLeft(), titlePaddingTop, getPaddingRight(), getPaddingBottom());

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void setBackClickListener(OnClickListener lis) {
        mLayoutBack.setOnClickListener(lis);
    }

    @Override
    public void setH5BackClickListener(OnClickListener lis) {
        mH5Close.setOnClickListener(lis);
    }

    @Override
    public void setH5CloseVisible() {
        mH5Close.setVisibility(View.VISIBLE);
    }

    @Override
    public void setH5CloseGone() {
        mH5Close.setVisibility(View.GONE);
    }

    @Override
    public void setOnTitleDoubleClickListener(OnClickListener lis) {
        if (lis == null) {
            return;
        }
        onDoubleClickListener = lis;
        mTxtTitle.setOnClickListener(onTitleClickListener);
    }

    @Override
    public int getTitleBarHeight() {
        return getContext().getResources().getDimensionPixelSize(R.dimen.moon_sdk_app_title_bar_height)
                + ViewUtil.getStatusBarHeight();
    }

    @Override
    public void setTitle(CharSequence text) {
        mTxtTitle.setText(text);
    }

    @Override
    public void setTitle(int resId) {
        setTitle(getResources().getText(resId));
    }

    @Override
    public void setTitleTextColor(int color) {
        mTxtTitle.setTextColor(color);
    }

    @Override
    public void setBackIcon(int resId) {
        setBackIcon(getResources().getDrawable(resId));
    }

    @Override
    public void setBackIcon(Drawable drawable) {
        mImgBack.setImageDrawable(drawable);
    }

    @Override
    public void setBackIconVisible(int visible) {
        mImgBack.setVisibility(visible);
    }

    @Override
    public void setBackText(int resId) {
        setBackText(getResources().getText(resId));
    }

    @Override
    public void setBackText(CharSequence text) {
        mTxtBack.setText(text);
    }

    @Override
    public void setBackTextColor(int resId) {
        mTxtBack.setTextColor(resId);
    }

    @Override
    public void setTitleBarBackgroundColor(int color) {
        setBackgroundColor(color);
    }

    @Override
    public void setTitleBarBackgroundDrawable(int resId) {
        setBackgroundResource(resId);
    }

    public void fadeIn() {
        AlphaAnimation fadeAnimation = new AlphaAnimation(0F, 1F);
        fadeAnimation.setDuration(400);
        fadeAnimation.setInterpolator(new DecelerateInterpolator());
        startAnimation(fadeAnimation);
    }


    @Override
    public void removeMenu(Menu menu) {
        if (menu == null) return;
        View view = menu.layout;
        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }
        mViewMenuBar.removeView(view);
    }

    @Override
    public void addMenu(@NonNull String id, Menu menu) {
        // TODO: 16/5/20 未完成添加逻辑
        View view = menu.layout;
        if (view == null) {
            if (VersionUtils.isDebug()) {
                throw new ViewInitException("Menu的ID不能为空");
            }
            return;
        }
        int index = -1;
        if (TextUtils.isEmpty(id) && VersionUtils.isDebug()) {
            throw new ViewInitException("Menu的ID不能为空");
        }
        if (mMenuViewMap.containsKey(id)) {
            View currView = mMenuViewMap.get(id);
            index = mViewMenuBar.indexOfChild(currView);
            if (index > -1 && view == currView) { // 找到 && 相同的 view ,不处理
                return;
            }
        }

        if (view.getParent() != null) {
            ((ViewGroup) view.getParent()).removeView(view);
        }

        if (index >= 0) {
            mViewMenuBar.removeViewAt(index);
        }
        initMenuLayoutParams(view);
        if (menu instanceof MenuText) {
            ((MenuText) menu).setTextColor(mMenuTextColor);
        }

        mViewMenuBar.addView(view, index);
        mMenuViewMap.put(id, view);
    }

    @Override
    public void setTitleBarVisibility(int visible) {
        setVisibility(visible);
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        Logcat.d(visibility);
    }

    @Override
    public int getTitleBarVisibility() {
        return getVisibility();
    }

    @Override
    public void setSupportNoNetworkStyle(boolean isSupport) {
        // TODO: 17/7/24 未完成无网络view
    }

    @Override
    public void setStyle(@StyleRes int style) {
        int attr = R.attr.moonTitleBarView;
        TypedArray a = getContext().obtainStyledAttributes(null, R.styleable.MoonTitleBarView, attr, style);
        initView(a);
        a.recycle();
    }

    @Override
    public void setPageAnimatorFlag(int flag) {
        mPageAnimatorFlag = flag;
    }

    @Override
    public int getPageAnimatorFlag() {
        return mPageAnimatorFlag;
    }

    @Override
    public void onPageAnimIn(float value, boolean isPopBack) {
        if (mPageAnimatorFlag == PAGE_ANIM_FLAG_NONE || !pageAnimatorEnable) {
            return;
        }
        if (mPageAnimatorFlag == PAGE_ANIM_FLAG_ALPHA) {
            setTranslationX(0);
            if (!isPopBack) {
                mTxtTitle.setTranslationX((1 - value) * mTxtTitle.getWidth());
                setAlpha(value);
            } else {
                setAlpha(1);
            }
        } else {
            setAlpha(1);
            int width = getWidth();
            if (isPopBack) {
                float curr = 1 - value;
                setTranslationX(-(curr * (width / 3F)));
            } else {
                setTranslationX((1 - value) * width);
            }
        }
    }

    @Override
    public void onPageAnimOut(float value, boolean isPopBack) {
        if (mPageAnimatorFlag == PAGE_ANIM_FLAG_NONE || !pageAnimatorEnable) {
            return;
        }
        if (mPageAnimatorFlag == PAGE_ANIM_FLAG_ALPHA) {
            if (isPopBack) {
                mTxtTitle.setTranslationX(value * mTxtTitle.getWidth());
                setAlpha(1 - value);
            }
        } else {
            int width = getWidth();
            if (isPopBack) {
                setTranslationX(value * width);
            } else {
                setTranslationX(-(value * (width / 3F)));
            }
        }
    }

    public TextView getTitleTextView() {
        return mTxtTitle;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = MeasureSpec.getSize(heightMeasureSpec);
        if (size > 0 && size <= STANDARD_HEIGHT && TranslucentStatusBarUtils.isSupportTranslucentStatusBarStyle()) {
            size += ViewUtil.getStatusBarHeight();
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.getMode(heightMeasureSpec));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    private void initMenuLayoutParams(View view) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -1);
        params.width = -2;
        params.height = -1;
        view.setLayoutParams(params);
        view.setMinimumWidth((int) getResources().getDimension(R.dimen.moon_sdk_app_title_bar_button));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(getButtonBackground());
        } else {
            view.setBackgroundDrawable(getButtonBackground());
        }
    }

    private Drawable getButtonBackground() {
        if (mButtonBackground == null || mButtonBackground.getConstantState() == null) {
            return null;
        }
        return mButtonBackground.getConstantState().newDrawable();
    }

    @NotNull
    @Override
    public View onCreateView(@NotNull View view) {
        return view;
    }

    @Override
    public void setPageAnimatorEnable(boolean enable) {
        pageAnimatorEnable = enable;
    }

    @Override
    public boolean isPageAnimatorEnable() {
        return pageAnimatorEnable;
    }
}
