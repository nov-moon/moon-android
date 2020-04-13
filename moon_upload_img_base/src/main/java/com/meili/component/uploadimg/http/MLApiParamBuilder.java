package com.meili.component.uploadimg.http;

import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadFile;
import com.meili.moon.sdk.http.IRequestParams;
import com.meili.moon.sdk.http.impl.SdkHttpParamsBuilder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by imuto on 16/5/24.
 */
public class MLApiParamBuilder extends SdkHttpParamsBuilder {

    public MLApiParamBuilder() {
    }

    @Override
    public SSLSocketFactory getSSLSocketFactory() {
        return (SSLSocketFactory) SSLSocketFactory.getDefault();
    }

    @Override
    public String getDefaultHost() {
        MLConfig.MLConfigurable config = MLUploadFile.getConfig(MLUploadFile.TargetFlags.DEF_IMG);
        return config.getHost();
    }

    @Override
    public void buildParams(@NotNull IRequestParams.IHttpRequestParams params) {

    }

    @Override
    public void buildSign(@NotNull IRequestParams.IHttpRequestParams iHttpRequestParams) {

    }

    @Nullable
    @Override
    public String getUserAgent() {
        return null;
    }
}
