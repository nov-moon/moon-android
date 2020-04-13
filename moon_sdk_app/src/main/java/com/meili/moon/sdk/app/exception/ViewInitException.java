package com.meili.moon.sdk.app.exception;

import com.meili.moon.sdk.common.BaseException;

/**
 * Created by imuto on 16/11/17.
 */
public class ViewInitException extends BaseException {
    private static final long serialVersionUID = 1L;

    public ViewInitException(String detailMessage, Throwable throwable) {
        super(0, detailMessage, throwable);
    }

    public ViewInitException(String detailMessage) {
        super(0, detailMessage, null);
    }
}
