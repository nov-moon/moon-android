package com.meili.moon.sdk.page.internal;

import android.content.pm.ActivityInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.meili.moon.sdk.page.PageIntent;
import com.meili.moon.sdk.page.Rainbow;
import com.meili.moon.sdk.page.exception.StartPageException;
import com.meili.moon.sdk.page.internal.utils.SdkFragmentExtra;
import com.meili.moon.sdk.util.ArrayUtil;
import com.meili.moon.sdk.util.ThrowableUtilsKt;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by imuto on 16/5/17.
 * fragment容器
 */
final class FragmentContainerImpl implements FragmentContainer {

    private WeakReference<PageFragmentContainer> fragmentsPageRef;
    private List<WeakReference<SdkFragment>> fragmentList = new ArrayList<>(10);
    private OnContainerFinishedListener mListener;
    private FragmentContainerProxy mProxy;
    private FragmentManager.FragmentLifecycleCallbacks fragmentCallback = new FragmentManager.FragmentLifecycleCallbacks() {
        @Override
        public void onFragmentDestroyed(FragmentManager fm, Fragment f) {
            super.onFragmentDestroyed(fm, f);
            int fragmentIndex = findFragmentIndex(f);
            if (fragmentIndex < 0) {
                return;
            }
            fragmentList.remove(fragmentIndex);
            if (fragmentList.isEmpty()) {
                fm.unregisterFragmentLifecycleCallbacks(fragmentCallback);
                if (mListener != null) {
                    mListener.onContainerFinished(mProxy);
                }
            }
        }
    };

    private FragmentContainerHelper mHelper = new FragmentContainerHelper(this);

    /*package*/ FragmentContainerImpl(PageFragmentContainer fragmentsPage, FragmentContainerProxy proxy) {
        onFragmentsPageCreate(fragmentsPage);
        mProxy = proxy;
    }

    public PageFragmentContainer getFragmentsPage() {
        return fragmentsPageRef == null ? null : fragmentsPageRef.get();
    }

    @Override
    public boolean isEmpty() {
        return fragmentList.isEmpty();
    }

    @Override
    public int size() {
        return fragmentList.size();
    }

    @Override
    public void setOnContainerFinished(OnContainerFinishedListener listener) {
        mListener = listener;
    }

    @Override
    public long getId() {
        return mProxy.getId();
    }

    @Override
    public String getAffinity() {
        return null;
    }

    /**
     * 如果当前container绑定了fragmentPage,并且fragmentPage没有结束,则返回true,否则返回false
     */
    public boolean isAlive() {
        return fragmentsPageRef != null
                && fragmentsPageRef.get() != null
                && !fragmentsPageRef.get().isFinishing();
    }


    @Override
    public SdkFragment getTopFragment() {
        if (fragmentList.isEmpty()) {
            return null;
        }
        return fragmentList.get(fragmentList.size() - 1).get();
    }

    @Override
    public SdkFragment getBottomFragment() {
        if (fragmentList.isEmpty()) {
            return null;
        }
        return fragmentList.get(0).get();
    }

    /**
     * 是否包含这个intent要打开的fragment,判断依据为intent的pageClass和nickName是否相同
     *
     * @param intent
     * @return
     */
    @Override
    public boolean contains(PageIntent intent) {
        PageFragmentContainer page = this.getFragmentsPage();
        if (page == null) {
            return false;
        }

        String tag = getTag(intent);

        return findFragment(tag) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized void startFragment(PageIntent intent, int requestCode) {
        PageFragmentContainer page = this.getFragmentsPage();
        if (page == null) {
            return;
        }

        if (page.onStartPage(intent)) {
            return;
        }

        FragmentManager fm = page.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        // set args
        SdkFragment newFragment = null;
        try {
            newFragment = ((Class<SdkFragment>) intent.getPageClass()).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        newFragment.setRequestCode(requestCode);
        newFragment.setArguments(intent.getExtras());
        newFragment.getPageIntent().reset(intent);
        newFragment.setContainerId(getId());


        // hide last fragment
        Fragment lastFragment = (Fragment) PageManagerImpl.INSTANCE.getTopPage();
        if (lastFragment == null && !fragmentList.isEmpty()) {
            lastFragment = fragmentList.get(fragmentList.size() - 1).get();
        }
        boolean isAnim = lastFragment != null;
//        boolean isAnim = lastFragment != null && page.contain((Page) lastFragment);

        if (isAnim) {
            newFragment.getPageIntent().setLastFragmentTag(lastFragment.getTag());

            int[] animations = intent.getAnimations();
            boolean isOverlay = newFragment.isOverlayFragment();
            if (animations != null) {
                if (animations.length == 2) {
                    ft.setCustomAnimations(animations[0], animations[1]);
                } else if (animations.length == 4) {
                    if (!isOverlay) {
                        ft.setCustomAnimations(animations[0], animations[1], animations[2], animations[3]);
                    } else {
                        ft.setCustomAnimations(animations[0], 0, 0, animations[3]);
                    }
                }
            }
            if (!isOverlay) {
                ft.hide(lastFragment);
            }
            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        } else {
            ft.setTransition(FragmentTransaction.TRANSIT_NONE);
        }
        try {// add to stack
            String tag = getTag(intent);
            ft.add(page.getContainerId(), newFragment, tag);
            ft.addToBackStack(tag);

            mHelper.startFragment(ft);

            fragmentList.add(new WeakReference(newFragment));

            PageCallbackHolder.INSTANCE.fixCallback(intent, tag);

            mHelper.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void gotoFragment(PageIntent intent, int requestCode) {
        PageFragmentContainer page = this.getFragmentsPage();
        if (page == null) {
            return;
        }
        try {
            FragmentManager fm = page.getSupportFragmentManager();
            String tag = getTag(intent);
            Fragment tagFragment = findFragment(tag);

            if (tagFragment == null) {
                startFragment(intent, requestCode);
            } else {

                PageCallbackHolder.INSTANCE.fixCallback(intent, tag);

                if (page.onGotoPage(intent)) {
                    return;
                }
                // 判断当前tag是否已经是目标页
                if (tagFragment != getTopFragment()) {
                    fm.popBackStack(tag, 0);
                }
                if (tagFragment instanceof SdkFragment) {
                    SdkFragment baseFragment = (SdkFragment) tagFragment;
                    baseFragment.onArgumentsReset(intent.getExtras());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private List<SdkFragment> onResultCache = new ArrayList<>();
//    private final List<Pair<Integer, SdkFragment>> processCache = new ArrayList<>();

    @Override
    public int finish(int step, SdkFragment page) {

        if (fragmentList.isEmpty()) {
            return step;
        }

//        Pair<Integer, SdkFragment> process = new Pair<>(step, page);
//        processCache.add(process);

        step = fixStep(step, page);

        mHelper.finishFragment(step, page);

        int result;

        //当前结束page的位置
        int pageIndex = findFragmentIndex(page);
        //将当前结束位置到起始位置，看为一个小数组，他的大小
        int validateSize = pageIndex + 1;
        //如果大于等于这个大小，则整个消除
        if (step >= validateSize) {
            result = step - validateSize;
        } else {
            result = 0;
        }

        //执行多少步消除操作
        int processStep = step - result;

        //消除操作的起始位置
        int start = pageIndex;

        for (int i = start; i > start - processStep; i--) {
            SdkFragment f = fragmentList.get(i).get();
            if (f == null) continue;

            //不处理已经标记为dirty的页面
            if (SdkFragmentExtra.isDirty(f)) {
                continue;
            }

            if (onResultCache.contains(f)) continue;

            Object fResult = f.getResult();

            if (fResult == null) {
                continue;
            }

            String fTag = f.getTag();

            if (TextUtils.isEmpty(fTag)) {
                continue;
            }

            onResultCache.add(f);
            PageCallbackHolder.INSTANCE.callback(fTag, fResult);
            PageCallbackHolder.INSTANCE.remove(fTag);

            f.clearResult();

            onResultCache.remove(f);
        }

        finishDirty(step, page);

        mHelper.commit();

//        CommonSdk.task().removeCallbacks(finishProcessor);
//        CommonSdk.task().post(finishProcessor);

        return result;
    }

    private int fixStep(int step, SdkFragment page) {
        int index = findFragmentIndex(page);
        if (index <= 0) {
            return step;
        }
        index --;

        for (int i = index; i >= 0; i--) {
            SdkFragment fragment = fragmentList.get(i).get();
            if (fragment == null || !SdkFragmentExtra.isDirty(fragment)) {
                break;
            }
            step++;
        }

        return step;
    }

    /**
     * 尝试结束之前的缓存
     *
     * @param page 当前页面
     */
    private void finishDirty(int step, SdkFragment page) {
        int index = findFragmentIndex(page);
        if (index <= 0) {
            return;
        }

        step --;
        if (step > index) {
            return;
        }

        //找到已经处理的最底部的fragment
        page = fragmentList.get(index - step).get();

        index = index - step - 1;
        step = 0;

        if (index < 0) {
            return;
        }

        for (int i = index; i >= 0; i--) {
            SdkFragment fragment = fragmentList.get(i).get();
            if (fragment == null || !SdkFragmentExtra.isDirty(fragment)) {
                break;
            }
            step++;
        }

        if (step == 0) {
            return;
        }

        mHelper.finishFragment(step, findPreFragment(page));
    }

    String getFinishTag(int step, SdkFragment page) {

        SdkFragment fragment;

        int pageIndex = findFragmentIndex(page);
        int validateSize = pageIndex + 1;
        if (step >= validateSize) {
            fragment = getBottomFragment();
        } else {
            fragment = fragmentList.get(validateSize - step).get();
        }
        if (fragment != null) {
            return fragment.getTag();
        }

        return null;
    }

    @Override
    public void finishAll() {

    }

    @Override
    public void onFragmentsPageCreate(PageFragmentContainer pageFragmentContainer) {
        fragmentsPageRef = new WeakReference<>(pageFragmentContainer);
        if (pageFragmentContainer != null) {
            FragmentManager supportFragmentManager = pageFragmentContainer.getSupportFragmentManager();
            supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallback, false);
            List<Fragment> fragments = supportFragmentManager.getFragments();
            if (!ArrayUtil.isEmpty(fragments)) {
                for (int i = 0; i < fragments.size(); i++) {
                    Fragment item = fragments.get(i);
                    if (item == null || !(item instanceof SdkFragment)) {
                        ThrowableUtilsKt.throwOnDebug(new StartPageException(0, "Fragment的堆栈包括其他加入的Fragment类型，" +
                                "可能是因为使用getSupportFragmentManager()造成的，请使用getChildFragmentManager()方法管理你的子堆栈"));
                        return;
                    }
                    WeakReference<SdkFragment> ref = new WeakReference<>((SdkFragment) fragments.get(i));
                    fragmentList.add(ref);
                }
            }
        }
    }

    /**
     * 获取tag
     * <br/>1.查看PageIntent绑定的Activity是否是单例模式,如果是则直接返回当前fragment的类名
     * <br/>2.如果不是则使用:类名+nickName的方式作为tag.
     * <br/>3.查看当前page的fragment的堆栈,如果有相同pageName的,并且他们的nickName还一样,则直接返回相同的tag
     */
    private String getTag(PageIntent intent) {
        Class<?> pageClass = intent.getPageClass();
        ActivityInfo activityInfo = intent.getActivityInfo();
        if (activityInfo != null) {
            if (activityInfo.launchMode == ActivityInfo.LAUNCH_SINGLE_INSTANCE) {
                return pageClass.getName();
            }
        }

        String nickName = intent.getNickName();
        if (TextUtils.isEmpty(nickName) && Rainbow.INSTANCE.getConfig().getCanOpenSamePage()) {
            nickName = System.currentTimeMillis() + "";
        }
        String tag = pageClass.getName() +
                (TextUtils.isEmpty(nickName) ? "" : "(" + nickName + ")");

        PageFragmentContainer page = this.getFragmentsPage();
        if (page == null) {
            return tag;
        }

        FragmentManager fm = page.getSupportFragmentManager();
        if (fm.findFragmentByTag(tag) != null) {
            return tag;
        }

        List<Fragment> fragments = fm.getFragments();
        if (fragments == null) {
            return tag;
        }

        for (Fragment f : fragments) {
            if (f == null || !f.getClass().equals(pageClass) || !(f instanceof SdkFragment)) {
                continue;
            }
            String fNick = ((SdkFragment) f).getNickName();
            if (TextUtils.equals(nickName, fNick)) {
                tag = f.getTag();
                break;
            }
        }

        return tag;
    }

    int findFragmentIndex(Fragment fragment) {
        for (int i = 0; i < fragmentList.size(); i++) {
            WeakReference<SdkFragment> item = fragmentList.get(i);
            if (item == null || item.get() == null) {
                continue;
            }
            if (item.get() == fragment) {
                return i;
            }
        }

        return -1;
    }

    SdkFragment findPreFragment(Fragment fragment) {
        int index = findFragmentIndex(fragment);
        if (index > 0) {
            return fragmentList.get(index - 1).get();
        }
        return null;
    }

    private Fragment findFragment(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return null;
        }
        for (int i = 0; i < fragmentList.size(); i++) {
            WeakReference<SdkFragment> item = fragmentList.get(i);
            if (item == null || item.get() == null) {
                continue;
            }
            if (item.get().getTag() != null && item.get().getTag().equals(tag)) {
                return item.get();
            }
        }

        return null;
    }

    SdkFragment findFragment(int index) {
        if (index < 0 || index >= size()) {
            return null;
        }
        return fragmentList.get(index).get();
    }
}
