package com.meili.moon.sdk.http.annotation;

import com.meili.moon.sdk.http.IHttpResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 对网络数据对应的model的注解，自定义response
 * Created by imuto on 2018/5/10.
 */

@Target(ElementType.TYPE)
// 表示用在方法上
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpResponse {
    Class<IHttpResponse> value();
}
