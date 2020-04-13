package com.meili.component.uploadimg;

import com.meili.component.uploadimg.common.IRetryHandler;
import com.meili.component.uploadimg.util.MLBuildType;
import com.mljr.moon.imgcompress.compressrule.ICompressRule;

import java.io.Serializable;
import java.util.List;

/**
 * sdk的全局配置标准接口
 * Created by imuto on 17/11/13.
 */
public interface MLConfig extends Serializable {

    /**
     * 获取主机域名地址
     */
    String getHost();

    /**
     * 获取当前的渠道号
     */
    String getChannelId();

    /**
     * 获取当前的渠道的publicKey，此publicKey需要业务客户端找后端同学要
     */
    String getChannelPublicKey();

    /**
     * 获取当前的渠道的Owner，此Owner需要业务客户端找后端同学要。从影像件同学处了解，此字段一般等同于publicKey
     */
    String getChannelOwner();

    /**
     * 获取上传服务
     */
    MLUploadService getUploadService();

    /**
     * 是否开启日志
     */
    boolean isLog();

    /**
     * 获取是否开启debug
     */
    boolean isDebug();

    /**
     * 获取全局的构建方式
     */
    MLBuildType getBuildType();

    /**
     * 获取全局的压缩配置,如果此配置为null，则认为默认不开启压缩
     */
    ICompressRule getCompressOpts();

    /**
     * 获取压缩后的图片是否要在上传完成后删除
     */
    boolean isAutoDelCompressCache();

    /**
     * 获取重试机制的处理器
     */
    IRetryHandler getUploadRetryHandler();

    /**
     * 获取支持的文件转换器，用来进行文件格式转换
     */
    List<? extends MLUploadFileConverter> getUploadFileConverter();

    /**
     * 是否是图片上传， 如果不是图片上传，则格式检测、图片压缩等步骤不会执行
     */
    boolean isImageUpload();

    interface MLConfigurable extends MLConfig {
        void configOptions(MLConfigOptions options);

        MLConfigOptions getConfigOptions();
    }
}
