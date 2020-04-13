package com.meili.moon.sdk.page;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;

import com.meili.moon.sdk.CommonSdk;
import com.meili.moon.sdk.exception.JsonException;

import java.io.Serializable;


/**
 * 可用来打开Fragment和Activity的intent
 */
public class PageIntent extends Intent {

    public static final String INNER_DATA_KEY = "$ml__";
    public static final String ACTIVITY_INFO_KEY = INNER_DATA_KEY + "activity_info__";
    private InnerData innerData;

    public PageIntent() {
        super();
    }

    public PageIntent(String pageName) {
        this(pageName, (String) null);
    }

    public PageIntent(String action, Uri uri) {
        super(action, uri);
    }

    public PageIntent(String pageName, String nickName) {
        super();
        this.setPageName(pageName);
        this.setNickName(nickName);
    }

    public PageIntent(String pageName, String nickName, String packageName) {
        this(pageName, nickName);
        this.setPackage(packageName);
    }

    public PageIntent(Class<?> cls) {
        super(CommonSdk.environment().app(), cls);
        this.setPageName(cls.getName());
    }

    public PageIntent(Intent intent) {
        super(intent);
        putExtras(intent.getExtras());
    }

    @Override
    public PageIntent setPackage(String packageName) {
        super.setPackage(packageName);
        return this;
    }

    @Override
    public PageIntent setAction(String action) {
        super.setAction(action);
        return this;
    }

    @Override
    public PageIntent putExtras(Intent src) {
        Bundle extras = src != null ? src.getExtras() : null;
        this.putExtras(extras);
        return this;
    }

    @Override
    public PageIntent putExtras(Bundle extras) {
        if (extras == null) {
            return this;
        }
        if (!TextUtils.isEmpty(extras.getString(INNER_DATA_KEY))) {
            InnerData data = getInnerDataFromBundle(extras);
            if (data != null) {
                if (!getInnerData().equals(data)) {
                    getInnerData().reset(data);
                    syncInnerData();
                }
            }
            extras.remove(INNER_DATA_KEY);
        }
        super.putExtras(extras);
        return this;
    }

    public PageIntent reset(PageIntent pageIntent) {
        if (pageIntent == null) {
            return this;
        }
        InnerData data = getInnerDataFromBundle(pageIntent.getExtras());
        getInnerData().reset(data);
        setPageName(pageIntent.getPageName());
        setNickName(pageIntent.getNickName());
        super.putExtras(pageIntent.getExtras());
        syncInnerData();
        return this;
    }

    @Override
    public Bundle getExtras() {
        syncInnerData();
        return super.getExtras();
    }

    public String getPageName() {
        return getInnerData().pageName;
    }

    public PageIntent setPageName(String pageName) {
//        if (this.getComponent() == null) {
//            this.setAction(PageHelper.ACTION_PREFIX + pageName);
//        } else {
        getInnerData().pageName = pageName;
        syncInnerData();
//        }
        return this;
    }

    public String getNickName() {
        String result = getInnerData().nickName;
//        if (TextUtils.isEmpty(result)) {
//            result = System.currentTimeMillis() + "";
//            setNickName(result);
//        }
        return result;
    }

    public PageIntent setNickName(String nickName) {
        getInnerData().nickName = nickName;
        syncInnerData();
        return this;
    }

    public int[] getAnimations() {
        int[] result = getInnerData().animations;
        if (result == null || result.length == 0) {
            result = PageAnims.DEFAULT.getAnimations();
        }
        return result;
    }

    public PageIntent setAnimations(int[] animations) {
        getInnerData().animations = animations;
        syncInnerData();
        return this;
    }

    public boolean isOverlayFragment() {
        return getInnerData().isOverlayFragment;
    }

    public PageIntent setOverlayFragment(boolean overlayFragment) {
        getInnerData().isOverlayFragment = overlayFragment;
        syncInnerData();
        return this;
    }

    public PageIntent setPageClass(Class<?> pageClass) {
        getInnerData().pageClass = pageClass;
        syncInnerData();
        return this;
    }

    public Class<?> getPageClass() {
        return getInnerData().pageClass;
    }

    public PageIntent setActivityInfo(ActivityInfo info) {
        putExtra(ACTIVITY_INFO_KEY, info);
        return this;
    }

    public ActivityInfo getActivityInfo() {
        return getParcelableExtra(ACTIVITY_INFO_KEY);
    }

    public PageIntent setFragmentContainerId(long id) {
        getInnerData().fragmentContainerId = id;
        syncInnerData();
        return this;
    }

    public long getFragmentContainerId() {
        return getInnerData().fragmentContainerId;
    }

    public PageIntent setRequestCode(int requestCode) {
        getInnerData().requestCode = requestCode;
        syncInnerData();
        return this;
    }

    public int getRequestCode() {
        return getInnerData().requestCode;
    }

    public PageIntent setLastFragmentTag(String tag) {
        getInnerData().lastFragmentTag = tag;
        syncInnerData();
        return this;
    }

    public String getLastFragmentTag() {
        return getInnerData().lastFragmentTag;
    }

    private void syncInnerData() {
        try {
            this.putExtra(INNER_DATA_KEY, CommonSdk.json().toJson(getInnerData()));
        } catch (JsonException e) {
            e.printStackTrace();
        }
    }

    private InnerData getInnerDataFromBundle(Bundle bundle) {
        InnerData innerData = null;
        if (bundle != null) {
            try {
                Object obj = bundle.get(INNER_DATA_KEY);
                if (obj instanceof InnerData) {
                    innerData = (InnerData) obj;
                } else if (obj instanceof String) {
                    String json = (String) obj;
                    if (!TextUtils.isEmpty(json)) {
                        innerData = CommonSdk.json().toObject(json, InnerData.class);
                    }
                }
            } catch (Exception e) {
                innerData = null;
            }
            return innerData;
        } else {
            return null;
        }
    }

    private InnerData getInnerData() {
        if (innerData != null) {
            return innerData;
        }
        innerData = getInnerDataFromBundle(super.getExtras());
        if (innerData == null) {
            innerData = new InnerData();
        }
        return innerData;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        syncInnerData();
        removeExtra(INNER_DATA_KEY);
        this.putExtra(INNER_DATA_KEY, CommonSdk.json().toJson(getInnerData()));
        super.writeToParcel(out, flags);
    }

    @Override
    public void readFromParcel(Parcel in) {
        super.readFromParcel(in);
    }

    public static final Creator<PageIntent> CREATOR
            = new Creator<PageIntent>() {
        public PageIntent createFromParcel(Parcel source) {
            PageIntent pageIntent = new PageIntent();
            pageIntent.readFromParcel(source);
            return pageIntent;
        }

        public PageIntent[] newArray(int size) {
            return new PageIntent[size];
        }
    };

    private static class InnerData implements Serializable {
        private String pageName;
        private String nickName;
        private int[] animations;
        private boolean isOverlayFragment;
        private long fragmentContainerId;
        private String lastFragmentTag;
        private int requestCode = -1;
        private Class<?> pageClass;

        public InnerData() {
        }

        public InnerData reset(InnerData data) {
            this.requestCode = data.requestCode;
            this.isOverlayFragment = data.isOverlayFragment;
            if (this.animations == null) {
                this.animations = data.animations;
            }
            if (this.fragmentContainerId < 1) {
                this.fragmentContainerId = data.fragmentContainerId;
            }
            if (TextUtils.isEmpty(this.lastFragmentTag)) {
                this.lastFragmentTag = data.lastFragmentTag;
            }
            if (this.pageClass == null) {
                this.pageClass = data.pageClass;
            }
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InnerData data = (InnerData) o;

            if (pageName != null ? !pageName.equals(data.pageName) : data.pageName != null) {
                return false;
            }
            return nickName != null ? nickName.equals(data.nickName) : data.nickName == null;

        }

        @Override
        public int hashCode() {
            int result = pageName != null ? pageName.hashCode() : 0;
            result = 31 * result + (nickName != null ? nickName.hashCode() : 0);
            return result;
        }

        public String getPageName() {
            return pageName;
        }

        public void setPageName(String pageName) {
            this.pageName = pageName;
        }

        public String getNickName() {
            return nickName;
        }

        public void setNickName(String nickName) {
            this.nickName = nickName;
        }

        public int[] getAnimations() {
            return animations;
        }

        public void setAnimations(int[] animations) {
            this.animations = animations;
        }

        public boolean isOverlayFragment() {
            return isOverlayFragment;
        }

        public void setOverlayFragment(boolean overlayFragment) {
            isOverlayFragment = overlayFragment;
        }

        public long getFragmentContainerId() {
            return fragmentContainerId;
        }

        public void setFragmentContainerId(long fragmentContainerId) {
            this.fragmentContainerId = fragmentContainerId;
        }

        public String getLastFragmentTag() {
            return lastFragmentTag;
        }

        public void setLastFragmentTag(String lastFragmentTag) {
            this.lastFragmentTag = lastFragmentTag;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public void setRequestCode(int requestCode) {
            this.requestCode = requestCode;
        }

        public Class<?> getPageClass() {
            return pageClass;
        }

        public void setPageClass(Class<?> pageClass) {
            this.pageClass = pageClass;
        }

    }
}
