package com.meili.moon.sdk.page;

import com.meili.moon.sdk.CommonSdk;

public enum PageAnims {

    /**
     * 没有动画
     */
    NONE,
    /**
     * 默认
     */
    DEFAULT,
    /**
     * 淡入淡出
     */
    FADE_IN,
    /**
     * 从上到下
     */
    SLIDE_TOP_IN,
    /**
     * 从下到上
     */
    SLIDE_BOTTOM_IN,
    /**
     * 从左到右
     */
    SLIDE_LEFT_IN,
    /**
     * 从右到左
     */
    SLIDE_RIGHT_IN;

    /**
     * Curr: enter, Last: exit; Last: popEnter, Curr: popExit;
     *
     * @return
     */
    public int[] getAnimations() {
        switch (this) {
            case DEFAULT:
            case SLIDE_RIGHT_IN:
                return new int[]{R.anim.mn_sdk_page_router_slide_right_in, R.anim.mn_sdk_page_router_slide_left_out,
                        R.anim.mn_sdk_page_router_slide_left_in, R.anim.mn_sdk_page_router_slide_right_out};
            case SLIDE_LEFT_IN:
                return new int[]{R.anim.mn_sdk_page_router_slide_left_in, R.anim.mn_sdk_page_router_slide_right_out,
                        R.anim.mn_sdk_page_router_slide_right_in, R.anim.mn_sdk_page_router_slide_left_out};
            case SLIDE_TOP_IN:
                return new int[]{R.anim.mn_sdk_page_router_slide_top_in, R.anim.mn_sdk_page_router_slide_bottom_out,
                        R.anim.mn_sdk_page_router_slide_bottom_in, R.anim.mn_sdk_page_router_slide_top_out};
            case SLIDE_BOTTOM_IN:
                return new int[]{R.anim.mn_sdk_page_router_slide_bottom_in, R.anim.mn_sdk_page_router_slide_top_out,
                        R.anim.mn_sdk_page_router_slide_top_in, R.anim.mn_sdk_page_router_slide_bottom_out};
            case FADE_IN:
                return new int[]{R.anim.mn_sdk_page_router_fade_in, R.anim.mn_sdk_page_router_fade_out,
                        R.anim.mn_sdk_page_router_fade_in, R.anim.mn_sdk_page_router_fade_out};
            case NONE:
            default:
                return new int[]{0, 0, 0, 0};
        }
    }

    /**
     * 页面跳转时动延迟时间
     *
     * @param startTime
     * @return
     */
    public static long getDelayTime(long startTime) {
        long distance = System.currentTimeMillis() - startTime;
        long mDelayDuration = CommonSdk.app().getResources().getInteger(R.integer.mn_page_animation_duration) + 100;
        if (distance > mDelayDuration) {
            distance = 0;
        } else {
            distance = mDelayDuration - distance;
        }
        return distance;
    }

}
