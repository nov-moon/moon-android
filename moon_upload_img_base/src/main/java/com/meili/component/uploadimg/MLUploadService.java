package com.meili.component.uploadimg;

import java.util.List;

/**
 * 统一的upload服务接口
 * Created by imuto on 17/11/10.
 */
public interface MLUploadService {
    /***
     * 上传，如果只上传一个图片，调用此方法。如果调用的是MLUploadService一个实例，则自动放到上传队列。
     * @param model 需要上传的model
     * @param option 自定义上传选项
     * @param callback 上传的返回结果
     */
    void upload(MLUploadModel model, MLUploadOption option, MLCallback.MLUploadCallback<? extends MLUploadModel> callback);

    /***
     * 上传，上传一组图片。如果调用的是MLUploadService一个实例，则自动放到上传队列。
     * @param data 需要上传的model列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    void upload(List<? extends MLUploadModel> data, MLUploadOption option, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback);

    void init();

    void stop();
}
