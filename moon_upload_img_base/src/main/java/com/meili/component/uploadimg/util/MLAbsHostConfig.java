package com.meili.component.uploadimg.util;

/**
 * 配置当前sdk的访问地址，host地址格式（scheme://host:port/），port根据实际情况可有可无。
 * <ul>
 * <li>线上的host使用了默认的实现</li>
 * </ul>
 * <p>
 * Created by imuto on 17/10/23.
 */
public abstract class MLAbsHostConfig<T> implements MLHostConfig<T> {

    @Override
    public String getHost() {
        return "http://carrier.mljr.com/";
    }

}
