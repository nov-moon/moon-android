package com.meili.component.uploadimg.http;

import com.alibaba.fastjson.annotation.JSONField;
import com.meili.moon.sdk.http.IHttpResponse;
import com.meili.moon.sdk.http.IParamsBuilder;
import com.meili.moon.sdk.http.annotation.HttpIgnoreBuildParam;
import com.meili.moon.sdk.http.impl.SdkHttpRequestParams;

import org.jetbrains.annotations.NotNull;

/**
 * Created by imuto
 */
@HttpIgnoreBuildParam
public class MLApiRequestParam extends SdkHttpRequestParams {

    @JSONField(serialize = false)
    private IHttpResponse response;

    public MLApiRequestParam() {
        super("", null);
    }

    /**
     * @param uri 不可为空
     */
    public MLApiRequestParam(String uri) {
        super(uri, null);
    }


    @Override
    public IParamsBuilder<IHttpRequestParams> getDefaultParamBuilder() {
        return new MLApiParamBuilder();
    }

    @NotNull
    @Override
    public IHttpResponse getResponse() {
        if (response == null) {
            response = new MLApiHttpResponse();
        }
        response.setRequestParams(this);
        return response;
    }

    @Override
    public void setResponse(@NotNull IHttpResponse iHttpResponse) {
        response = iHttpResponse;
    }

}
