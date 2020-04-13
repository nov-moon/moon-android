package com.meili.component.uploadimg.upload.oss.model;

/**
 * OSS上传结果的信息类，用户可以实现当前接口获取OSS上传后的结果信息
 * Created by imuto on 2018/4/2.
 */
public interface MLOSSUploadInfo {

    /** 设置oss的objectKey，只有在图片上传成功后才会调用 */
    void setObjectKey(String objectKey);

    /** 设置oss上传成功后的url */
    void setOssUrl(String url);

}
