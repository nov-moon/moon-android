package com.meili.moon.sdk.app.widget.pagetools.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.meili.moon.sdk.app.widget.pagetools.PageToolsParams;


/**
 * Created by imuto on 15/12/14.
 */
public interface PageToolView {

   View onCreateView(Context context, ViewGroup parent);

   void onDataChanged(PageToolsParams params);

   void onVisibilityChanged(boolean visible);

}
