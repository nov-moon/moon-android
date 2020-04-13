package com.meili.component.uploadimg.upload.oss.model;


import com.meili.moon.sdk.http.common.BaseModel;

/**
 * 调用服务器绑定关系接口后返回的数据类型
 * Created by imuto on 2018/4/2.
 */
public class MLBindRelationResultModel extends BaseModel {
    private String uuid;
    private String url;
    private String path;
    private String msg;
    private Boolean success;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }
}
