package com.meili.moon.sdk.page.internal;

import com.meili.moon.sdk.page.PageIntent;

/**
 * 容纳管理fragment的容器
 */
interface FragmentContainer {

    /** 获取container的Id信息 */
    long getId();

    /** 获取affinity信息 */
    String getAffinity();

    /**
     * 获取当前container,是否是活跃状态
     */
    boolean isAlive();

    /**
     * 获取当前container的顶部fragment
     */
    SdkFragment getTopFragment();

    /**
     * 获取栈低的fragment
     */
    SdkFragment getBottomFragment();

    /**
     * 当前container是否包含intent指定的fragment
     * <br/>具体判断标准,由实现类决定
     */
    boolean contains(PageIntent intent);

    /**
     * 打开一个新的fragment
     */
    void startFragment(PageIntent intent, int requestCode);

    /**
     * 从堆栈中查找fragment,如果没有则打开一个新的,如果有则回退到fragment
     */
    void gotoFragment(PageIntent intent, int requestCode);

    int finish(int step, SdkFragment page);

    void finishAll();

    void onFragmentsPageCreate(PageFragmentContainer pageFragmentContainer);

    PageFragmentContainer getFragmentsPage();

    boolean isEmpty();

    int size();

    void setOnContainerFinished(OnContainerFinishedListener listener);

    interface OnContainerFinishedListener {
        void onContainerFinished(FragmentContainer container);
    }
}
