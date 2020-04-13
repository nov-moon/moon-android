package com.meili.component.uploadimg.upload.oss.model;


import com.meili.moon.sdk.http.common.BaseModel;

/**
 * 服务器端返回的app信息
 * Created by imuto on 17/11/13.
 */
public class MLChannelInfoModel extends BaseModel {
    /** oss的accessKeyID */
    private String accessKeyID;
    /** oss的accessKeySecret */
    private String accessKeySecret;
    /** oss的endpoint */
    private String endpoint;
    /** oss的token */
    private String token;
    /** oss的tokenExpireTime */
    private String tokenExpireTime;
    /** oss的bucketName */
    private String bucketName;

    public String getAccessKeyID() {
        return accessKeyID;
    }

    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenExpireTime() {
        return tokenExpireTime;
    }

    public void setTokenExpireTime(String tokenExpireTime) {
        this.tokenExpireTime = tokenExpireTime;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
