package com.meili.component.uploadimg.common;

import com.meili.component.uploadimg.upload.mljr.MLMljrUploadTask;
import com.meili.component.uploadimg.upload.oss.MLBindRelationTask;
import com.meili.component.uploadimg.upload.oss.MLOSSUploadTask;
import com.meili.moon.sdk.msg.MessageRegistry;

/**
 * Created by imuto on 17/12/21.
 */

public class MLImgUploadTaskDefine {

    private static MLImgUploadTaskDefine instance;

    private MLImgUploadTaskDefine() {
        onLoad();
    }

    public static synchronized MLImgUploadTaskDefine getInstance() {
        if (instance == null) {
            instance = new MLImgUploadTaskDefine();
        }
        return instance;
    }

    public void onLoad() {
        MessageRegistry.INSTANCE.register("ml/imgUpload/oss", MLOSSUploadTask.class);
        MessageRegistry.INSTANCE.register("ml/imgUpload/mljr", MLMljrUploadTask.class);
        MessageRegistry.INSTANCE.register("ml/imgUpload/bindRelation", MLBindRelationTask.class);
    }
}
