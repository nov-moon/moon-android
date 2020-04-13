package com.meili.component.uploadimg.upload;

import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.upload.oss.model.MLChannelInfoModel;

/**
 * 上传服务的委托类定义，主要提供初始化上传组件，添加任务，停止任务的定义
 * Created by imuto on 17/11/13.
 */
public interface UploadServiceDelegate {
    /** 添加一个上传任务 */
    void add(UploadRequest model);

    /** 停止上传 */
    void stop();

    /** 初始化上传组件 */
    void initClient(MLChannelInfoModel model, MLConfig config);

    /** 当前上传组件是否已经初始化 */
    boolean hasInitToken();
}
