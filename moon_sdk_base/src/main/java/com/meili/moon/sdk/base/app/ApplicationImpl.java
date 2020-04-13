package com.meili.moon.sdk.base.app;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.meili.moon.sdk.base.Sdk;
import com.meili.moon.sdk.common.IApplication;


/**
 * Application的自定义绑定对象
 * Created by imuto on 17/10/18.
 */
public class ApplicationImpl extends ContextWrapper implements IApplication {

    private Application mApp;

    public ApplicationImpl(Context base) {
        super(base);
        if (base instanceof Application) {
            mApp = (Application) base;
        }
    }

    public void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void registerLocalReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    public void unregisterLocalReceiver(BroadcastReceiver receiver) {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        if (mApp != null) {
            mApp.registerActivityLifecycleCallbacks(callback);
        }
    }

    @Override
    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        if (mApp != null) {
            mApp.unregisterActivityLifecycleCallbacks(callback);
        }
    }

    /**
     * 当前app是否正在运行
     */
    @Override
    public boolean isRunning() {
        return Sdk.page().getTopPage() != null;
    }

    @Override
    public Application application() {
        return mApp;
    }
}
