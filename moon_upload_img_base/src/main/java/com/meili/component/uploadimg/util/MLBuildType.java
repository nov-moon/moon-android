package com.meili.component.uploadimg.util;

/**
 * 当前编译类型
 * Created by imuto on 17/10/23.
 */
public enum MLBuildType {

    DEBUG(0),
    RELEASE(1);

    private int value;

    private MLBuildType(int v) {
        value = v;
    }

    public int getValue() {
        return value;
    }
}
