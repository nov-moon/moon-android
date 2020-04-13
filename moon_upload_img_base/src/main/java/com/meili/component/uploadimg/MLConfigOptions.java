package com.meili.component.uploadimg;

import com.meili.component.uploadimg.common.IRetryHandler;
import com.meili.component.uploadimg.common.MLDefRetryHandler;
import com.meili.component.uploadimg.converter.BMPConverter;
import com.meili.component.uploadimg.converter.SupportTypeConverter;
import com.meili.component.uploadimg.converter.WebPConverter;
import com.meili.component.uploadimg.exception.ConvertFileException;
import com.meili.component.uploadimg.upload.MLUploadService;
import com.meili.component.uploadimg.util.MLAbsHostConfig;
import com.meili.component.uploadimg.util.MLBuildType;
import com.meili.component.uploadimg.util.MLDefHostConfig;
import com.mljr.moon.imgcompress.compressrule.ICompressRule;
import com.mljr.moon.imgcompress.compressrule.MNCompressOption;

import java.util.ArrayList;
import java.util.List;


/**
 * 配置SDK
 * Created by imuto on 17/10/30.
 */
public class MLConfigOptions implements MLConfig {
    /**
     * 配置编译类型
     */
    private MLBuildType buildType;
    /**
     * 配置自定义host
     */
    private MLAbsHostConfig hostConfig;

    /**
     * 是否打印log
     */
    private boolean isLog = false;

    private com.meili.component.uploadimg.MLUploadService mUploadService;

    private String channelId;
    private String channelPublicKey;
    private String channelOwner;

    /**
     * 压缩规则
     */
    private ICompressRule mCompressOptions;

    /**
     * 是否删除上传后的压缩图片，默认删除
     */
    private boolean isAutoDelCompressCache = true;

    private IRetryHandler mUploadRetryHandler;

    private List<? extends MLUploadFileConverter> uploadFileConverters;

    private boolean isImageUpload = false;

    private MLConfigOptions() {
        mUploadService = new MLUploadService(this);
    }

    @Override
    public String getHost() {
        return isDebug() ? hostConfig.getHostForDebug(0) : hostConfig.getHost();
    }

    @Override
    public boolean isDebug() {
        return buildType == MLBuildType.DEBUG;
    }

    @Override
    public String getChannelId() {
        return channelId;
    }

    @Override
    public String getChannelPublicKey() {
        return channelPublicKey;
    }

    @Override
    public String getChannelOwner() {
        return channelOwner;
    }

    @Override
    public MLBuildType getBuildType() {
        return buildType;
    }

    @Override
    public ICompressRule getCompressOpts() {
        return mCompressOptions;
    }

    @Override
    public boolean isAutoDelCompressCache() {
        return isAutoDelCompressCache;
    }

    @Override
    public IRetryHandler getUploadRetryHandler() {
        return mUploadRetryHandler;
    }

    @Override
    public List<? extends MLUploadFileConverter> getUploadFileConverter() {
        return uploadFileConverters;
    }

    @Override
    public boolean isImageUpload() {
        return isImageUpload;
    }

    public void setImageUpload(boolean isImageUpload) {
        this.isImageUpload = isImageUpload;
    }

    @Override
    public com.meili.component.uploadimg.MLUploadService getUploadService() {
        return mUploadService;
    }

    @Override
    public boolean isLog() {
        return isLog;
    }

    void setBuildType(MLBuildType buildType) {
        this.buildType = buildType;
    }


    void setHostConfig(MLAbsHostConfig hostConfig) {
        this.hostConfig = hostConfig;
    }


    void setLog(boolean log) {
        isLog = log;
    }

    void setChannel(String channelId) {
        this.channelId = channelId;
    }

    public void setChannelPublicKey(String channelPublicKey) {
        this.channelPublicKey = channelPublicKey;
    }

    public void setChannelOwner(String channelOwner) {
        this.channelOwner = channelOwner;
    }

    public static class Build {

        /**
         * 默认的转换器列表、支持四种文件格式：PNG->PNG, JPG->JPG, WEBP->JPG, BMP->JPG
         */
        public static List<? extends MLUploadFileConverter> DEF_FILE_CONVERTER_LIST;

        static {
            ArrayList<MLUploadFileConverter> defList = new ArrayList<>(3);
            defList.add(new SupportTypeConverter());
            defList.add(new WebPConverter());
            defList.add(new BMPConverter());
            DEF_FILE_CONVERTER_LIST = defList;
        }

        private MLConfigOptions options;
        private MLBuildType mlBuildType;
        private boolean isLog;
        private MLAbsHostConfig hostConfig;
        private int uploadMaxRetryTimes = 2;
        private IRetryHandler uploadRetry;

        public Build() {
            options = new MLConfigOptions();
            options.mCompressOptions = MNCompressOption.PHOTO_HD;
            options.mUploadRetryHandler = new MLDefRetryHandler();
            mlBuildType = MLBuildType.RELEASE;
            isLog = false;
            hostConfig = new MLDefHostConfig();
            options.uploadFileConverters = DEF_FILE_CONVERTER_LIST;
        }

        /**
         * 设置当前构建类型：参见{@link MLBuildType#DEBUG}，{@link MLBuildType#RELEASE}
         */
        public Build setBuildType(MLBuildType buildType) {
            mlBuildType = buildType;
            if (!isLog) {
                isLog = mlBuildType == MLBuildType.DEBUG;
            }
            return this;
        }

        /**
         * 设置服务器配置，默认已经配置开发服务器和线上服务器地址，一般情况不用设置
         */
        public Build setHostConfig(MLAbsHostConfig hostConfig) {
//            options.setHostConfig(hostConfig);
            this.hostConfig = hostConfig;
            return this;
        }

        /**
         * 设置是否打印日志，默认如果构建类型为debug则打印日志，否则不打印日志
         */
        public Build setLog(boolean log) {
            isLog = log;
            return this;
        }

        /**
         * 设置是否打印日志，默认如果构建类型为debug则打印日志，否则不打印日志
         */
        public Build setChannel(String channel) {
            options.setChannel(channel);
            return this;
        }

        /**
         * 设置全局压缩选项，如果为null，则不压缩，默认使用{@link MNCompressOption#PHOTO_HD}进行压缩
         */
        public Build setCompressOpts(ICompressRule opts) {
            options.mCompressOptions = opts;
            return this;
        }

        /**
         * 设置是否在上传成功后自动删除压缩缓存，默认为删除
         */
        public Build setAutoDelCompressCache(Boolean isDel) {
            options.isAutoDelCompressCache = isDel;
            return this;
        }

        /**
         * 设置全局重试机制
         */
        public Build setUploadRetryHandler(IRetryHandler retryHandler) {
            uploadRetry = retryHandler;
            return this;
        }

        /**
         * 如果times<0,则设置无效，times=0，则不做重试,如果已经设置了UploadRetryHandler，则此配置不生效
         */
        public Build setUploadRetryMaxTimes(int times) {
            uploadMaxRetryTimes = times;
            return this;
        }

        /**
         * 设置上传文件格式转换器列表，这里配置的转换器，必须包含你要支持的所有类型的文件转换器，没有配置的格式，
         * 将不能上传。默认使用{@link #DEF_FILE_CONVERTER_LIST}作为转换器列表
         */
        public Build setUploadFileConverters(List<? extends MLUploadFileConverter> converters) {
            if (converters == null) {
                throw new ConvertFileException("Converters 列表不能为空");
            }
            options.uploadFileConverters = converters;
            return this;
        }

        /**
         * 设置是否为上传图片，默认为上传图片
         */
        public Build setIsUploadImage(boolean isUploadImage) {
            options.isImageUpload = isUploadImage;
            return this;
        }

        public MLConfigOptions build() {
            options.setBuildType(mlBuildType);
            options.setLog(isLog);
            options.setHostConfig(hostConfig);
            if (uploadRetry == null) {
                ((MLDefRetryHandler) options.mUploadRetryHandler).setMaxCount(uploadMaxRetryTimes);
            } else {
                options.mUploadRetryHandler = uploadRetry;
            }
            return options;
        }

    }
}
