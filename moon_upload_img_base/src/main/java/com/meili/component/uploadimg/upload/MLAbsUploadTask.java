package com.meili.component.uploadimg.upload;

import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.ServiceException;
import com.meili.component.uploadimg.MLCompressInfoModel;
import com.meili.component.uploadimg.MLConfig;
import com.meili.component.uploadimg.MLUploadModel;
import com.meili.component.uploadimg.MLUploadOption;
import com.meili.component.uploadimg.common.IRetryHandler;
import com.meili.component.uploadimg.common.MLDefRetryHandler;
import com.meili.component.uploadimg.converter.ConverterManager;
import com.meili.component.uploadimg.exception.ErrorEnum;
import com.meili.component.uploadimg.exception.MLUploadImgException;
import com.meili.moon.sdk.common.BaseException;
import com.meili.moon.sdk.log.LogUtil;
import com.meili.moon.sdk.msg.BaseMessage;
import com.meili.moon.sdk.msg.MessageCallback;
import com.meili.moon.sdk.msg.MessageTask;
import com.meili.moon.sdk.util.encrypt.MD5;
import com.mljr.moon.imgcompress.MNSyncCompressImage;
import com.mljr.moon.imgcompress.compressrule.ICompressRule;

import java.io.File;
import java.io.IOException;

/**
 * 压缩，并上传的任务
 * Created by imuto on 17/12/21.
 */
public abstract class MLAbsUploadTask<ResultType> extends MessageTask<ResultType> {

    /** 平台标示 */
    private static final int PLATFORM_FLAG = 0;
    /** 后缀分隔符 */
    private static final String FILE_SUFFIX_SEPARATOR = ".";
    /** ok名字错误的flag */
    private static final String OBJECT_KEY_ERROR_FLAG = "_";

    public MLAbsUploadTask(BaseMessage msg) {
        super(msg);
    }

    @Override
    public void doBackground() throws Throwable {
        Bundle args = getArguments();

        MLUploadModel data = (MLUploadModel) args.getSerializable("data");
        MLConfig config = (MLConfig) args.getSerializable("config");
        MLUploadOption options = args.getParcelable("options");

        if (data == null || TextUtils.isEmpty(data.getUploadFilePath())) {
            throw new BaseException(0, "上传内容错误：data = " + data);
        }

        File uploadFile = new File(data.getUploadFilePath());
        if (!uploadFile.exists() || uploadFile.length() <= 0) {
            throw new MLUploadImgException(ErrorEnum.PARAMS_CHECK, "上传文件不存在或大小为0，文件地址：" + uploadFile.getAbsolutePath());
        }

        File uploadFileConvert = null;
        if (config.isImageUpload()) {
            //转化文件到支持类型文件
            uploadFileConvert = ConverterManager.convert(uploadFile, config);
            uploadFile = uploadFileConvert;

            //压缩文件
            uploadFile = compress(data, uploadFile, options, config);
        } else {
            LogUtil.e("其他文件类型，不进行压缩");
        }

        //进行重试初始化
        ResultType result = null;
        IRetryHandler retryHandler = config.getUploadRetryHandler();
        if (options != null) {
            if (options.uploadRetryHandler != null) {
                retryHandler = options.uploadRetryHandler;
            } else if (options.maxRetryTimes >= 0) {
                retryHandler = new MLDefRetryHandler();
                ((MLDefRetryHandler) retryHandler).setMaxCount(options.maxRetryTimes);
            }
        }

        boolean retry = true;
        Throwable throwable = null;

        //开始上传
        while (retry) {
            try {
                result = internalUploadItem(uploadFile, options, config);
                break;
            } catch (ClientException e) {
                e.printStackTrace();
                throwable = e;
                retry = retryHandler.retry(e.getCause());
            } catch (ServiceException e) {
                e.printStackTrace();
                throwable = e;
                retry = false;
            } catch (Throwable e) {
                e.printStackTrace();
                throwable = e;
                retry = retryHandler.retry(e.getCause());
            }
        }
        if (retryHandler != null) {
            retryHandler.reset();
        }

        //尝试删除缓存
        tryDelCompressCache(data, uploadFile, options, config);
        tryDelConvertCache(data, uploadFileConvert, options);

        if (throwable != null) {
            throw throwable;
        }
        setResult(result);
    }

    /** 压缩model */
    private File compress(MLUploadModel data, File uploadFile, MLUploadOption options, MLConfig config) throws IOException {
        LogUtil.i("压缩：开始");

        //判断文件是否存在
        if (!uploadFile.exists()) {
            throw new BaseException(0, "上传内容错误：上传内容不存在（" + uploadFile.getAbsolutePath() + "）");
        }

        ICompressRule compressOptions = config.getCompressOpts();

        if (options != null) {
            if (!options.isCompress) {
                compressOptions = null;
            } else if (options.compressOptions != null) {
                compressOptions = options.compressOptions;
            }
        }

        //记录MLUploadModel接口上的size
        data.setCompressSize(uploadFile.length());

        //转化model
        MLCompressInfoModel compressInfoModel = null;
        if (data instanceof MLCompressInfoModel) {
            compressInfoModel = (MLCompressInfoModel) data;
        }

        //记录原始文件信息
        String originHashCode = null;
        if (compressInfoModel != null) {
            compressInfoModel.setOriginSize(uploadFile.length());
            originHashCode = MD5.getFileMD5String(uploadFile);
            compressInfoModel.setOriginHashCode(originHashCode);
        }

        //压缩文件
        if (compressOptions != null) {
            LogUtil.i("压缩前：" + uploadFile.length() + "  path：" + uploadFile.getAbsolutePath());
            uploadFile = MNSyncCompressImage.compress(uploadFile, compressOptions);
            LogUtil.i("压缩后：" + uploadFile.length() + "  path：" + uploadFile.getAbsolutePath());
        } else {
            LogUtil.i("压缩：未压缩");
        }

        //记录压缩后文件信息
        if (compressInfoModel != null) {
            compressInfoModel.setCompressSize(uploadFile.length());
            String compressHashCode = MD5.getFileMD5String(uploadFile);
            compressInfoModel.setCompressHashCode(compressHashCode);
            if (originHashCode.equals(compressHashCode)) {
                compressInfoModel.setIsCompress(0);
            } else {
                compressInfoModel.setIsCompress(1);
            }
            if (compressInfoModel instanceof MLCompressInfoModel.MLCompressInfoWithFileModel) {
                ((MLCompressInfoModel.MLCompressInfoWithFileModel) compressInfoModel).setCompressResultFile(uploadFile);
            }
        }

        LogUtil.i("压缩：结束");
        return uploadFile;
    }

    /** 上传一个指定model */
    private ResultType internalUploadItem(File uploadFile, MLUploadOption option, MLConfig mlConfig) throws Throwable {
        if (uploadFile == null || !uploadFile.exists()) {
            throw new BaseException("上传文件不存在: uploadFile = " + uploadFile);
        }
        LogUtil.d("开始上传：" + uploadFile.getAbsolutePath());

        return uploadItem(uploadFile, option, mlConfig);
    }

    protected abstract ResultType uploadItem(File uploadFile, MLUploadOption option, MLConfig mlConfig) throws Throwable;

    /** 尝试删除压缩缓存 */
    private void tryDelCompressCache(MLUploadModel data, File uploadFile, MLUploadOption options, MLConfig config) {
        //如果没有上传文件，或者上传文件的路径和原始文件路径相同，则不删除文件，直接返回
        if (uploadFile == null || !uploadFile.exists() || data.getUploadFilePath().equals(uploadFile.getAbsolutePath())) {
            return;
        }

        try {
            //option是否指定自动删除
            boolean isAutoDelByOption = options != null && options.isAutoDelCompressCache;
            //全局配置是否指定自动删除
            boolean isAutoDelByConfig = options == null && config.isAutoDelCompressCache();
            if (isAutoDelByOption || isAutoDelByConfig) {
                LogUtil.d("删除压缩文件：" + uploadFile.getAbsolutePath());
                uploadFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 尝试删除格式转换缓存 */
    private void tryDelConvertCache(MLUploadModel data, File convertFile, MLUploadOption options) {
        //如果没有上传文件，或者上传文件的路径和原始文件路径相同，则不删除文件，直接返回
        if (convertFile == null || !convertFile.exists() || data.getUploadFilePath().equals(convertFile.getAbsolutePath())) {
            return;
        }

        try {
            LogUtil.d("删除格式转换文件：" + convertFile.getAbsolutePath());
            convertFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onProgress(long curr, long total) {
        super.onProgress(curr, total);
        MessageCallback callback = getMsg().getCallback();
        callback.onProgress(curr, total);
    }

    /**
     * 生成objectKey
     * <p>
     * 格式为：channelId + / + 用户自定义前缀 + 文件md5 + 当前毫秒时间戳 + 上传平台标示(android端为:0) + . + 文件后缀
     * <p>
     * 如果在获取md5等信息错误时，拼接规则如下：
     * <p>
     * channelId + / + 用户自定义前缀 + 当前纳秒时间戳 + md5失败标示(_) + 上传平台标示(android端为:0) + . + 文件后缀
     *
     * @param filePath
     * @param option
     * @return
     */
    protected String getObjectKey(String filePath, MLUploadOption option, String prefix) {

        File file = new File(filePath);
        if (!file.exists()) {
            throw new BaseException("上传文件不存在");
        }

        //文件后缀
        String suffix = "";
        if (filePath.contains(FILE_SUFFIX_SEPARATOR)) {
            suffix = filePath.substring(filePath.lastIndexOf(FILE_SUFFIX_SEPARATOR), filePath.length());
        }

        //ok结果
        StringBuilder result = new StringBuilder(prefix);

        //添加用户自定义path部分
        if (option != null && !TextUtils.isEmpty(option.idPrefix)) {
            if (option.idPrefix.startsWith("/")) {
                result.append(option.idPrefix.substring(1));
            } else {
                result.append(option.idPrefix);
            }
        }
        try {
            result.append(MD5.getFileMD5String(file))
                    .append(System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
            result.append(System.nanoTime())
                    .append(OBJECT_KEY_ERROR_FLAG);
        }

        result.append(PLATFORM_FLAG)
                .append(suffix);
        return result.toString();
    }
}
