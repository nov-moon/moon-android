package com.meili.component.uploadimg.upload;

import com.meili.component.uploadimg.MLCallback;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 一个完整的上传请求封装
 * Created by imuto on 17/11/13.
 */
public class UploadRequest {
    private List<? extends MLUploadModel> data;
    private ArrayList<MLBindRelationModel> dataRelations;
    private MLCallback.MLUploadCallback callback;
    private MLUploadOption option;
    private boolean isAllUploadSuccess = true;
    // 当前request中是否有上传成功的内容
    private boolean hasUploadSuccess = false;
    private AtomicInteger uploadCount = new AtomicInteger(0);
    /** 是否已经完成绑定 */
    private boolean hasFinishBind = false;
    /** 当前请求是否是list请求 */
    private boolean isList = false;

    UploadRequest(MLUploadModel model) {
        List<MLUploadModel> list = new ArrayList<>();
        list.add(model);
        data = list;
        initData();
        initDataRelations();
    }

    UploadRequest(List<? extends MLUploadModel> list) {
        isList = true;
        data = list;
        initData();
        initDataRelations();
    }


    public List<? extends MLUploadModel> getData() {
        return data;
    }

    public MLCallback.MLUploadCallback getCallback() {
        return callback;
    }

    public void setCallback(MLCallback.MLUploadCallback callback) {
        this.callback = callback;
    }

    public MLUploadOption getOption() {
        return option;
    }

    public void setOption(MLUploadOption option) {
        this.option = option;
    }

    public boolean isAllUploadSuccess() {
        return isAllUploadSuccess;
    }

    public void setAllUploadSuccess(boolean allUploadSuccess) {
        isAllUploadSuccess = allUploadSuccess;
    }

    public int getTotal() {
        return data.size();
    }

    public boolean isList() {
        return isList;
    }


    public int getUploadCount() {
        return uploadCount.get();
    }

    public void increaseUploadCount() {
        uploadCount.incrementAndGet();
    }

    /** 是否本请求的上传任务已经完成 */
    public boolean hasFinish() {
        return uploadCount.get() >= data.size();
    }


    public void setRelationObjectKey(String objectKey, MLUploadModel model) {
        int i = data.indexOf(model);

        MLBindRelationModel item = dataRelations.get(i);
        item.objectKey = objectKey;
        item.fileSize = model.getCompressSize();
        if (item.fileSize <= 0) {
            File uploadFile = new File(model.getUploadFilePath());
            item.fileSize = uploadFile.length();
        }
    }

    public ArrayList<MLBindRelationModel> getDataRelations() {
        return dataRelations;
    }

    /** 是否本请求的绑定任务已经完成 */
    public boolean hasFinishBind() {
        return hasFinishBind;
    }

    public void setHasFinishBind(boolean hasFinishBind) {
        this.hasFinishBind = hasFinishBind;
    }

    /** 将data数据的result清空 */
    private void initData() {
        for (MLUploadModel item : data) {
            if (item == null) {
                continue;
            }
            item.setUploadResult(null, null);
        }
    }

    private void initDataRelations() {
        dataRelations = new ArrayList<>();
        for (int i = 0; i < data.size(); i++) {
            MLBindRelationModel model = new MLBindRelationModel();
            model.index = i;
            dataRelations.add(model);
        }

    }

    public boolean isHasUploadSuccess() {
        return hasUploadSuccess;
    }

    public void setHasUploadSuccess(boolean hasUploadSuccess) {
        this.hasUploadSuccess = hasUploadSuccess;
    }
}
