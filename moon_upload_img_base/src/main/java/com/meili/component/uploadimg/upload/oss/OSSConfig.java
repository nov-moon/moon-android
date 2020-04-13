package com.meili.component.uploadimg.upload.oss;

import java.io.Serializable;

/**
 * Created by imuto on 17/11/13.
 */
/*package*/class OSSConfig implements Serializable {
    /** 阿里服务器域名 */
    public String endpoint;
    /** 阿里bucketName */
    public String bucketName;
    /** 文件名的前缀 */
    public String objectKeyPrefix;

    public void clear() {
        endpoint = null;
        bucketName = null;
        objectKeyPrefix = null;
    }
}
