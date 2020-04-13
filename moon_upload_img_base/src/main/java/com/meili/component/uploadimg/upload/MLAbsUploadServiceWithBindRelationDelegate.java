package com.meili.component.uploadimg.upload;

import android.os.Bundle;
import android.text.TextUtils;

import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationModel;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationResultModel;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.msg.BaseMessage;
import com.meili.moon.sdk.msg.MessageCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * 实现了上传委托类。以队列的形式管理上传请求
 * Created by imuto on 17/11/13.
 */
public abstract class MLAbsUploadServiceWithBindRelationDelegate<ItemResultType>
        extends MLAbsUploadServiceDelegate<ItemResultType> {

    @Override
    protected void onCurrRequestFinish() {
        if (currUploadRequest.hasFinishBind()) {
            uploadQueue.remove(currUploadRequest);
            currUploadRequest = null;
            uploadCircle();
        } else {
            bindRelations();
        }
    }

    /** 调用服务器接口，绑定oss数据到影像件 */
    private void bindRelations() {

        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList("data", currUploadRequest.getDataRelations());
        bundle.putSerializable("config", mConfig);

        BaseMessage message = new BaseMessage("ml/imgUpload/bindRelation");
        message.setArguments(bundle);

        message.setCallback(new MessageCallback() {
            @Override
            public void onSuccess(Object o) {
                // 检查数据，查看是否合法，并且设置结果信息到result上
                ArrayList<MLBindRelationModel> dataRelations = currUploadRequest.getDataRelations();
                for (int i = 0; i < currUploadRequest.getData().size(); i++) {
                    MLUploadModel item = currUploadRequest.getData().get(i);
                    MLBindRelationResultModel resultModel = dataRelations.get(i).resultModel;
                    if (resultModel == null || !resultModel.getSuccess() || TextUtils.isEmpty(resultModel.getUuid())) {
                        continue;
                    }
                    item.setUploadResult(resultModel.getUuid(), resultModel.getUrl());
                }

                onUploadSuccess();
            }

            @Override
            public void onError(@NotNull BaseException exception) {
                super.onError(exception);
                onUploadError(exception);
            }

            @Override
            public void onFinished(boolean isSuccess) {
                super.onFinished(isSuccess);
                onUploadFinish();
                uploadCircle();
            }

        });
        currTask = message.send();
    }
}
