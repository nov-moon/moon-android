package com.meili.moon.sdk.base.view;

import android.app.Activity;
import android.view.View;

/**
 * Created by imuto on 16/5/13.
 */
public class ViewFinder {
    private Activity activity;
    private View view;
    public ViewFinder(Activity activity) {
        this.activity = activity;
    }

    public ViewFinder(View view) {
        this.view = view;
    }

    public View find(int id) {
        if (activity != null) {
            return activity.findViewById(id);
        } else if (view != null) {
            return view.findViewById(id);
        }
        return null;
    }
    public View findByInfo(ViewInfo info) {
        return find(info.value, info.parentId);
    }

    public View find(int id, int pid) {
        View pView = null;
        if (pid > 0) {
            pView = this.find(pid);
        }

        View view;
        if (pView != null) {
            view = pView.findViewById(id);
        } else {
            view = this.find(id);
        }
        return view;
    }
}
