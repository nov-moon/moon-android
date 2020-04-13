package com.meili.component.uploadimg.util;

/**
 * 配置当前sdk的访问地址，host地址格式（scheme://host:port/），port根据实际情况可有可无。
 * Created by imuto on 17/10/23.
 */
public interface MLHostConfig<T> {

    /** 获取线上host配置 */
    String getHost();

    /** 获取测试host配置 */
    String getHostForDebug(T key);
}
