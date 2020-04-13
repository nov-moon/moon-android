package com.meili.component.uploadimg.upload.oss;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.utils.BinaryUtil;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.upload.MLAbsUploadTask;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.msg.BaseMessage;

import java.io.File;

/**
 * Created by imuto on 2018/6/28.
 */
public class MLOSSUploadTask extends MLAbsUploadTask<String> {

    public MLOSSUploadTask(BaseMessage msg) {
        super(msg);
    }

    @Override
    protected String uploadItem(File uploadFile, MLUploadOption option, MLConfig mlConfig) throws Throwable {
        if (uploadFile == null || !uploadFile.exists()) {
            throw new BaseException("上传文件不存在: uploadFile = " + uploadFile);
        }

        OSSConfig config = (OSSConfig) getArguments().getSerializable("ossConfig");

        LogUtil.d("开始上传：" + uploadFile.getAbsolutePath());
        // 构造上传请求
        String absolutePath = uploadFile.getAbsolutePath();
        PutObjectRequest put = new PutObjectRequest(config.bucketName
                , getObjectKey(absolutePath, option, config.objectKeyPrefix), absolutePath);

        put.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
            @Override
            public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
                update(currentSize, totalSize);
            }
        });

        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentMD5(BinaryUtil.calculateBase64Md5(uploadFile.getAbsolutePath()));
            put.setMetadata(metadata);

            MLOSSUploadServiceDelegate.getOSS(mlConfig).putObject(put);
        } catch (ClientException e) {
            LogUtil.d("开始失败");
            e.printStackTrace();
            throw e;
        } catch (ServiceException e) {
            LogUtil.d("开始失败");
            LogUtil.d("ErrorCode: " + e.getErrorCode());
            LogUtil.d("RequestId: " + e.getRequestId());
            LogUtil.d("HostId: " + e.getHostId());
            LogUtil.d("RawMessage: " + e.getRawMessage());
            e.printStackTrace();
            throw e;
        } catch (Throwable e) {
            LogUtil.d("开始失败");
            e.printStackTrace();
            throw e;
        }

        return put.getObjectKey();
    }
}
