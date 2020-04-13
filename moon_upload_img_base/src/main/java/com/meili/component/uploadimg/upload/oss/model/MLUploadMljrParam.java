package com.meili.component.uploadimg.upload.oss.model;


import com.meili.component.uploadimg.http.MLApiRequestParam;
import com.meili.moon.sdk.http.annotation.HttpRequest;

import java.io.File;

/**
 * 上传文件到影像件
 * Created by imuto
 */
@HttpRequest("mapper/fileupload/forApp")
public class MLUploadMljrParam extends MLApiRequestParam {
    public String publickey;
    public String owner;
    public String caller;
    public File file;
}
