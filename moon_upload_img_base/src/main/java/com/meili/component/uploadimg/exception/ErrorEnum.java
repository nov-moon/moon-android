package com.meili.component.uploadimg.exception;

/**
 * 上传图片系统的错误枚举
 * Created by imuto on 2018/4/3.
 */
public enum ErrorEnum {

    /** 参数校验错误 */
    PARAMS_CHECK(0),
    /** token初始化错误 */
    TOKEN(1),
    /** oss上传错误 */
    OSS(2),
    /** 图片压缩错误 */
    COMPRESS(3),
    /** 绑定影像件错误 */
    BIND_RELATION(4),
    /** 其他错误 */
    OTHER(5);

    private int value;

    private ErrorEnum(int v) {
        value = v;
    }

    public int getValue() {
        return value;
    }
}
