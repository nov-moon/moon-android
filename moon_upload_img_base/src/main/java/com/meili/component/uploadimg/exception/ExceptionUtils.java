package com.meili.component.uploadimg.exception;

/**
 * Created by imuto on 2018/4/3.
 */

public class ExceptionUtils {

    public static MLUploadImgException wrapException(ErrorEnum errorCode, String msg) {
        return wrapException(errorCode, msg, null);
    }

    public static MLUploadImgException wrapException(ErrorEnum errorCode, Throwable throwable) {
        return wrapException(errorCode, null, throwable);
    }

    public static MLUploadImgException wrapException(ErrorEnum errorCode, String msg, Throwable throwable) {
        if (throwable instanceof MLUploadImgException) {
            return (MLUploadImgException) throwable;
        } else if (throwable instanceof ConvertFileException) {
            return new MLUploadImgException(ErrorEnum.PARAMS_CHECK, throwable.getMessage(), throwable);
        } else {
            return new MLUploadImgException(errorCode, msg, throwable);
        }
    }
}
