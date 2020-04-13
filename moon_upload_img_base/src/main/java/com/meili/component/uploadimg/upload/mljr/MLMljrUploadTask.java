package com.meili.component.uploadimg.upload.mljr;

import android.text.TextUtils;

import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.upload.MLAbsUploadTask;
import com.meili.component.uploadimg.upload.oss.model.MLBindRelationResultModel;
import com.meili.component.uploadimg.upload.oss.model.MLUploadMljrParam;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.http.HttpSdk;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.msg.BaseMessage;

import java.io.File;

/**
 * Created by imuto on 2018/6/28.
 */
public class MLMljrUploadTask extends MLAbsUploadTask<MLBindRelationResultModel> {

    public MLMljrUploadTask(BaseMessage msg) {
        super(msg);
    }

    @Override
    protected MLBindRelationResultModel uploadItem(File uploadFile, MLUploadOption option, MLConfig mlConfig) {

        MLConfig config = (MLConfig) getArguments().getSerializable("config");

        if (TextUtils.isEmpty(config.getChannelOwner()) || TextUtils.isEmpty(config.getChannelPublicKey())) {
            throw new BaseException("未设置publicKey或owner，无法使用影像件上传服务");
        }

        LogUtil.d("开始上传：" + uploadFile.getAbsolutePath());
        // 构造上传请求
        try {
            MLUploadMljrParam param = new MLUploadMljrParam();
            param.caller = config.getChannelId();
            param.publickey = config.getChannelPublicKey();
            param.owner = config.getChannelOwner();
            param.file = uploadFile;

            return HttpSdk.http().postSync(param, MLBindRelationResultModel.class);
        } catch (Throwable e) {
            LogUtil.d("开始失败");
            e.printStackTrace();
            throw e;
        }
    }
}
