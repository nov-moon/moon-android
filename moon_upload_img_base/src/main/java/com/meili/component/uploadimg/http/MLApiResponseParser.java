package com.meili.component.uploadimg.http;


import com.alibaba.fastjson.JSONObject;
import com.meili.moon.sdk.http.IHttpResponse;
import com.meili.moon.sdk.http.impl.SdkHttpRespParser;


/**
 * Created by imuto on 15/8/17.
 * 默认的ResponseParser实现
 */
public class MLApiResponseParser extends SdkHttpRespParser {
    @Override
    public void parseCommonData(JSONObject jsonObject, IHttpResponse iHttpResponse) {
        iHttpResponse.setState(jsonObject.getInteger("errorCode"));
        iHttpResponse.setMessage(jsonObject.getString("errorMsg"));
        iHttpResponse.setData(jsonObject.getString("data"));
    }
}
