package com.meili.component.uploadimg.common;

import com.alibaba.fastjson.JSONException;
import com.meili.moon.sdk.exception.CancelledException;
import com.meili.moon.sdk.http.exception.HttpException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.PortUnreachableException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * 默认的重试机制
 * Created by imuto on 17/12/21.
 */
public class MLDefRetryHandler implements IRetryHandler {

    private int MAX_COUNT = 2;
    private int currTimes = 0;

    @SuppressWarnings("unchecked")
    private final static List<Class<? extends Exception>> BLACK_LIST = (List<Class<? extends Exception>>) Arrays.asList(
            HttpException.class,
            CancelledException.class,
            MalformedURLException.class,
            URISyntaxException.class,
            NoRouteToHostException.class,
            PortUnreachableException.class,
            ProtocolException.class,
            NullPointerException.class,
            FileNotFoundException.class,
            JSONException.class,
            SocketTimeoutException.class,
            UnknownHostException.class,
            IllegalArgumentException.class
    );

    @Override
    public boolean retry(Throwable throwable) {
        boolean result = !BLACK_LIST.contains(throwable.getClass()) && currTimes < MAX_COUNT;
        if (result) {
            currTimes++;
        }
        return result;
    }

    @Override
    public void reset() {
        currTimes = 0;
    }

    /** 设置最大重试次数 */
    public void setMaxCount(int maxCount) {
        MAX_COUNT = maxCount;
    }
}
