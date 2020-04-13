package com.meili.component.uploadimg.upload.oss;

import android.os.Bundle;
import android.text.TextUtils;

import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.exception.ErrorEnum;
import com.meili.component.uploadimg.exception.MLUploadImgException;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationModel;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationParam;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationResultModel;
import com.meili.moon.sdk.http.HttpSdk;
import com.meili.moon.sdk.msg.BaseMessage;
import com.meili.moon.sdk.msg.MessageTask;
import com.meili.moon.sdk.util.ArrayUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * 绑定影像件关系task
 * Created by imuto on 2018/4/2.
 */
public class MLBindRelationTask extends MessageTask<List<MLBindRelationModel>> {
    public MLBindRelationTask(BaseMessage msg) {
        super(msg);
    }

    @Override
    public void doBackground() throws Throwable {
        Bundle args = getArguments();
        final List<MLBindRelationModel> data = args.getParcelableArrayList("data");

        MLConfig config = (MLConfig) args.getSerializable("config");

        if (ArrayUtil.isEmpty(data)) {
            throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "上传数据不能为空");
        }
        String channelId = config.getChannelId();

        JSONArray jsonArray = new JSONArray();

        for (MLBindRelationModel item : data) {
            JSONObject jsonItem = new JSONObject();
            jsonItem.put("fileName", getFileName(item.objectKey, channelId));
            jsonItem.put("fileSize", item.fileSize);
            jsonItem.put("path", item.objectKey);
            jsonArray.put(jsonItem);
        }

        MLBindRelationParam param = new MLBindRelationParam();
        param.caller = channelId;
        param.fileInfoList = jsonArray.toString();

        List<MLBindRelationResultModel> result = HttpSdk.http().postSyncForList(param, MLBindRelationResultModel.class);
        if (ArrayUtil.isEmpty(result) || !ArrayUtil.sameSize(result, data.size())) {
            throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "请求返回数据错误");
        }
        for (int i = 0; i < result.size(); i++) {
            if (!TextUtils.equals(result.get(i).getPath(), data.get(i).objectKey)) {
                throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "返回数据对应关系错误");
            }
            data.get(i).resultModel = result.get(i);
        }

//
//        HttpSdk.http().postSync(param, new SimpleCallback<List<MLBindRelationResultModel>>() {
//            @Override
//            public void onSuccess(List<MLBindRelationResultModel> result) {
//                super.onSuccess(result);
//                if (ArrayUtil.isEmpty(result) || result.size() != data.size()) {
//                    throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "请求返回数据错误");
//                }
//
//                for (int i = 0; i < result.size(); i++) {
//                    if (!TextUtils.equals(result.get(i).getPath(), data.get(i).objectKey)) {
//                        throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "返回数据对应关系错误");
//                    }
//                    data.get(i).resultModel = result.get(i);
//                }
//            }
//
//            @Override
//            public void onError(@NotNull BaseException exception) {
//                super.onError(exception);
//                throw new MLUploadImgException(ErrorEnum.BIND_RELATION, exception);
//            }
//        });

        setResult(data);
    }

    private String getFileName(String objectKey, String channelId) {
        if (!objectKey.startsWith(channelId)) {
            throw new MLUploadImgException(ErrorEnum.BIND_RELATION, "objectKey命名有误");
        }

        objectKey = objectKey.substring(objectKey.indexOf(channelId) + channelId.length());
        if (objectKey.startsWith("/")) {
            objectKey = objectKey.substring(1);
        }
        return objectKey;

    }
}
