package com.meili.component.uploadimg.upload.oss;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.meili.component.uploadimg.MLCallback;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.upload.MLAbsUploadServiceWithBindRelationDelegate;
import com.meili.component.uploadimg.upload.oss.model.MLChannelInfoModel;
import com.meili.component.uploadimg.upload.oss.model.MLGetChannelInfo;
import com.meili.component.uploadimg.upload.oss.model.MLOSSUploadInfo;
import com.meili.moon.sdk.CommonSdk;
import com.meili.moon.sdk.http.HttpSdk;

import java.util.HashMap;
import java.util.Map;

/**
 * 实现了上传委托类。以队列的形式管理上传请求
 * Created by imuto
 */
public class MLOSSUploadServiceDelegate extends MLAbsUploadServiceWithBindRelationDelegate<String> {

    /** 阿里oss的入口 */
    private static Map<MLConfig, OSS> mOssMap = new HashMap<>();

    private OSSConfig mConfig;

    @Override
    public void initClient(MLChannelInfoModel model, final MLConfig config) {
        super.initClient(model, config);

        if (mConfig == null) {
            mConfig = new OSSConfig();
        }
        mConfig.clear();

        mConfig.bucketName = model.getBucketName();
        mConfig.endpoint = model.getEndpoint();
        mConfig.objectKeyPrefix = config.getChannelId() + "/";
        //设置sts的管理对象，oss的sdk会对sts的生命周期做管理策略
        OSSCredentialProvider credentialProvider = new OSSFederationCredentialProvider() {
            @Override
            public OSSFederationToken getFederationToken() {
                final OSSFederationToken[] token = {null};
                try {
                    //从服务器获取sts信息
                    MLGetChannelInfo param = new MLGetChannelInfo();
                    param.caller = config.getChannelId();
                    MLChannelInfoModel result = HttpSdk.http().postSync(param, MLChannelInfoModel.class);

                    token[0] = new OSSFederationToken(result.getAccessKeyID()
                            , result.getAccessKeySecret()
                            , result.getToken(), result.getTokenExpireTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return token[0];
            }
        };

        mOssMap.put(config, new OSSClient(CommonSdk.app(), mConfig.endpoint, credentialProvider));
    }

    /*package*/ static OSS getOSS(MLConfig config) {
        return mOssMap.get(config);
    }

    private OSS oss() {
        return mOssMap.get(mConfig);
    }

    @Override
    protected void onTaskBundle(Bundle bundle) {
        super.onTaskBundle(bundle);
        bundle.putSerializable("ossConfig", mConfig);
    }

    @Override
    protected void onItemUploadSuccess(String result, MLUploadModel model, MLCallback.MLUploadCallback callback) {
        currUploadRequest.setRelationObjectKey(result, model);

        if (model instanceof MLOSSUploadInfo) {
            ((MLOSSUploadInfo) model).setObjectKey(result);
            ((MLOSSUploadInfo) model).setOssUrl(oss().presignPublicObjectURL(mConfig.bucketName, result));
        }

        super.onItemUploadSuccess(result, model, callback);
    }

    @Override
    public boolean hasInitToken() {
        return mConfig != null && !TextUtils.isEmpty(mConfig.endpoint);
    }

    @Override
    protected String getUploadTaskName() {
        return "ml/imgUpload/oss";
    }
}
