package com.meili.moon.sdk.page.internal;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;

import com.meili.moon.sdk.CommonSdk;
import com.meili.moon.sdk.page.PageIntent;
import com.meili.moon.sdk.page.exception.StartPageException;
import com.meili.moon.sdk.page.internal.utils.PageManagerUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by imuto on 16/5/17.
 * Fragment的容器代理
 */
final class FragmentContainerProxy implements FragmentContainer {

    private static final int WORKS_DELAY = 100;

    private volatile boolean openingPage = false;
    private FragmentContainer containerImpl;
    private final ConcurrentLinkedQueue<Runnable> works = new ConcurrentLinkedQueue<>();

    private final long id;
    private final String affinity;
    private OnContainerFinishedListener mContainerListener;

    /*package*/ FragmentContainerProxy(String affinity) {
        this.id = System.currentTimeMillis();
        this.affinity = affinity;
    }

    /*package*/  FragmentContainerProxy(PageIntent intent) {
        if (intent != null) {
            this.id = intent.getFragmentContainerId();
            ActivityInfo acInfo = intent.getActivityInfo();
            this.affinity = acInfo == null ? null : acInfo.targetActivity;
        } else {
            this.id = System.currentTimeMillis();
            this.affinity = null;
        }
    }

    /**
     * 打开一个fragment
     * <br/>1.如果当前container没有绑定fragmentPage,则保存当前操作,并开启一个Activity
     * <br/>2.如果container已经绑定,则判断当前fragmentPage是否在前台
     * <br/>    如果不在,则保存当前操作,并唤起到前台
     * <br/>    如果在,则直接执行真正container的fragment操作
     */
    @Override
    public void startFragment(final PageIntent intent, final int requestCode) {

        synchronized (this) {
            if (isAlive()) {
                //noinspection ConstantConditions
                if (containerImpl.getFragmentsPage().isFront()) {
                    containerImpl.startFragment(intent, requestCode);
                } else {
                    try {
                        addMethodRunnable2Work(intent, requestCode, "startFragment", PageIntent.class, int.class);
                        bringContainerToFront();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    addMethodRunnable2Work(intent, requestCode, "startFragment", PageIntent.class, int.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                if (!openingPage) {
                    PageManagerUtils.openPageForResult(requestCode, getNewActivityIntent(intent), true);
                    openingPage = true;
                }
            }

        }
    }

    /**
     * 打开一个fragment
     * <br/>1.如果当前container没有绑定fragmentPage,则保存当前操作,并开启一个Activity
     * <br/>2.如果container已经绑定,则判断当前fragmentPage是否在前台
     * <br/>    如果不在,则保存当前操作,并唤起到前台
     * <br/>    如果在,则直接执行真正container的fragment操作
     */
    @Override
    public void gotoFragment(final PageIntent intent, int requestCode) {
        synchronized (this) {
            if (isAlive()) {
                /*
                    如果当前activity在最上面则直接调用container的gotoFragment
                    否则将请求加入到works堆栈中,并唤起Activity到最前,再执行
                 */
                //noinspection ConstantConditions
                if (containerImpl.getFragmentsPage().isFront()) {
                    containerImpl.gotoFragment(intent, requestCode);
                } else {
                    try {
                        addMethodRunnable2Work(intent, -1, "gotoFragment", PageIntent.class);
                        bringContainerToFront();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                try {
                    addMethodRunnable2Work(intent, -1, "gotoFragment", PageIntent.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
                if (!openingPage) {
                    PageManagerUtils.innerGotoPage(getNewActivityIntent(intent), true, null, null);
                    openingPage = true;
                }
            }
        }
    }

    @Override
    public void onFragmentsPageCreate(PageFragmentContainer fragmentsPage) {

        synchronized (this) {

            openingPage = false;

            if (containerImpl == null) {
                containerImpl = new FragmentContainerImpl(fragmentsPage, this);
            } else if (containerImpl.getFragmentsPage() != fragmentsPage) {
                containerImpl = new FragmentContainerImpl(fragmentsPage, this);
            }

            containerImpl.onFragmentsPageCreate(fragmentsPage);

            containerImpl.setOnContainerFinished(mContainerListener);

            int delay = 0;
            boolean first = true;
            while (!works.isEmpty()) {
                Runnable poll = works.poll();
                if (poll == null) continue;
                if (first) {
                    CommonSdk.task().post(poll);
                    first = false;
                } else {
                    CommonSdk.task().post(poll, delay);
                }
                delay += WORKS_DELAY;
            }
        }
    }

    @Override
    public PageFragmentContainer getFragmentsPage() {
        return containerImpl.getFragmentsPage();
    }

    @Override
    public boolean isEmpty() {
        return containerImpl == null || containerImpl.isEmpty();
    }

    @Override
    public int size() {
        if (containerImpl == null) {
            return 0;
        }
        return containerImpl.size();
    }

    @Override
    public void setOnContainerFinished(OnContainerFinishedListener listener) {
        this.mContainerListener = listener;
        if (containerImpl != null) {
            containerImpl.setOnContainerFinished(listener);
        }
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getAffinity() {
        return affinity;
    }

    @Override
    public int finish(int step, SdkFragment page) {
        synchronized (this) {
            works.clear();
            if (this.containerImpl == null) {
                return step;
            }
            return containerImpl.finish(step, page);
        }
    }

    @Override
    public void finishAll() {
        synchronized (this) {
            works.clear();
            if (this.containerImpl == null) {
                return;
            }
            containerImpl.finishAll();
        }
    }

    @Override
    public SdkFragment getTopFragment() {
        SdkFragment result = null;
        if (isAlive()) {
            result = containerImpl.getTopFragment();
        }
        return result;
    }

    @Override
    public SdkFragment getBottomFragment() {
        SdkFragment result = null;
        if (isAlive()) {
            result = containerImpl.getBottomFragment();
        }
        return result;
    }

    /**
     * 首先查看当前container是否已经绑定page,如果还没有则返回false
     * 如果已经绑定,则查看当前page是否已经打开了相同的class和nickName的fragment
     * <p/>
     * contain的主要用意为是否已经打开过这个fragment
     */
    @Override
    public boolean contains(final PageIntent intent) {
        return isAlive() && containerImpl.contains(intent);
    }

    @Override
    public boolean isAlive() {
        return containerImpl != null && this.containerImpl.isAlive();
    }

    /**
     * 根据传入intent获取一个打开新Activity的intent
     */
    private PageIntent getNewActivityIntent(PageIntent intent) {

        PageIntent pageIntent = new PageIntent();
        pageIntent.setFragmentContainerId(this.getId())
                .setActivityInfo(intent.getActivityInfo())
                .setAnimations(intent.getAnimations())
                .setClass(CommonSdk.environment().app(), PageManagerImpl.INSTANCE.getConfig().getPageContainer())
                .setPackage(CommonSdk.environment().app().getPackageName());

        return pageIntent;

    }

    private void bringContainerToFront() {
        final PageFragmentContainer fragmentsPage = containerImpl.getFragmentsPage();
        if (fragmentsPage != null) {
            if (!fragmentsPage.isFront() && fragmentsPage instanceof Activity) {
                Activity activity = (Activity) fragmentsPage;
                ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
                am.moveTaskToFront(activity.getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }

    /**
     * 添加一个runnable到work队列里,并当执行的时候回调method指定的方法
     */
    private void addMethodRunnable2Work(PageIntent intent, int requestCode, String method, Class<?>... paramTypes) throws NoSuchMethodException {
        MethodRunnable runnable = new MethodRunnable(
                intent, requestCode, getClass().getMethod(method, paramTypes));
        works.add(runnable);
    }

    private class MethodRunnable implements Runnable {
        PageIntent pageIntent;
        int requestCode;
        Method method;

        MethodRunnable(PageIntent pageIntent, int requestCode, Method method) {
            this.pageIntent = pageIntent;
            this.requestCode = requestCode;
            this.method = method;
        }

        @Override
        public void run() {
            try {
                if (method == null) {
                    return;
                }

                Object obj = FragmentContainerProxy.this;
                if (method.getName().equals("gotoFragment")) {
                    method.invoke(obj, pageIntent);
                } else {
                    method.invoke(obj, pageIntent, requestCode);
                }
            } catch (Exception ex) {
                throw new StartPageException(0, "open page error = " + pageIntent.getPageName()
                        + " msg = " + ex.getMessage(), ex);
            }
        }
    }

}
