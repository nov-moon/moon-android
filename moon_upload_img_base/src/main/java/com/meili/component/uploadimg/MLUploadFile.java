package com.meili.component.uploadimg;

import android.app.Application;
import android.support.annotation.MainThread;
import android.text.TextUtils;
import android.util.SparseArray;

import com.meili.component.uploadimg.MLConfig.MLConfigurable;
import com.meili.component.uploadimg.common.MLImgUploadTaskDefine;
import com.meili.component.uploadimg.exception.ErrorEnum;
import com.meili.component.uploadimg.exception.ExceptionUtils;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.http.HttpSdk;
import com.meili.moon.sdk.util.ArrayUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传图片SDK的主API类
 * Created by imuto on 17/11/10.
 */
public class MLUploadFile {

    public class TargetFlags {
        public static final int DEF_IMG = -1;
        public static final int DEF_VIDEO = -2;
    }

    private static SparseArray<MLConfigurable> mConfig = new SparseArray<>();

    /**
     * 初始化SDK，必须在主线程调用，只需要初始化一次即可
     */
    @MainThread
    public static void init(Application application, String channelId, String channelPublicKey, int targetFlag) {
        HttpSdk.init(application);
        MLImgUploadTaskDefine.getInstance();

        ConfigImpl config = new ConfigImpl();
        MLConfigOptions configOptions = config.getConfigOptions();
        configOptions.setChannel(channelId);
        configOptions.setChannelOwner(channelId);
        configOptions.setChannelPublicKey(channelPublicKey);

        mConfig.put(targetFlag, config);
        config.getUploadService().init();
    }

    /**
     * 配置sdk, 可使用{@link MLConfigOptions.Build} 类构建options
     */
    public static void config(MLConfigOptions options, int targetFlag) {
        MLConfigurable oldConfig = mConfig.get(targetFlag);

        if (TextUtils.isEmpty(options.getChannelId())) {
            options.setChannel(oldConfig.getChannelId());
        }
        if (TextUtils.isEmpty(options.getChannelPublicKey())) {
            options.setChannelPublicKey(oldConfig.getChannelPublicKey());
        }
        if (TextUtils.isEmpty(options.getChannelOwner())) {
            options.setChannelOwner(oldConfig.getChannelOwner());
        }
        oldConfig.configOptions(options);
        oldConfig.getUploadService().init();
    }

    public static MLConfigurable getConfig(int targetFlag) {
        return mConfig.get(targetFlag);
    }

    /***
     * 上传，上传一张图片。
     * @param model 需要上传的model
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(MLUploadModel model, int targetFlag, MLCallback.MLUploadCallback<? extends MLUploadModel> callback) {
        upload(model, null, targetFlag, callback);
    }

    /***
     * 上传，上传一张图片。
     * @param model 需要上传的model
     * @param option 自定义上传选项
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(MLUploadModel model, MLUploadOption option, int targetFlag, MLCallback.MLUploadCallback<? extends MLUploadModel> callback) {
        MLConfigurable config = mConfig.get(targetFlag);
        if (config == null) {
            throw new BaseException("请调用 MLUploadImg.init() 方法先初始化sdk");
        }
        config.getUploadService().upload(model, option, callback);
    }

    /***
     * 上传，上传一组图片。
     * @param data 需要上传的model列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(List<? extends MLUploadModel> data, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        upload(data, null, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。
     * @param data 需要上传的model列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(List<? extends MLUploadModel> data, MLUploadOption option, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        MLConfigurable config = mConfig.get(targetFlag);

        if (config == null) {
            throw new BaseException("请调用 MLUploadImg.init() 方法先初始化sdk");
        }
        config.getUploadService().upload(data, option, callback);
    }

    /***
     * 上传，上传一张图片。
     * @param imgFilePath 需要上传的图片路径
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(String imgFilePath, int targetFlag, MLCallback.MLUploadCallback<MLUploadModel> callback) {
        upload(imgFilePath, null, targetFlag, callback);
    }

    /***
     * 上传，上传一张图片。结果回调的model为默认实现{@link MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePath 需要上传的图片路径
     * @param option 自定义上传选项
     * @param callback 上传的返回结果
     */
    @MainThread
    public static void upload(String imgFilePath, MLUploadOption option, int targetFlag, MLCallback.MLUploadCallback<MLUploadModel> callback) {
        upload(new MLUploadModel.DefUploadModelImpl(imgFilePath), option, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(ArrayList<String> imgFilePaths, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        upload(imgFilePaths, null, targetFlag, callback);
    }


    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(ArrayList<String> imgFilePaths, MLUploadOption option, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        upload(imgFilePaths.toArray(new String[imgFilePaths.size()]), option, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(String[] imgFilePaths, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        upload(imgFilePaths, null, targetFlag, callback);
    }

    /***
     * 上传，上传一组图片。结果回调的model为默认实现{@link MLUploadModel.DefUploadModelImpl}，可进行强转使用
     * @param imgFilePaths 需要上传的图片列表
     * @param option 自定义上传选项
     * @param callback 上传的返回结果，每上传成功一个，则调用一次返回结果
     */
    @MainThread
    public static void upload(String[] imgFilePaths, MLUploadOption option, int targetFlag, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        if (mConfig == null) {
            throw new BaseException("请调用 MLUploadImg.init() 方法先初始化sdk");
        }
        if (ArrayUtil.isEmpty(imgFilePaths)) {
            if (callback != null) {
                String msg = "数据集合为空";
                callback.onError(ExceptionUtils.wrapException(ErrorEnum.PARAMS_CHECK, msg), msg);
            }
            return;
        }
        List<MLUploadModel> data = new ArrayList<>();
        for (String imgFilePath : imgFilePaths) {
            data.add(new MLUploadModel.DefUploadModelImpl(imgFilePath));
        }
        upload(data, option, targetFlag, callback);
    }

    /**
     * 停止上传，并且正在上传的不会得到回调,如果没有正在上传的内容，则什么都不操作
     */
    @MainThread
    public static void stop(int targetFlag) {
        MLConfigurable config = mConfig.get(targetFlag);

        if (config == null) {
            throw new BaseException("请调用 MLUploadImg.init() 方法先初始化sdk");
        }
        config.getUploadService().stop();
    }
}
