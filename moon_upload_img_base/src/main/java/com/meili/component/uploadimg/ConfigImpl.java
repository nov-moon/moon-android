package com.meili.component.uploadimg;

import com.meili.component.uploadimg.common.IRetryHandler;
import com.meili.component.uploadimg.util.MLBuildType;
import com.meili.moon.sdk.log.LogUtil;
import com.mljr.moon.imgcompress.compressrule.ICompressRule;

import java.util.List;

/**
 * 配置的默认实现类
 * Created by imuto on 17/11/13.
 */
/*package*/class ConfigImpl implements MLConfig.MLConfigurable {

    private MLConfigOptions options;

    /*package*/ ConfigImpl() {
        configOptions(new MLConfigOptions.Build().build());
    }

    @Override
    public String getHost() {
        return options.getHost();
    }

    @Override
    public boolean isDebug() {
        return options.isDebug();
    }

    @Override
    public String getChannelId() {
        return options.getChannelId();
    }

    @Override
    public String getChannelPublicKey() {
        return options.getChannelPublicKey();
    }

    @Override
    public String getChannelOwner() {
        return options.getChannelOwner();
    }

    @Override
    public MLBuildType getBuildType() {
        return options.getBuildType();
    }

    @Override
    public ICompressRule getCompressOpts() {
        return options.getCompressOpts();
    }

    @Override
    public boolean isAutoDelCompressCache() {
        return options.isAutoDelCompressCache();
    }

    @Override
    public IRetryHandler getUploadRetryHandler() {
        return options.getUploadRetryHandler();
    }

    @Override
    public List<? extends MLUploadFileConverter> getUploadFileConverter() {
        return options.getUploadFileConverter();
    }

    @Override
    public boolean isImageUpload() {
        return options.isImageUpload();
    }

    @Override
    public void configOptions(MLConfigOptions options) {
        this.options = options;
        LogUtil.isDebug(options.isLog());
    }

    @Override
    public MLConfigOptions getConfigOptions() {
        return options;
    }

    @Override
    public MLUploadService getUploadService() {
        return options.getUploadService();
    }

    @Override
    public boolean isLog() {
        return options.isLog();
    }
}
