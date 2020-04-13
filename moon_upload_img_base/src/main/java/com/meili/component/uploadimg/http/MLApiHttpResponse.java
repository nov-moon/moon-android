package com.meili.component.uploadimg.http;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.meili.moon.sdk.http.IRequestParams;
import com.meili.moon.sdk.http.IResponseParser;
import com.meili.moon.sdk.http.exception.HttpException;
import com.meili.moon.sdk.http.impl.SdkHttpResponse;

import org.jetbrains.annotations.NotNull;


public class MLApiHttpResponse extends SdkHttpResponse {

    private IResponseParser parser = new MLApiResponseParser();
    private IRequestParams.IHttpRequestParams params;

    @Override
    public boolean handleParseData(String response) {
        if (TextUtils.isEmpty(response)) {
            setState(-1);
            setMessage("未知错误");
            return true;
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        setState(jsonObject.getInteger("errorCode"));
        setMessage(jsonObject.getString("errorMsg"));
        if (getState() != 0) {
            throw new HttpException(getState(), getMessage(), null);
        }
        return super.handleParseData(response);
    }


    @NotNull
    @Override
    public IResponseParser getParser() {
        return parser;
    }

    @Override
    public void setParser(@NotNull IResponseParser iResponseParser) {
        parser = iResponseParser;
    }

    @NotNull
    @Override
    public IRequestParams.IHttpRequestParams getRequestParams() {
        return params;
    }

    @Override
    public void setRequestParams(@NotNull IRequestParams.IHttpRequestParams iHttpRequestParams) {
        params = iHttpRequestParams;
    }
}
