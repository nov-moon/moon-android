package com.meili.moon.sdk.page.internal;

import android.app.Activity;
import android.support.v4.app.FragmentManager;

/**
 * Created by imuto on 16/10/26.
 */

class PageStackDelegate {

    public void finishActivity(SdkFragment fragment) {
        Activity activity = fragment.getPageActivity();
        if (activity != null) {
            try {
                activity.finish();
                overridePendingTransition(fragment);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    /** 当前是否可结束指定fragment */
    public boolean canFinishFragment(SdkFragment fragment) {
        if (fragment == null || fragment.getPageActivity() == null) {
            return false;
        }
        FragmentManager fragmentManager = fragment.getFragmentManager();
        return fragmentManager != null && !fragmentManager.isStateSaved();
    }

    public void finishFragmentByTag(SdkFragment fragment, int step) {
        FragmentManager fm = fragment.getFragmentManager();
        int stackCount = fm.getBackStackEntryCount();
        int backStep = step;
        if (step > stackCount) {
            backStep = stackCount;
        }
        FragmentManager.BackStackEntry stackRecord =
                fm.getBackStackEntryAt(stackCount - backStep - 1);
        if (stackRecord != null) {
            String tag = stackRecord.getName();
            SdkFragment tagFragment = (SdkFragment) fm.findFragmentByTag(tag);
            if (tagFragment != null && canFinishFragment(fragment)) {
                fm.popBackStackImmediate(tag, 0);
            }
        }
    }

    public void overridePendingTransition(SdkFragment fragment) {
        Activity activity = fragment.getPageActivity();
        int[] anims = fragment.getPageIntent().getAnimations();
        if (anims != null && anims.length >= 4) {
            activity.overridePendingTransition(anims[2], anims[3]);
        }
    }


}
