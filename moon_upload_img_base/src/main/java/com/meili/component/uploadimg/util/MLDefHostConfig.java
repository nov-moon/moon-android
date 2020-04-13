package com.meili.component.uploadimg.util;

import android.util.SparseArray;

/**
 * sdk的默认host配置
 * <p>
 * Created by imuto on 17/10/23.
 */
public class MLDefHostConfig extends MLAbsHostConfig<Integer> {

    private SparseArray<String> mArray = new SparseArray<>();

    public MLDefHostConfig() {
        mArray.append(0, "http://218.17.119.179:30300/carrier-web/");
//        mArray.append(0, "http://192.168.49.133:30300/");
    }

    @Override
    public String getHostForDebug(Integer key) {
        return mArray.get(key);
    }
}
