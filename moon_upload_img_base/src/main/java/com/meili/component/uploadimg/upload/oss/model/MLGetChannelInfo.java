package com.meili.component.uploadimg.upload.oss.model;


import com.meili.component.uploadimg.http.MLApiRequestParam;
import com.meili.moon.sdk.http.annotation.HttpRequest;

/**
 * 获取oss的token等信息
 * Created by imuto
 */
@HttpRequest("mapper/oss/getToken")
public class MLGetChannelInfo extends MLApiRequestParam {
    /** 调用方名称 */
    public String caller;
}
