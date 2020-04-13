package com.meili.component.uploadimg;

import com.meili.component.uploadimg.exception.MLUploadImgException;

import java.util.List;

/**
 * 回调
 * Created by imuto on 17/11/13.
 */
public interface MLCallback {

    /**
     * 失败回调
     */
    interface MLUploadFailedCallback extends MLCallback {
        void onError(MLUploadImgException e, String msg);
    }

    /**
     * 成功的回调
     *
     * @param <T> 结果的泛型
     */
    interface MLUploadSuccessCallback<T> extends MLCallback {
        void onSuccess(T result);
    }

    /**
     * 进度回调,用于Item完成的回调，不用于具体某个Item的进度回调
     * <p>
     * 例如10张图，当前上传了2张
     *
     * @param <T> 结果的泛型
     */
    interface MLProgressCallback<T> extends MLCallback {
        void onProgress(T item, long total, long curr);
    }

    /**
     * Item状态更新回调
     * <p>
     * 其中的进度、成功、失败回调都是针对某一个item的回调
     * 例如进度回调的数据方式为：总大小1024，当前上传512
     *
     * @param <T> 结果的泛型
     */
    interface MLItemCallback<T> extends MLCallback {
        void onItemProgress(T item, long total, long curr);

        /** 当一个item上传成功后的回调 */
        void onItemUploadSuccess(T item);

        /** 当一个item上传失败后的回调 */
        void onItemUploadError(T item, MLUploadImgException e, String msg);
    }

    /**
     * 任务结束回调
     */
    interface MLFinishCallback extends MLCallback {
        /** isSuccess 任务是否是成功执行 */
        void onFinish(boolean isSuccess);
    }

    /**
     * 上传结果回调
     *
     * @param <T> 结果的泛型
     */
    interface MLUploadCallback<T> extends MLUploadFailedCallback, MLUploadSuccessCallback<T> {
    }

    /**
     * 各种callback的包装实现
     *
     * @param <ResultType> 结果的泛型，例如list
     */
    interface FullCallback<ResultType, ItemType> extends MLUploadCallback<ResultType>, MLFinishCallback, MLProgressCallback<ItemType>, MLItemCallback<ItemType> {
    }

    /**
     * 各种callback的包装实现
     *
     * @param <ResultType> 结果的泛型，例如list
     */
    abstract class SimpleBaseCallback<ResultType, ItemType> implements FullCallback<ResultType, ItemType> {
        @Override
        public void onProgress(ItemType item, long total, long curr) {
        }

        @Override
        public void onItemProgress(ItemType item, long total, long curr) {
        }

        @Override
        public void onItemUploadSuccess(ItemType item) {
        }

        @Override
        public void onItemUploadError(ItemType item, MLUploadImgException e, String msg) {
        }

        @Override
        public void onFinish(boolean isSuccess) {
        }
    }

    /**
     * 封装的List上传Callback
     */
    abstract class SimpleListCallback extends SimpleBaseCallback<List<? extends MLUploadModel>, MLUploadModel> {
    }

    /**
     * 封装的单个图片上传Callback
     */
    abstract class SimpleCallback extends SimpleBaseCallback<MLUploadModel, MLUploadModel> {
    }
}
