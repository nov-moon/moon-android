package com.meili.moon.sdk.page.internal;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;

import com.meili.moon.sdk.CommonSdk;
import com.meili.moon.sdk.page.Page;
import com.meili.moon.sdk.page.PageIntent;
import com.meili.moon.sdk.page.PagesContainer;
import com.meili.moon.sdk.page.exception.StartPageException;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * 页面栈管理,存储打开的activity栈
 * <p/>
 * Created by imuto on 16/5/16.
 */
public enum PageStackManager implements Application.ActivityLifecycleCallbacks {

    INSTANCE;

    private static final Object lock = new Object();
    /**
     * activity的栈
     */
    private final LinkedList<ActivityRef> ACTIVITY_QUEUE = new LinkedList<>();
    /**
     * container的列表
     */
    private final List<FragmentContainer> CONTAINER_LIST = new ArrayList<>(5);

    private final FragmentContainer.OnContainerFinishedListener onContainerFinishedListener =
            new FragmentContainer.OnContainerFinishedListener() {
                @Override
                public void onContainerFinished(FragmentContainer container) {
                    CONTAINER_LIST.remove(container);
                    PagesContainer activityContainer = getContainer(container.getId());
                    if (activityContainer instanceof Activity) {
                        ((Activity) activityContainer).finish();
                    }
                }
            };

    private boolean isAlive = false;
    /**
     * 堆栈管理的部分功能方法委托类
     */
    private final PageStackDelegate mDelegate = new PageStackDelegate();

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        this.setTopActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        this.setTopActivity(activity);
        isAlive = true;
    }

    @Override
    public void onActivityPaused(Activity activity) {
        isAlive = false;
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity.isFinishing()) {
            this.removeActivity(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    public Activity getTopActivity() {
        Activity topActivity = null;
        synchronized (lock) {
            for (ActivityRef ref : ACTIVITY_QUEUE) {
                Activity ac = ref.get();
                if (ac != null) {
                    topActivity = ac;
                    break;
                }
            }
        }
        return topActivity;
    }

    public PagesContainer getContainer(long containerId) {
        PagesContainer topActivity = null;
        synchronized (lock) {
            for (ActivityRef ref : ACTIVITY_QUEUE) {
                Activity ac = ref.get();
                if (ac != null
                        && !ac.isFinishing()
                        && ac instanceof PagesContainer
                ) {
                    PagesContainer pagesContainer = (PagesContainer) ac;
                    if (pagesContainer.getPageIntent().getFragmentContainerId() == containerId) {
                        topActivity = (PagesContainer) ac;
                        break;
                    }
                }
            }
        }
        return topActivity;
    }

    public PagesContainer getPreContainer(long containerId) {
        PagesContainer container = getContainer(containerId);
        if (container == null) return null;
        int index = ACTIVITY_QUEUE.indexOf(new ActivityRef((Activity) container));
        if (index < 0 || index >= ACTIVITY_QUEUE.size() - 1) return null;

        ActivityRef activityRef = ACTIVITY_QUEUE.get(index + 1);
        Activity activity = activityRef.get();

        if (activity == null) return null;

        if (!(activity instanceof PagesContainer)) {
            return null;
        }

        return (PagesContainer) activity;
    }

    public PagesContainer getTopContainer() {
        PagesContainer topActivity = null;
        synchronized (lock) {
            for (ActivityRef ref : ACTIVITY_QUEUE) {
                Activity ac = ref.get();
                if (ac != null && !ac.isFinishing() && ac instanceof PagesContainer) {
                    topActivity = (PagesContainer) ac;
                    break;
                }
            }
        }
        return topActivity;
    }

    public FragmentContainer getFragmentContainer(long containerId) {
        synchronized (lock) {
            for (int i = 0; i < CONTAINER_LIST.size(); i++) {
                FragmentContainer container = CONTAINER_LIST.get(i);

                if (container.getId() == containerId) {
                    return container;
                }
            }
        }
        return null;
    }

    public FragmentContainer getPreFragmentContainer(long containerId) {
        synchronized (lock) {
            int index = -1;
            for (int i = 0; i < CONTAINER_LIST.size(); i++) {
                FragmentContainer container = CONTAINER_LIST.get(i);

                if (container.getId() == containerId) {
                    index = i - 1;
                    break;
                }
            }

            if (index > 0) {
                return CONTAINER_LIST.get(index);
            }
        }
        return null;
    }

    public Fragment getTopFragment() {
        return getTopFragment(0);
    }

    /**
     * 获取指定containerIndex的顶部fragment
     *
     * @param containerTopIndex 第几个container
     */
    /*package*/ Fragment getTopFragment(int containerTopIndex) {
        synchronized (lock) {
            if (containerTopIndex < 0) {
                containerTopIndex = 0;
            }
            if (containerTopIndex > CONTAINER_LIST.size()) {
                return null;
            }
            FragmentContainer indexContainer = null;
            for (int i = CONTAINER_LIST.size() - 1 - containerTopIndex; i >= 0; i--) {
                FragmentContainer item = CONTAINER_LIST.get(i);
                if (item != null && !item.isEmpty()) {
                    indexContainer = item;
                    break;
                }
            }
            if (indexContainer == null) {
                return null;
            }
            return indexContainer.getTopFragment();
        }
    }

    /**
     * 启动一个新的fragment,并判断fragment是否需要新的container
     */
    public void startFragment(PageIntent intent, int requestCode) throws Exception {
        synchronized (lock) {
            // 同步container和堆栈,如果堆栈数量大于container的数量,则补充container的数量
            if (CONTAINER_LIST.size() == 0 && ACTIVITY_QUEUE.size() > 0) {
                for (ActivityRef acRef : ACTIVITY_QUEUE) {
                    Activity ac = acRef.get();
                    if (ac != null && ac instanceof PageFragmentContainer) {
                        syncFragmentContainer((PageFragmentContainer) ac);
                    }
                }
            }

            // 判断是否需要启动新的container
            boolean needFragmentsPage = isNeedNewContainer(intent);

            //启动新的container
            FragmentContainer topProxy = getTopFragmentContainer();
            if (needFragmentsPage || topProxy == null) {

                String affinity = intent.getActivityInfo() == null ?
                        "" : intent.getActivityInfo().taskAffinity;

                topProxy = getNewFragmentContainer(affinity);
                CONTAINER_LIST.add(topProxy);
            }
            intent.setFragmentContainerId(topProxy.getId());
            // 准备打开Fragment
            topProxy.startFragment(intent, requestCode);
        }

    }


    /**
     * 尝试从堆栈历史中查找intent指定的fragment
     * 如果没有,则打开一个新的fragment
     * 如果有,则回退到相应的fragment
     */
    public void gotoFragment(PageIntent intent, int requestCode) throws Exception {
        synchronized (lock) {
            FragmentContainer targetProxy = null;
            Activity targetActivity = null;
            //查找当前堆栈是否已经打开过intent指定的fragment
            for (ActivityRef ref : ACTIVITY_QUEUE) {
                Activity ac = ref.get();
                if (ac != null && ac instanceof PageFragmentContainer) {
                    //获取当前activity的container
                    FragmentContainer proxy = syncFragmentContainer((PageFragmentContainer) ac);

                    //查看当前container是否包含这个fragment
                    if (proxy != null && proxy.contains(intent)) {
                        targetProxy = proxy;
                        targetActivity = ac;
                        break;
                    }
                }
            }

            //如果当前堆栈中已经有这个fragment,则直接销毁前面的activity,并使用container回退到fragment
            if (targetProxy != null) {

                finishOverTargetActivities(targetActivity);

                targetProxy.gotoFragment(intent, requestCode);
            } else {
                //如果堆栈中没有fragment,则打开一个新的fragment
                startFragment(intent, requestCode);
            }
        }
    }


    /**
     * 直接打开activity
     */
    public void startActivity(PageIntent intent, int requestCode) {
        Activity currActivity = getTopActivity();
        //使用currActivity打开新的activity
        Intent in = new Intent(intent);
        if (intent.getPageClass() == null
                && TextUtils.isEmpty(intent.getPageName())
                && intent.getFragmentContainerId() <= 0) {
            in.removeExtra(PageIntent.INNER_DATA_KEY);
        }
        if (currActivity != null) {
            //暂时先不处理动画
            int enterAnim = 0;
            int exitAnim = 0;
            int[] anims = intent.getAnimations();
            if (anims != null && anims.length > 1) {
                enterAnim = anims[0];
                exitAnim = anims[1];
            }
            if (enterAnim == 0 && exitAnim == 0) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            }
            if (requestCode <= 0) {
                currActivity.startActivity(in);
            } else {
                currActivity.startActivityForResult(in, requestCode);
            }
            currActivity.overridePendingTransition(0, 0);
        } else {
            if (requestCode >= 0) {
                throw new StartPageException(0, "open page error: " + intent.getPageName());
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                CommonSdk.environment().app().startActivity(intent);
            }
        }
    }


    /**
     * 打开指定的activity, 尝试首先在堆栈中找对应target的activity,如果没有则开启新的activity
     */
    public void gotoActivity(PageIntent intent, Class<?> target) {
        Activity currActivity = getTopActivity();
        //part 1 检查原有堆栈
        if (currActivity != null) {
            synchronized (lock) {
                //1.1尝试查找栈里是否有指定的activity
                Activity acTarget = contains(intent, target);
                //1.2关闭需要关闭的activity,指定的activity自然就会打开
                if (acTarget != null) {
                    finishOverTargetActivities(acTarget);
                } else {
                    //1.3打开activity
                    startActivity(intent, -1);
                }
            }
            //part 2 没有栈顶activity,直接开启新的
        } else {
            startActivity(intent, -1);
        }
    }

    /**
     * 关闭目标activity上面的activity
     */
    private void finishOverTargetActivities(Activity target) {
        ACTIVITY_QUEUE.peekFirst();
        ActivityRef firstRef = ACTIVITY_QUEUE.pollFirst();
        while (firstRef != null) {
            Activity ac = firstRef.get();
            if (ac != target) {
                try {
                    ac.finish();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                }
                firstRef = ACTIVITY_QUEUE.pollFirst();
            } else {
                ACTIVITY_QUEUE.addFirst(firstRef);
                break;
            }
        }
    }

    /**
     * 堆栈是否包含指定的target
     * 判断方式为首先堆栈的item的Class必须和target的Class相同
     * intent中的nickName也必须和item的nickName相同
     */
    private Activity contains(PageIntent intent, Class<?> target) {
        if (target != null) {
            String nickName = intent.getNickName();
            ACTIVITY_QUEUE.peekFirst();
            for (ActivityRef acRef : ACTIVITY_QUEUE) {
                Activity ac = acRef.get();
                if (ac != null && ac.getClass().equals(target)) {
                    String acNickName = ac instanceof Page ? ((Page) ac).getNickName() : null;
                    if (equals(nickName, acNickName)) {
                        return ac;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 匹配当前intent是否需要开启新的container进行管理
     * <p/>
     * <br/>1.如果container的缓存为空则认为需要启动一个新的container
     * <br/>2.如果activityInfo中明确指定FLAG_ACTIVITY_NEW_TASK 则启动新的container
     * <br/>3.如果栈顶的activity不是Page类型 则启动新的container
     * <br/>4.如果栈顶container和intent的affinity不同,则启动新的container
     * <br/>否则, 不启动新的container
     */
    private boolean isNeedNewContainer(PageIntent intent) {
        // 是否需要新Fragment栈,如果container为空,则说明原来没有container,
        boolean needFragmentsPage = CONTAINER_LIST.isEmpty();
        ActivityInfo acInfo = intent.getActivityInfo();
        String affinity = null;

        if (needFragmentsPage) {
            return true;
        }

        if (acInfo != null) {
            affinity = acInfo.taskAffinity;
            //如果flag是newTask,则需要新的container
            needFragmentsPage = (acInfo.flags & Intent.FLAG_ACTIVITY_NEW_TASK) != 0;
            if (needFragmentsPage) {
                return true;
            }
        }

        // 栈顶Activity不是FragmentsPage类型
        Activity topActivity = this.getTopActivity();
        if (topActivity == null || !(topActivity instanceof PageFragmentContainer)) {
            return true;
        }

        // affinity 不一致需要新Fragment栈
        FragmentContainer topProxy = getTopFragmentContainer();
        if (topProxy != null && !TextUtils.isEmpty(affinity)) {
            needFragmentsPage = !equals(topProxy.getAffinity(), affinity);
        }
        return needFragmentsPage;
    }

    public void finishAll() {
        synchronized (lock) {
            ACTIVITY_QUEUE.peekFirst();
            ActivityRef firstRef = ACTIVITY_QUEUE.pollFirst();
            while (firstRef != null) {
                Activity ac = firstRef.get();
                if (ac != null) {
                    try {
                        ac.finish();
                    } catch (Throwable ex) {
                        ex.printStackTrace();
                    }
                }
                firstRef = ACTIVITY_QUEUE.pollFirst();
            }
            CONTAINER_LIST.clear();
        }
    }

    public void finishAffinity(PageIntent pageIntent) {
        FragmentContainer proxy = null;
        synchronized (lock) {
            for (FragmentContainer fcp : CONTAINER_LIST) {
                if (fcp.contains(pageIntent)) {
                    proxy = fcp;
                    break;
                }
            }
            if (proxy == null) {
                return;
            }

            proxy.finishAll();

            CONTAINER_LIST.remove(proxy);
        }
    }

    public void finishAffinity(String affinity) {

        LinkedList<FragmentContainer> finishList = new LinkedList<>();
        synchronized (lock) {
            for (int i = CONTAINER_LIST.size() - 1; i >= 0; i--) {
                FragmentContainer fcp = CONTAINER_LIST.get(i);
                finishList.addFirst(fcp);
                if (equals(fcp.getAffinity(), affinity)) {
                    break;
                }
                if (i == 0) {
                    finishList.add(fcp);
                }
            }
        }

        if (finishList.isEmpty()) {
            return;
        }

        for (FragmentContainer container : finishList) {
            try {
                container.finishAll();
                CONTAINER_LIST.remove(container);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    protected long lastPageChangeRequestTime;
    private static long MIN_TIME_SPACE = 30;

    /**
     * 结束指定步数的fragment
     *
     * @param step
     * @param fragment
     */
    public void finish(int step, SdkFragment fragment) {
        long duration = System.currentTimeMillis() - lastPageChangeRequestTime;
        if (step == 1 && duration < MIN_TIME_SPACE) {
            return;
        }

        FragmentManager fm = fragment.getFragmentManager();
        if (fm == null) {
            return;
        }
        int backCount = 0;

        if (backCount > step) {
            step = backCount;
        }

        innerFinish(step, fragment);
    }

    private void innerFinish(int step, SdkFragment page) {
        long containerId = page.getContainerId();

        FragmentContainer container = getFragmentContainer(containerId);
        if (container == null) return;

        int finish = container.finish(step, page);

        if (finish > 0) {
            FragmentContainer preFragmentContainer = getPreFragmentContainer(containerId);
            if (preFragmentContainer == null) return;

            innerFinish(finish, preFragmentContainer.getTopFragment());
        }
    }

    protected void finishActivity(SdkFragment fragment) {
        Activity activity = fragment.getPageActivity();
        if (activity != null) {
            try {
                activity.finish();
                mDelegate.overridePendingTransition(fragment);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    private static boolean equals(String a, String b) {
        return !(TextUtils.isEmpty(a) || TextUtils.isEmpty(b)) && a.equals(b);
    }

    private void setTopActivity(Activity activity) {
        synchronized (lock) {
            // add to stack
            ActivityRef topActivityRef = new ActivityRef(activity);
            ACTIVITY_QUEUE.remove(topActivityRef);
            ACTIVITY_QUEUE.addFirst(topActivityRef);

            // remove destroyed items
            Iterator<ActivityRef> iterator = ACTIVITY_QUEUE.iterator();
            while (iterator.hasNext()) {
                ActivityRef ref = iterator.next();
                Activity act = ref.get();
                if (act == null || act.isFinishing()) {
                    iterator.remove();
                }
            }

            // FragmentsPage ?
            if (activity instanceof PageFragmentContainer) {
                //通知container,继续打开fragment
                PageFragmentContainer fragmentsPage = (PageFragmentContainer) activity;
                FragmentContainer fragmentContainerProxy = syncFragmentContainer(fragmentsPage);
                if (fragmentContainerProxy != null) {
                    fragmentContainerProxy.onFragmentsPageCreate(fragmentsPage);
                }
            }
        }
    }

    private void removeActivity(Activity activity) {
        synchronized (lock) {
            // FragmentsPage ?
            if (activity instanceof PageFragmentContainer) {
                PageFragmentContainer fragmentsPage = (PageFragmentContainer) activity;
                PageIntent pageIntent = fragmentsPage.getPageIntent();
                long id = pageIntent.getFragmentContainerId();
                if (id > 0) {
                    FragmentContainer container = findContainer(id);
                    if (container != null) {
                        container.setOnContainerFinished(null);
                        CONTAINER_LIST.remove(container);
                    }
                }
            }

            // remove activity
            ActivityRef activityRef = new ActivityRef(activity);
            ACTIVITY_QUEUE.remove(activityRef);
        }
    }

    /**
     * 获取FragmentContainerProxy
     * <br/>1.如果fragmentsPage已经有containerID,则返回一个container
     * <br/>有ID分两种情况,一种是已经绑定了container,一种是Activity启动回调的有id,当时没有绑定
     * <br/>2.如果没有,则返回null
     */
    private synchronized FragmentContainer syncFragmentContainer(
            PageFragmentContainer fragmentsPage) {
        if (fragmentsPage == null) {
            return null;
        }
        FragmentContainer result = null;
        PageIntent pageIntent = fragmentsPage.getPageIntent();

        long id = pageIntent.getFragmentContainerId();
        //如果id>0,这种情况属于新开的了一个activity,打开activity以后回调的打开fragment
        if (id > 0) {
            result = findContainer(id);
            if (result == null) {
                result = getNewFragmentContainer(pageIntent);
                CONTAINER_LIST.add(result);
            }
        }

        return result;
    }

    private FragmentContainer findContainer(long id) {
        for (int i = CONTAINER_LIST.size() - 1; i >= 0; i--) {
            FragmentContainer fragmentContainer = CONTAINER_LIST.get(i);
            if (fragmentContainer.getId() == id) {
                return fragmentContainer;
            }
        }
        return null;
    }

    private synchronized FragmentContainer getTopFragmentContainer() {
//        FragmentContainer topProxy = null;
//
//        for (int i = CONTAINER_LIST.size() - 1; i >= 0; i--) {
//            FragmentContainer ac = CONTAINER_LIST.get(i);
//            if (ac != null) {
//                if (ac instanceof PageFragmentContainer) {
//                    PageFragmentContainer fragmentsPage = (PageFragmentContainer) ac;
//                    topProxy = this.syncFragmentContainer(fragmentsPage);
//                }
//                break;
//            }
//        }

        if (CONTAINER_LIST.size() > 0) {
            return CONTAINER_LIST.get(CONTAINER_LIST.size() - 1);
        } else {
            return null;
        }
    }

    private FragmentContainer getNewFragmentContainer(PageIntent pageIntent) {
//        PageFragmentContainer pageFragmentContainer = null;
//        if (getTopActivity() instanceof PageFragmentContainer) {
//            pageFragmentContainer = (PageFragmentContainer) getTopActivity();
//        }

        FragmentContainerProxy fragmentContainerProxy = new FragmentContainerProxy(pageIntent);
        fragmentContainerProxy.setOnContainerFinished(onContainerFinishedListener);
        return fragmentContainerProxy;
    }


    private FragmentContainer getNewFragmentContainer(String affinity) {
//        PageFragmentContainer pageFragmentContainer = null;
//        if (getTopActivity() instanceof PageFragmentContainer) {
//            pageFragmentContainer = (PageFragmentContainer) getTopActivity();
//        }

        FragmentContainerProxy fragmentContainerProxy = new FragmentContainerProxy(affinity);
        fragmentContainerProxy.setOnContainerFinished(onContainerFinishedListener);
        return fragmentContainerProxy;
    }

    public boolean isAlive() {
        return isAlive;
    }

    private static class ActivityRef extends WeakReference<Activity> {

        public ActivityRef(Activity r) {
            super(r);
        }

        @Override
        public int hashCode() {
            Activity r = this.get();
            return r == null ? 0 : r.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Activity activity = this.get();
            if (activity == null) return false;

            ActivityRef that = (ActivityRef) o;
            Activity thatActivity = that.get();

            return thatActivity != null && activity.equals(thatActivity);

        }
    }
}
