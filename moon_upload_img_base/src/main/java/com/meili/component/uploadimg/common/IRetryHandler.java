package com.meili.component.uploadimg.common;

import java.io.Serializable;

/**
 * 重试处理器
 * Created by imuto on 17/12/21.
 */
public interface IRetryHandler extends Serializable {
    /** 是否可以重试 */
    boolean retry(Throwable throwable);

    /** 重置计数等 */
    void reset();
}
