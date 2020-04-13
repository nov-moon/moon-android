package com.meili.component.uploadimg.exception;


import com.meili.moon.sdk.common.BaseException;

/**
 * 文件转换错误
 * Created by imuto on 18/1/16.
 */
public class ConvertFileException extends BaseException {
    public ConvertFileException() {
    }

    public ConvertFileException(String detailMessage) {
        super(detailMessage);
    }

    public ConvertFileException(String message, Throwable cause) {
        super(0, message, cause);
    }

    public ConvertFileException(Throwable cause) {
        super(cause);
    }
}
