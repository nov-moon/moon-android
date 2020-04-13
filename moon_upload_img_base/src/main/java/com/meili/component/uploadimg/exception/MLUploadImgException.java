package com.meili.component.uploadimg.exception;


import com.meili.moon.sdk.common.BaseException;

/**
 * 图片上传的错误对象
 * Created by imuto on 2018/4/3.
 */
public class MLUploadImgException extends BaseException {
    private ErrorEnum errorCode;

    public MLUploadImgException(ErrorEnum errorCode, String msg, Throwable cause) {
        super(0, msg, cause);
        this.errorCode = errorCode;
    }

    public MLUploadImgException(ErrorEnum errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public MLUploadImgException(ErrorEnum errorCode, String msg) {
        super(msg);
        this.errorCode = errorCode;
    }

    public MLUploadImgException(ErrorEnum errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }

    public ErrorEnum getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return super.toString() + " errorCode=" + errorCode.getValue();
    }
}
