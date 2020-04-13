package com.meili.component.uploadimg.upload.mljr;

import com.meili.component.uploadimg.MLCallback;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.upload.MLAbsUploadServiceDelegate;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationResultModel;
import com.meili.component.uploadimg.upload.oss.model.MLChannelInfoModel;

/**
 * Created by imuto on 2018/6/28.
 */
public class MLMljrUploadServiceDelegate extends MLAbsUploadServiceDelegate<MLBindRelationResultModel> {
    @Override
    protected String getUploadTaskName() {
        return "ml/imgUpload/mljr";
    }

    @Override
    public void initClient(MLChannelInfoModel model, MLConfig config) {
        super.initClient(model, config);
    }

    @Override
    public boolean hasInitToken() {
        return true;
    }

    @Override
    protected void onItemUploadSuccess(MLBindRelationResultModel result, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        model.setUploadResult(result.getUuid(), result.getUrl());
        super.onItemUploadSuccess(result, model, callback);
    }
}
