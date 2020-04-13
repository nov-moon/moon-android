package com.meili.component.uploadimg;

import android.app.Application;
import android.support.annotation.MainThread;

import com.mljr.moon.imgcompress.MNCompressConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传图片SDK的主API类
 * Created by imuto on 17/11/10.
 */
public class MLUploadImg {

    private static int targetFlag = MLUploadFile.TargetFlags.DEF_IMG;

    /**
     * 初始化SDK，必须在主线程调用，只需要初始化一次即可
     */
    @MainThread
    public static void init(Application application, String channelId, String channelPublicKey) {
        MLUploadFile.init(application, channelId, channelPublicKey, targetFlag);

        MLConfigOptions configOptions = MLUploadFile.getConfig(targetFlag).getConfigOptions();
        configOptions.setImageUpload(true);

        MNCompressConfig.initialize(application);
    }

    /** 配置sdk, 可使用{@link MLConfigOptions.Build} 类构建options */
    public static void config(MLConfigOptions options) {
        options.setImageUpload(true);
        MLUploadFile.config(options, targetFlag);
    }

    /***
     * 上传，上传一张图片。
     * @param model 需要上传的model
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(MLUploadModel model, MLCallback.MLUploadCallback<? extends MLUploadModel> callback) {
        MLUploadFile.upload(model, null, targetFlag, callback);
    }

    /***
     * 上传，上传一张图片。
     * @param model 需要上传的model
     * @param option 自定义上传选项
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(MLUploadModel model, MLUploadOption option, MLCallback.MLUploadCallback<? extends MLUploadModel> callback) {
        MLUploadFile.upload(model, option, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。
     * @param data 需要上传的model列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(List<? extends MLUploadModel> data, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(data, null, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。
     * @param data 需要上传的model列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(List<? extends MLUploadModel> data, MLUploadOption option, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(data, option, targetFlag, callback);
    }

    /***
     * 上传，上传一张图片。
     * @param imgFilePath 需要上传的图片路径
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(String imgFilePath, MLCallback.MLUploadCallback<MLUploadModel> callback) {
        MLUploadFile.upload(imgFilePath, null, targetFlag, callback);
    }

    /***
     * 上传，上传一张图片。结果回调的model为默认实现{@link com.meili.component.uploadimg.MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePath 需要上传的图片路径
     * @param option 自定义上传选项
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(String imgFilePath, MLUploadOption option, MLCallback.MLUploadCallback<MLUploadModel> callback) {
        MLUploadFile.upload(imgFilePath, option, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link com.meili.component.uploadimg.MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(ArrayList<String> imgFilePaths, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(imgFilePaths, null, targetFlag, callback);
    }


    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link com.meili.component.uploadimg.MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(ArrayList<String> imgFilePaths, MLUploadOption option, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(imgFilePaths, option, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link com.meili.component.uploadimg.MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(String[] imgFilePaths, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(imgFilePaths, null, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link com.meili.component.uploadimg.MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(String[] imgFilePaths, MLUploadOption option, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLUploadFile.upload(imgFilePaths, option, targetFlag, callback);
    }

    /** 停止上传，并且正在上传的不会得到回调,如果没有正在上传的内容，则什么都不操作 */
    @MainThread
    public static void stop() {
        MLUploadFile.stop(targetFlag);
    }
}
