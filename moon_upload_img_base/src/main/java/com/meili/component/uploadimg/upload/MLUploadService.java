package com.meili.component.uploadimg.upload;

import com.meili.component.uploadimg.MLCallback;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.exception.ErrorEnum;
import com.meili.component.uploadimg.exception.ExceptionUtils;
import com.meili.component.uploadimg.upload.mljr.MLMljrUploadServiceDelegate;
import com.meili.component.uploadimg.upload.oss.MLOSSUploadServiceDelegate;
import com.meili.component.uploadimg.upload.oss.model.MLChannelInfoModel;
import com.meili.component.uploadimg.upload.oss.model.MLGetChannelInfo;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.common.Callback;
import com.meili.moon.sdk.common.Cancelable;
import com.meili.moon.sdk.http.HttpSdk;
import com.meili.moon.sdk.http.IHttpResponse;
import com.meili.moon.sdk.log.LogUtil;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 默认的上传服务
 * Created by imuto on 17/11/10.
 */
public class MLUploadService implements com.meili.component.uploadimg.MLUploadService {

    private UploadRequest mRequestModel;

    private UploadServiceDelegate mDelegate;
    private UploadServiceDelegate mDelegateAlternative;

    private Cancelable cancelable;
    private MLConfig mConfig;

    public MLUploadService(MLConfig config) {
        mConfig = config;
        mDelegateAlternative = new MLMljrUploadServiceDelegate();
        mDelegateAlternative.initClient(null, mConfig);
        mDelegate = new MLOSSUploadServiceDelegate();
    }

    @Override
    public void upload(MLUploadModel model, MLUploadOption option, MLCallback.MLUploadCallback<? extends MLUploadModel> callback) {
        UploadRequest uploadModel = new UploadRequest(model);
        uploadModel.setCallback(callback);
        uploadModel.setOption(option);
        upload(uploadModel);
    }

    @Override
    public void upload(List<? extends MLUploadModel> data, MLUploadOption option, MLCallback.MLUploadCallback<List<? extends MLUploadModel>> callback) {
        UploadRequest uploadModel = new UploadRequest(data);
        uploadModel.setCallback(callback);
        uploadModel.setOption(option);
        upload(uploadModel);
    }

    private void upload(UploadRequest model) {
        if (model == null) {
            return;
        }
        //如果option!=null，则验证option
        if (model.getOption() != null) {
            String validate = model.getOption().validate();
            if (validate != null) {
                model.getCallback().onError(ExceptionUtils.wrapException(ErrorEnum.PARAMS_CHECK, validate), validate);
                return;
            }
        }
        if (!mDelegate.hasInitToken()) {
            if (mRequestModel != null) {
                String msg = "上一次上传尚未完成";
                model.getCallback().onError(ExceptionUtils.wrapException(ErrorEnum.TOKEN, msg), msg);
                if (model.getCallback() instanceof MLCallback.MLFinishCallback) {
                    ((MLCallback.MLFinishCallback) model.getCallback()).onFinish(false);
                }
                return;
            }
            mRequestModel = model;
            init();
            return;
        }
        mDelegate.add(model);
    }

    @Override
    public void init() {
        if (cancelable != null) {
            try {
                cancelable.cancel(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        LogUtil.e("开始初始化token信息");

//        if (true) {
//            LogUtil.e("进入备选方案");
//            mDelegateAlternative.add(mRequestModel);
//            return;
//        }

        final MLGetChannelInfo param = new MLGetChannelInfo();
        param.caller = mConfig.getChannelId();

        cancelable = HttpSdk.http().post(param, new Callback.SimpleCallback<MLChannelInfoModel>() {
            @Override
            public void onSuccess(MLChannelInfoModel result) {
                super.onSuccess(result);
                LogUtil.e("初始化token信息成功");

                mDelegate.initClient(result, mConfig);

                upload(mRequestModel);
            }

            @Override
            public void onError(@NotNull BaseException exception) {
                super.onError(exception);
                LogUtil.e("初始化token信息失败");
                IHttpResponse response = param.getResponse();
                if (response.getState() == -1) {
                    LogUtil.e("进入备选方案");
                    mDelegateAlternative.add(mRequestModel);
                } else if (mRequestModel != null) {
                    String msg = "初始化channel信息错误";
                    mRequestModel.getCallback().onError(ExceptionUtils.wrapException(ErrorEnum.TOKEN, msg), msg);
                    if (mRequestModel.getCallback() instanceof MLCallback.MLFinishCallback) {
                        ((MLCallback.MLFinishCallback) mRequestModel.getCallback()).onFinish(false);
                    }
                }
            }

            @Override
            public void onFinished(boolean isSuccess) {
                super.onFinished(isSuccess);
                mRequestModel = null;
                cancelable = null;
            }
        });
    }

    @Override
    public void stop() {
        mDelegate.stop();
    }

}
