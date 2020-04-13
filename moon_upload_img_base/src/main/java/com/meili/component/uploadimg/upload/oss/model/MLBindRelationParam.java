package com.meili.component.uploadimg.upload.oss.model;


import com.meili.component.uploadimg.http.MLApiRequestParam;
import com.meili.moon.sdk.http.annotation.HttpRequest;

/**
 * 绑定oss数据到影像件系统
 * Created by imuto
 */
@HttpRequest("mapper/relation/records")
public class MLBindRelationParam extends MLApiRequestParam {
    /** 调用方名称 */
    public String caller;
    /** 文件信息List的json数据 */
    public String fileInfoList;
}
