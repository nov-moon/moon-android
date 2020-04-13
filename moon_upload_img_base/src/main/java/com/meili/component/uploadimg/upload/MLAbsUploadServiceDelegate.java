package com.meili.component.uploadimg.upload;

import android.os.Bundle;

import com.meili.component.uploadimg.MLCallback;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.exception.ErrorEnum;
import com.meili.component.uploadimg.exception.ExceptionUtils;
import com.meili.component.uploadimg.exception.MLUploadImgException;
import com.meili.component.uploadimg.upload.oss.model.MLChannelInfoModel;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.common.Cancelable;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.msg.BaseMessage;
import com.meili.moon.sdk.msg.MessageCallback;
import com.meili.moon.sdk.util.ArrayUtil;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 实现了上传委托类。以队列的形式管理上传请求
 * Created by imuto on 17/11/13.
 */
public abstract class MLAbsUploadServiceDelegate<ItemResultType> implements UploadServiceDelegate {

    /**
     * 当前上传任务的队列
     */
    protected Queue<UploadRequest> uploadQueue = new LinkedList<>();
    /**
     * 当前正在上传的task，如果没有正在上传的任务，则为null
     */
    protected Cancelable currTask;
    /**
     * 当前正在上传的请求
     */
    protected UploadRequest currUploadRequest;

    /**
     * 是否已经停止
     */
    protected boolean hasStopAll;

    protected MLConfig mConfig;

    @Override
    public void initClient(MLChannelInfoModel model, MLConfig config) {
        mConfig = config;
    }

    @Override
    public void add(UploadRequest model) {

        hasStopAll = false;

        //将上传的model加到队列
        if (model != null) {
            uploadQueue.offer(model);
        }
        upload();
    }

    @Override
    public void stop() {
        LogUtil.e("正在停止...");
        hasStopAll = true;
        if (currTask != null) {
            currTask.cancel(true);
        }
        if (currUploadRequest != null) {
            uploadQueue.remove(currUploadRequest);
        }
        uploadQueue.clear();
        currTask = null;
        currUploadRequest = null;
        LogUtil.e("已经停止！");
    }

    /**
     * 开始上传
     */
    private void upload() {
        if (currUploadRequest != null) {
            return;
        }
        if (ArrayUtil.isEmpty(uploadQueue)) {
            return;
        }

        uploadCircle();
    }

    /**
     * 循环检查上传队列，如果有上传任务则执行上传，没有则结束上传
     */
    protected void uploadCircle() {
        if (hasStopAll) {
            return;
        }
        currTask = null;
        if (ArrayUtil.isEmpty(uploadQueue)) {
            LogUtil.d("所有的任务上传完成");
            return;
        }

        currUploadRequest = uploadQueue.peek();
        List<? extends MLUploadModel> currUploadList = currUploadRequest.getData();
        MLCallback.MLUploadCallback callback = currUploadRequest.getCallback();

        // 检查当前request中的文件是否已经上传完成，如果已经上传完成，则进行下一个request轮训
        // 如果还没有完成，则进行上传操作
        if (currUploadRequest.hasFinish()) {
            onCurrRequestFinish();
        } else {
            uploadItemNew(currUploadList.get(currUploadRequest.getUploadCount()), callback, currUploadRequest.getOption());
        }
    }

    protected void onCurrRequestFinish() {
        try {
            if (!currUploadRequest.isHasUploadSuccess()) {
                onUploadError(new BaseException("上传错误"));
            } else {
                onUploadSuccess();
            }
            onUploadFinish();

            uploadQueue.remove(currUploadRequest);
            currUploadRequest = null;
        } catch (Exception e) {
            e.printStackTrace();
            onUploadError(e);
        }
    }

    /**
     * 上传一个指定model
     */
    protected void uploadItemNew(final MLUploadModel model, final MLCallback.MLUploadCallback callback, MLUploadOption option) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("options", option);
        bundle.putSerializable("data", model);
        bundle.putSerializable("config", mConfig);

        onTaskBundle(bundle);

        BaseMessage message = new BaseMessage(getUploadTaskName());
        message.setArguments(bundle);
        message.setCallback(new MessageCallback() {
            @Override
            public void onSuccess(Object result) {
                onItemUploadSuccess((ItemResultType) result, model, callback);
            }

            @Override
            public void onError(@NotNull BaseException exception) {
                super.onError(exception);
                onItemUploadError(exception, model, callback);
                if (mConfig != null && mConfig.isDebug()) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void onProgress(long curr, long total) {
                super.onProgress(curr, total);
                onItemProgress(curr, total, model, callback);
            }

            @Override
            public void onFinished(boolean isSuccess) {
                super.onFinished(isSuccess);
                onItemUploadFinish(isSuccess, model, callback);
            }

        });
        currTask = message.send();
    }

    protected void onItemUploadSuccess(ItemResultType result, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        if (callback instanceof MLCallback.MLItemCallback) {
            ((MLCallback.MLItemCallback) callback).onItemUploadSuccess(model);
        }
        currUploadRequest.setHasUploadSuccess(true);
    }

    protected void onItemUploadError(BaseException exception, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        if (callback instanceof MLCallback.MLItemCallback) {
            ((MLCallback.MLItemCallback) callback).onItemUploadError(
                    model, ExceptionUtils.wrapException(ErrorEnum.OSS, exception), exception.getMessage());
        }
        currUploadRequest.setAllUploadSuccess(false);
    }

    protected void onItemProgress(long curr, long total, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        if (callback instanceof MLCallback.MLItemCallback) {
            ((MLCallback.MLItemCallback) callback).onItemProgress(model, total, curr);
        }
    }

    protected void onItemUploadFinish(boolean isSuccess, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        currUploadRequest.increaseUploadCount();
        callbackItemProgress(model);
        uploadCircle();
    }

    protected void onUploadSuccess() {
        //回调操作
        try {
            Object result;
            if (currUploadRequest.isList()) {
                result = currUploadRequest.getData();
            } else {
                result = currUploadRequest.getData().get(0);
            }

            currUploadRequest.getCallback().onSuccess(result);
        } catch (Exception e) {
            e.printStackTrace();
            currUploadRequest.getCallback().onError(
                    new MLUploadImgException(ErrorEnum.BIND_RELATION, e), e.getMessage());
        }
    }

    protected void onUploadError(Exception exception) {
        MLUploadImgException e = ExceptionUtils.wrapException(ErrorEnum.BIND_RELATION, exception);
        currUploadRequest.getCallback().onError(e, e.getMessage());
    }

    protected void onUploadFinish() {
        currUploadRequest.setHasFinishBind(true);
        if (currUploadRequest.getCallback() instanceof MLCallback.MLFinishCallback) {
            ((MLCallback.MLFinishCallback) currUploadRequest.getCallback()).onFinish(currUploadRequest.isAllUploadSuccess());
        }
    }

    protected void onTaskBundle(Bundle bundle) {

    }

    protected abstract String getUploadTaskName();

    /**
     * 进行item完成的进度回调
     */
    protected void callbackItemProgress(MLUploadModel model) {
        if (currUploadRequest.getCallback() instanceof MLCallback.MLProgressCallback) {
            int total = currUploadRequest.getTotal();
            int curr = currUploadRequest.getUploadCount();
            ((MLCallback.MLProgressCallback) currUploadRequest.getCallback()).onProgress(model, total, curr);
        }
    }

}
